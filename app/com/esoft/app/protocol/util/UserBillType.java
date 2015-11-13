package com.esoft.app.protocol.util;

import java.util.HashMap;
import java.util.Map;

import com.esoft.archer.user.UserBillConstants;

/**
 * 
 * @author Hch
 * 借款者账户汉化工具类
 *
 */
public class UserBillType {
	public static Map<String,String> typeNameMap=new HashMap<String,String>();
	public static Map<String,String> typeInfoNameMap=new HashMap<String,String>();
	static{
		typeNameMap.put(UserBillConstants.Type.FREEZE, "冻结");
		typeNameMap.put(UserBillConstants.Type.UNFREEZE, "解冻");
		typeNameMap.put(UserBillConstants.Type.TO_BALANCE, "从余额转出");
		typeNameMap.put(UserBillConstants.Type.TI_BALANCE, "转入到余额");
		typeNameMap.put(UserBillConstants.Type.TO_FROZEN, "从冻结金额中转出");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.INVEST_SUCCESS, "投资成功");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.ADMIN_OPERATION, "管理员干预");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.RECHARGE_SUCCESS, "充值成功");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.WITHDRAWALS_SUCCESS, "活动充值成功");//目前好像没用到这
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.APPLY_LOAN, "申请借款");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.REFUSE_APPLY_LOAN, "借款申请未通过");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.APPLY_WITHDRAW, "申请提现");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.REFUSE_APPLY_WITHDRAW, "提现申请未通过");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.NORMAL_REPAY, "正常还款");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.ADVANCE_REPAY, "提前还款");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.OVERDUE_REPAY, "逾期还款");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.CANCEL_LOAN, "借款流标");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.WITHDRAW_LOAN, "借款撤标");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.GIVE_MONEY_TO_BORROWER, "借款放款");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.WITHDRAW_SUCCESS, "提现成功");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.CANCEL_INVEST, "投资流标");
		typeInfoNameMap.put(UserBillConstants.OperatorInfo.TRANSFER, "债权转让");
	}
	public static String getValue(Map<String,String> map,String key){
		return map.get(key);
	}
}
