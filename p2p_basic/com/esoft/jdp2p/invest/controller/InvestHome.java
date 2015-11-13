package com.esoft.jdp2p.invest.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.user.model.User;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.ArithUtil;
import com.esoft.jdp2p.coupon.exception.ExceedDeadlineException;
import com.esoft.jdp2p.coupon.exception.UnreachedMoneyLimitException;
import com.esoft.jdp2p.invest.InvestConstants;
import com.esoft.jdp2p.invest.exception.ExceedInvestTransferMoney;
import com.esoft.jdp2p.invest.exception.ExceedMaxAcceptableRate;
import com.esoft.jdp2p.invest.exception.ExceedMoneyNeedRaised;
import com.esoft.jdp2p.invest.exception.IllegalLoanStatusException;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.invest.model.TransferApply;
import com.esoft.jdp2p.invest.service.InvestService;
import com.esoft.jdp2p.invest.service.TransferService;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.repay.service.RepayService;

/**
 * Filename: InvestHome.java Description: Copyright: Copyright (c)2013 Company:
 * jdp2p
 * 
 * @author: yinjunlu
 * @version: 1.0 Create at: 2014-1-11 下午4:26:10
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-11 yinjunlu 1.0 1.0 Version
 */
@Component
@Scope(ScopeType.VIEW)
public class InvestHome extends EntityHome<Invest> implements Serializable{
	
	
	@Resource
	private InvestService investService;

	@Resource
	private LoginUserInfo loginUserInfo;

	@Resource
	private RepayService repayService;
	
	@Resource
	private TransferService transferService;
	//判断新手标是否投过
	private String investLoanPD;
	@Resource
	HibernateTemplate ht;

	@Override
	protected Invest createInstance() {
		Invest invest = new Invest();
		TransferApply ta = new TransferApply();
		Loan loan = new Loan();
		loan.setUser(new User());
		invest.setLoan(loan);
		invest.setTransferApply(ta);
		return invest;
	}
	
	/**购买债权转让*/
	public String transfer(){
		try {
			if (loginUserInfo.getLoginUserId().equals(getInstance().getTransferApply().getInvest().getUser().getId())) {
				FacesUtil.addErrorMessage("您不能购买自己的债权");
				return null;
			}
			transferService.transfer(getInstance().getTransferApply().getId(), loginUserInfo.getLoginUserId(), getInstance().getInvestMoney());
			FacesUtil.addInfoMessage("购买成功！");
			return "pretty:user-transfer-purchased";
		} catch (InsufficientBalance e) {
			FacesUtil.addErrorMessage("余额不足");
		} catch (ExceedInvestTransferMoney e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		return null;
	}
	
	/**
	 * 投资
	 */
	public String save() {
		try {
			Loan loan = getBaseService().get(Loan.class,
					getInstance().getLoan().getId());
			if (loan.getUser().getId()
					.equals(loginUserInfo.getLoginUserId())) {
				FacesUtil.addInfoMessage("你不能投自己的项目！");
				return null;
			} else {
				this.getInstance().setUser(
						new User(loginUserInfo.getLoginUserId()));
				this.getInstance().setIsAutoInvest(false);
				investService.create(getInstance());
			}
		} catch (InsufficientBalance e) {
			FacesUtil.addErrorMessage("账户余额不足，请充值！");
			return null;
		} catch (ExceedMoneyNeedRaised e) {
			FacesUtil.addErrorMessage("投资金额不能大于尚未募集的金额！");
			return null;
		} catch (ExceedMaxAcceptableRate e) {
			FacesUtil.addErrorMessage("竞标利率不能大于借款者可接受的最高利率！");
			return null;
		} catch (ExceedDeadlineException e) {
			FacesUtil.addErrorMessage("优惠券已过期");
			return null;
		} catch (UnreachedMoneyLimitException e) {
			FacesUtil.addErrorMessage("投资金额未到达优惠券使用条件");
			return null;
		} catch (IllegalLoanStatusException e) {
			FacesUtil.addErrorMessage("当前借款不可投资");
			return null;
		}
		FacesUtil.addInfoMessage("投资成功！");
		if (FacesUtil.isMobileRequest()) {
			return "pretty:mobile_user_invests";
		}
		return "pretty:user_invest_bidding";
	}
	
	
	/**
	 * 微信投资
	 */
	public String weixinSave() {
		try {
			Loan loan = getBaseService().get(Loan.class,
					getInstance().getLoan().getId());
			if (loan.getUser().getId()
					.equals(loginUserInfo.getLoginUserId())) {
				FacesUtil.addInfoMessage("你不能投自己的项目！");
				return null;
			} else {
				this.getInstance().setUser(
						new User(loginUserInfo.getLoginUserId()));
				this.getInstance().setIsAutoInvest(false);
				investService.create(getInstance());
			}
		} catch (InsufficientBalance e) {
			FacesUtil.addErrorMessage("账户余额不足，请充值！");
			return null;
		} catch (ExceedMoneyNeedRaised e) {
			FacesUtil.addErrorMessage("投资金额不能大于尚未募集的金额！");
			return null;
		} catch (ExceedMaxAcceptableRate e) {
			FacesUtil.addErrorMessage("竞标利率不能大于借款者可接受的最高利率！");
			return null;
		} catch (ExceedDeadlineException e) {
			FacesUtil.addErrorMessage("优惠券已过期");
			return null;
		} catch (UnreachedMoneyLimitException e) {
			FacesUtil.addErrorMessage("投资金额未到达优惠券使用条件");
			return null;
		} catch (IllegalLoanStatusException e) {
			FacesUtil.addErrorMessage("当前借款不可投资");
			return null;
		}
		FacesUtil.addInfoMessage("投资成功！");
		return "pretty:weixinapp_user_repaying_repaying";
	}
	
	/**
	 * 获取某个用户总的投资额
	 * 
	 * @return
	 */
	public double getSumInvest(String userId) {
		Object o = getBaseService().find(
				"select sum(im.money) from Invest im where im.user.id=?",
				userId).get(0);
		if (o == null) {
			return 0;
		}
		return (Double) o;
	}
	
	/**
	  * 获取某个用户获得的总的投资额(投资成功的)
	  * 取状态投资已完成和债权转让成功
	  * yinxunzhi
	  * @return
	  */
	 public double getSumInvestMoney(String userId) {
	  Object o = getBaseService()
	    .find("select sum(im.money) from Invest im where im.user.id=? and im.status in (?,?)",
	      new String[] { userId,
	        InvestConstants.InvestStatus.COMPLETE}).get(0);
	  if (o == null) {
	   return 0;
	  }
	  return (Double) o;
	 }


	/**
	 * 某个用户投资的数量
	 * 
	 * @return
	 */
	public long getInvestAmount(String userId) {
		String hql = "select count(*) from Invest invest where invest.user.id=?";
		return (Long) getBaseService().iterate(hql, userId).next();
	}
	
		/**
		 * 获取是否投资过新手标
		 * 董
		 * @return
		 */
		public String getInvestLoanPD() {
			String hql = "select invest.id from Invest invest " +
					"left join invest.loan loa "+
					"left join loa.loanAttrs attr where  attr.id = 'freshman' and invest.user.id=?";
/*			String hql = "select invest.id from Invest invest " +
					"left join invest.loan loan "+
					"left join loan.loanAttrs attr where  attr.id = 'freshman'" +
					" where invest.user.id=?";
*/			
			if(loginUserInfo.getLoginUserId()!=null){
			List<Object> oos = ht.find(hql, new String[] { loginUserInfo.getLoginUserId()});
			if(oos.size()>0){
				return "Y";
			}else{
				return "N";
			}
			}
			return "N";

		}

		public void setInvestLoanPD(String investLoanPD) {
			this.investLoanPD = investLoanPD;
		}
		
		
		/**
		 * 规定新手标投资额度
		 */
		public Double makeTopInvest(String userId,String loanId){
					Double invest = (Double) getBaseService().find(
					"select sum(im.money) from Invest im where im.user.id=? and im.loan.id=?",
					userId ,loanId).get(0);
					Loan loan = ht.get(Loan.class, loanId);
				    double left = loan.getTotalMoney() - invest;
				    return left;
		}
				
				
}
