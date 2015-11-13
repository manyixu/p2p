package com.esoft.yeepay.recharge.controller;

import java.io.IOException;
import java.util.Date;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.esoft.archer.user.controller.RechargeHome;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.DateStyle;
import com.esoft.jdp2p.user.service.RechargeService;
import com.esoft.yeepay.recharge.service.impl.YeePayRechargeOperation;

public class YeePayRechargeHome extends RechargeHome{
	@Resource
	YeePayRechargeOperation yeePayRechargeOperation;
	@Resource
	private RechargeService rechargeService;
	
	/**
	 * 充值
	 */
	@Override
	public void recharge() {
		
		this.getInstance().setRechargeWay("YeePay");
		try {
			yeePayRechargeOperation.createOperation(this.getInstance(),
					FacesContext.getCurrentInstance());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void calculateFee() {
		/*
		 * 充值限制条件：1单日最高10万，单月最高10万，单笔最高5万
		 */
		String userId=this.getInstance().getUser().getId();
		Date date = new Date();
		//当日已充值金额
		Double dayMaxMoney=rechargeService.getRechargeMoney("D", userId, date);
		//当月已充值金额
		Double monthMaxMoney=rechargeService.getRechargeMoney("M", userId, date);
		if(dayMaxMoney!=null){
			if(dayMaxMoney>400000){
				FacesUtil.addInfoMessage("单日充值最高金额不能超过40万！");
				return;
			}
		}
		if(monthMaxMoney!=null){
			if(monthMaxMoney>10000000){
				FacesUtil.addInfoMessage("每月充值最高金额不能超过1000万！");
				return;
			}
		}
		double fee = rechargeService.calculateFee(this.getInstance()
				.getActualMoney());
		this.getInstance().setFee(fee);
	}

	
}
