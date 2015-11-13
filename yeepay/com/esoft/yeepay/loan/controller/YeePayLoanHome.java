package com.esoft.yeepay.loan.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.exception.NoMatchingObjectsException;
import com.esoft.archer.notice.model.Notice;
import com.esoft.archer.notice.model.NoticePool;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.jdp2p.invest.InvestConstants;
import com.esoft.jdp2p.invest.InvestConstants.InvestStatus;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.loan.controller.LoanHome;
import com.esoft.jdp2p.loan.exception.ExistWaitAffirmInvests;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.loan.model.OperationLog;
import com.esoft.jdp2p.loan.service.LoanCalculator;
import com.esoft.yeepay.loan.service.impl.YeePayCancelInvestOperation;
import com.esoft.yeepay.loan.service.impl.YeePayRecheckOperation;
import com.esoft.yeepay.trusteeship.exception.YeePayOperationException;

@Component
@Scope(ScopeType.VIEW)
public class YeePayLoanHome extends LoanHome {
	@Resource
	private LoginUserInfo loginUserInfo;
	@Resource
	YeePayRecheckOperation yeePayRecheckOperation;
	@Resource
	YeePayCancelInvestOperation yeePayCancelInvestOperation;
	@Autowired
	private HttpServletRequest request;
	@Resource
	private OperationLogService operationLogService;
	@Resource
	private LoanCalculator loanCalculator;
	@Resource
	NoticePool noticePool;
	/**
	 * 放款
	 */
	@Override
	@Transactional(readOnly = false)
	public String recheck() {
		Loan loan = getBaseService().get(Loan.class, getInstance().getId());
		Double jd = 0.0;
		try {
			jd = loanCalculator.calculateRaiseCompletedRate(loan.getId());
		} catch (NoMatchingObjectsException e1) {
			e1.printStackTrace();
		}
		if(jd<100){
			FacesUtil.addInfoMessage("标未满，不可以放款.");
			return null;
		}
		List<Invest> invests = loan.getInvests();
		for (Invest invest : invests) {
			if (invest.getStatus().equals(
					InvestConstants.InvestStatus.WAIT_AFFIRM)) {
				FacesUtil.addInfoMessage("放款失败，存在等待第三方资金托管确认的投资。");
				return null;
			}
		}
		try {
			yeePayRecheckOperation.createOperation(loan);
			//后台操作日志
			OperationLog operationLog = new OperationLog();
			operationLog.setOperationId(loan.getId());
			operationLog.setOperationIp(FacesUtil.getRequestIp(request));
			operationLog.setOperationName(loan.getName());
			operationLog.setOperationAction("放款");
			operationLog.setOperationDate(new Date());
			operationLog.setOperationType("项目管理");
			operationLog.setOperationUser(loginUserInfo.getLoginUserId());
			operationLogService.save(operationLog);
			FacesUtil.addInfoMessage("放款成功");
			noticePool.add(new Notice("借款：" + getInstance().getId() + "放款成功"));
			return FacesUtil.redirect(loanListUrl);

		} catch (ExistWaitAffirmInvests e) {
			FacesUtil.addInfoMessage("放款失败，存在等待第三方资金托管确认的投资。");
		} catch (YeePayOperationException e) {
			FacesUtil.addInfoMessage(e.getMessage());
		}
		return null;
	}

	@Override
	@Transactional(readOnly = false)
	public String failByManager() {
		Long l = (Long) getBaseService()
				.find("select count(invest) from Invest invest where invest.loan.id=? and invest.status=?",
						getInstance().getId(), InvestStatus.WAIT_AFFIRM).get(0);
		if (l > 0) {
			FacesUtil.addInfoMessage("流标失败，存在等待第三方资金托管确认的投资。");
			return null;
		}
		yeePayCancelInvestOperation.createOperation(this.getInstance().getId(),
				loginUserInfo.getLoginUserId());
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(getInstance().getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(getInstance().getName());
		operationLog.setOperationAction("流标");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("项目管理");
		operationLog.setOperationUser(loginUserInfo.getLoginUserId());
		operationLogService.save(operationLog);
		FacesUtil.addInfoMessage("流标成功");
		return FacesUtil.redirect(loanListUrl);
	}

	@Override
	public Class<Loan> getEntityClass() {
		return Loan.class;
	}

}
