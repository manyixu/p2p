package com.esoft.yeepay.loan.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.user.UserBillConstants.OperatorInfo;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.annotations.Logger;
import com.esoft.core.util.ArithUtil;
import com.esoft.jdp2p.invest.InvestConstants;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.loan.LoanConstants;
import com.esoft.jdp2p.loan.exception.BorrowedMoneyTooLittle;
import com.esoft.jdp2p.loan.exception.ExistWaitAffirmInvests;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.loan.service.impl.LoanServiceImpl;
import com.esoft.jdp2p.repay.service.RepayService;
import com.esoft.jdp2p.risk.service.SystemBillService;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class YeePayLoanServiceImpl extends LoanServiceImpl {
	
	@Logger
	private static Log log;

	@Resource
	HibernateTemplate ht;

	@Resource
	UserBillBO ubs;

	@Resource
	SystemBillService sbs;

	@Resource
	RepayService repayService;

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void giveMoneyToBorrower(String loanId)
			throws ExistWaitAffirmInvests, BorrowedMoneyTooLittle {
		Loan loan = ht.get(Loan.class, loanId);
		ht.evict(loan);
		loan=  ht.get(Loan.class, loanId, LockMode.UPGRADE);

		// 更改项目状态，放款。
		loan.setStatus(LoanConstants.LoanStatus.REPAYING);
		// 获取当前日期
		Date dateNow = new Date();
		// 设置放款日期
		loan.setGiveMoneyTime(dateNow);

		// 实际到借款账户的金额
		double actualMoney = 0D;

		List<Invest> investSs = Lists.newArrayList(Collections2.filter(
				loan.getInvests(), new Predicate<Invest>() {
					public boolean apply(Invest invest) {
						return invest
								.getStatus()
								.equals(InvestConstants.InvestStatus.WAIT_LOANING_VERIFY);
					}
				}));
		for (Invest invest : investSs) {
			try {
				ubs.transferOutFromFrozen(invest.getUser().getId(),
						invest.getMoney(), OperatorInfo.GIVE_MONEY_TO_BORROWER,
						"投资成功，取出投资金额, 借款ID：" + loan.getId());
				actualMoney = ArithUtil.add(actualMoney,
						invest.getInvestMoney());
			} catch (InsufficientBalance e) {
				throw new RuntimeException(e);
			}
			// 更改投资状态
			invest.setStatus(InvestConstants.InvestStatus.REPAYING);
			ht.update(invest);
		}
		// 设置借款实际借到的金额
		loan.setMoney(actualMoney);
		// 根据借款期限产生还款信息
		repayService.generateRepay(loan.getId());

		// 把借款转给借款人账户
		ubs.transferIntoBalance(loan.getUser().getId(), actualMoney,
				OperatorInfo.GIVE_MONEY_TO_BORROWER,
				"借款到账, 借款ID：" + loan.getId());
		try {
			ubs.unfreezeMoney(loan.getUser().getId(), loan.getDeposit(),
					OperatorInfo.GIVE_MONEY_TO_BORROWER, "借款成功，解冻借款保证金, 借款ID："
							+ loan.getId());
			ubs.transferOutFromBalance(loan.getUser().getId(),
					loan.getLoanGuranteeFee(),
					OperatorInfo.GIVE_MONEY_TO_BORROWER, "取出借款管理费, 借款ID："
							+ loan.getId());
			log.debug("YeePay===>>借款管理费,用户ID："+loan.getUser().getId()+", 借款ID：" + loan.getId());
			sbs.transferInto(loan.getLoanGuranteeFee(),
					OperatorInfo.GIVE_MONEY_TO_BORROWER,
					"借款管理费,用户ID："+loan.getUser().getId()+", 借款ID：" + loan.getId());
		} catch (InsufficientBalance e) {
			throw new RuntimeException(e);
		}
		ht.merge(loan);
	}

}
