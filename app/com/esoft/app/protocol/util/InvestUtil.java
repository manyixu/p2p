package com.esoft.app.protocol.util;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

import com.esoft.app.protocol.model.InvestSub;
import com.esoft.archer.common.exception.NoMatchingObjectsException;
import com.esoft.archer.user.model.User;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.loan.service.LoanCalculator;

/**
 * 
 * @author Hch
 * 投资工具类
 *
 */
public class InvestUtil {
	public static InvestSub getInvestSub(Invest invest,LoanCalculator loanCalculator) throws IllegalAccessException, InvocationTargetException, NoMatchingObjectsException{
		InvestSub sub=new InvestSub();
		BeanUtils.copyProperties(sub,invest);
		sub.getRepayRoadmap();
		Loan loan=invest.getLoan();
		sub.setLoanId(loan.getId());
		sub.setLoanName(loan.getName());
		sub.setJd(loanCalculator.calculateRaiseCompletedRate(loan.getId()));
		sub.setUnPaidMoney(invest.getRepayRoadmap().getUnPaidMoney());
		sub.setMoney(invest.getMoney());
		sub.setCompleteTime(loan.getCompleteTime());
		return sub;
	}
	
	public static InvestSub getInvestSub(Invest invest) throws IllegalAccessException, InvocationTargetException, NoMatchingObjectsException{
		InvestSub sub=new InvestSub();
		BeanUtils.copyProperties(sub,invest);
		sub.getRepayRoadmap();
		User user=invest.getUser();
		sub.setUserId(user.getId());
		sub.setUserName(user.getUsername());
		sub.setUnPaidMoney(invest.getRepayRoadmap().getUnPaidMoney());
		sub.setMoney(invest.getMoney());
		return sub;
	}
}
