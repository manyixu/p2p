package com.esoft.app.protocol.util;

import java.util.HashMap;
import java.util.Map;

import com.esoft.jdp2p.loan.LoanConstants;

/**
 * 
 * @author Hch
 * 项目状态汉化工具类
 *
 */
public class LoanType {
	public static Map<String,String> statusNameMap=new HashMap<String,String>();
	static{
		statusNameMap.put(LoanConstants.LoanStatus.WAITING_VERIFY, "等待审核");
		statusNameMap.put(LoanConstants.LoanStatus.WAITING_VERIFY_AFFIRM, "审核后等待第三方确认");
		statusNameMap.put(LoanConstants.LoanStatus.WAITING_VERIFY_AFFIRM_USER, "审核后等待用户确认");
		statusNameMap.put(LoanConstants.LoanStatus.DQGS, "贷前公示");
		statusNameMap.put(LoanConstants.LoanStatus.VERIFY_FAIL, "审核未通过");
		statusNameMap.put(LoanConstants.LoanStatus.RAISING, "筹款中");
		statusNameMap.put(LoanConstants.LoanStatus.RECHECK, "等待复核");
		statusNameMap.put(LoanConstants.LoanStatus.WAITING_RECHECK_VERIFY, "放款后，等待确认");
/*		statusNameMap.put(LoanConstants.LoanStatus.WITHDRAWAL, "撤标");*/
		statusNameMap.put(LoanConstants.LoanStatus.CANCEL, "流标");
		statusNameMap.put(LoanConstants.LoanStatus.REPAYING, "还款中");
		statusNameMap.put(LoanConstants.LoanStatus.OVERDUE, "逾期");
		statusNameMap.put(LoanConstants.LoanStatus.BAD_DEBT, "坏账");
	}
	public static String getValue(Map<String,String> map,String key){
		return map.get(key);
	}
}
