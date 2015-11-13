package com.esoft.app.protocol.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import com.esoft.app.protocol.model.LoanSub;
import com.esoft.archer.common.controller.StringHome;
import com.esoft.archer.common.exception.NoMatchingObjectsException;
import com.esoft.archer.system.controller.DictUtil;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.loan.service.LoanCalculator;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author Hch
 * 项目工具类
 *
 */
public class LoanUtil {
	public static LoanSub getLoanSub(GsonBuilder builder,Loan loan,LoanCalculator loanCalculator,DictUtil dictUtil,Map<String,String> htMap) throws IllegalAccessException, InvocationTargetException, NoMatchingObjectsException{
		String statusValue=LoanType.getValue(LoanType.statusNameMap,loan.getStatus());
		LoanSub sub=new LoanSub();
		BeanUtils.copyProperties(sub, loan);
		double jd = loanCalculator.calculateRaiseCompletedRate(loan.getId());
		sub.setJd(((int)jd)+"");//进度
		sub.setLastStrTime(loanCalculator.calculateRemainTime(loan.getId()));//最后时间
		sub.setSybj(loanCalculator.calculateMoneyNeedRaised(loan.getId()));//剩余本金
		sub.setDescription(StringHome.removeLabel(sub.getDescription()));
		sub.setCompanyDescription(StringHome.removeLabel(sub.getCompanyDescription()));
		sub.setGuaranteeInfoDescription(StringHome.removeLabel(sub.getGuaranteeInfoDescription()));
		sub.setGuaranteeCompanyDescription(StringHome.removeLabel(sub.getGuaranteeCompanyDescription()));
		sub.setRepayTimeUnit(loan.getType().getRepayTimeUnit());
		if(statusValue!=null){
			sub.setStatus(statusValue);
		}
		sub.setContractType(htMap.get(sub.getContractType()));
		sub.setRepayType(dictUtil.getValue("repay_type", loan.getType().getRepayType()));
		return sub;
	}
}
