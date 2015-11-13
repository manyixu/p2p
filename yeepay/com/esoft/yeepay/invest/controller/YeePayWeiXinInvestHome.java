package com.esoft.yeepay.invest.controller;

import java.io.IOException;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;

import com.esoft.archer.config.controller.ConfigHome;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.user.model.User;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.jdp2p.invest.controller.InvestHome;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.invest.service.InvestService;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.repay.service.RepayService;
import com.esoft.yeepay.invest.service.impl.YeePayInvestTransferNofreezeOperation;
import com.esoft.yeepay.invest.service.impl.YeePayInvestTransferOperation;
import com.esoft.yeepay.invest.service.impl.YeePayWeiXinInvestNofreezeOperation;
import com.esoft.yeepay.invest.service.impl.YeePayWeiXinInvestOperation;
import com.esoft.yeepay.trusteeship.exception.YeePayOperationException;

public class YeePayWeiXinInvestHome extends InvestHome {
	@Resource
	private YeePayWeiXinInvestOperation yeePayWeiXinInvestOperation;
	@Resource
	private YeePayWeiXinInvestNofreezeOperation yeePayWeiXinInvestNofreezeOperation;

	@Resource
	private YeePayInvestTransferOperation yeePayInvestTransferOperation;
	@Resource
	private YeePayInvestTransferNofreezeOperation yeePayInvestTransferNofreezeOperation;

	@Resource
	private InvestService investService;

	@Resource
	private LoginUserInfo loginUserInfo;

	@Resource
	private RepayService repayService;
	
	@Resource
	private ConfigHome configHome;
	/**
	 * 投资
	 * 
	 * @return
	 */
	@Override
	public String save() {
		Loan loan = getBaseService().get(Loan.class,
				getInstance().getLoan().getId());
		if (loan.getUser().getId()
				.equals(loginUserInfo.getLoginUserId())) {
			FacesUtil.addInfoMessage("你不能投自己的项目！");
			return null;
		}
		this.getInstance().setUser(new User(loginUserInfo.getLoginUserId()));
		this.getInstance().setIsAutoInvest(false);
		try {
			if("0".equals(configHome.getConfigValue("freeze_money"))){
				yeePayWeiXinInvestNofreezeOperation.createOperation(getInstance(), FacesContext.getCurrentInstance());
			}else{
				yeePayWeiXinInvestOperation.createOperation(getInstance(),
						FacesContext.getCurrentInstance());
			}
		} catch (YeePayOperationException e) {
			FacesUtil.addInfoMessage(e.getMessage());
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public String transfer() {
		Invest invest = this.getInstance();
		invest.setUser(new User(loginUserInfo.getLoginUserId()));
		try {
			if("0".equals(configHome.getConfigValue("freeze_money"))){
				yeePayInvestTransferNofreezeOperation.createOperation(invest,
						FacesContext.getCurrentInstance());
			}else{
				yeePayInvestTransferOperation.createOperation(invest,
						FacesContext.getCurrentInstance());
			}
		} catch (YeePayOperationException e) {
			FacesUtil.addInfoMessage(e.getMessage());
			return null;
		}catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Class<Invest> getEntityClass() {
		return Invest.class;
	}
}
