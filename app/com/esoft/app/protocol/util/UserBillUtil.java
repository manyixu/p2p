package com.esoft.app.protocol.util;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

import com.esoft.app.protocol.model.UserBillSub;
import com.esoft.archer.user.model.UserBill;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author Hch
 * 借款者工具类
 *
 */
public class UserBillUtil {
	public static UserBillSub getUserBillSub(GsonBuilder builder,UserBill userBill) throws IllegalAccessException, InvocationTargetException{
		UserBillSub sub=new UserBillSub();
		BeanUtils.copyProperties(sub, userBill);
		String typeValue=UserBillType.getValue(UserBillType.typeNameMap,userBill.getType());
		if(typeValue!=null){
			sub.setType(typeValue);
		}
		String typeInfoValue=UserBillType.getValue(UserBillType.typeInfoNameMap, userBill.getTypeInfo());
		if(typeInfoValue!=null){
			sub.setTypeInfo(typeInfoValue);
			
		}
		sub.setMoneyStr(NumberUtil.getNumber(userBill.getMoney()));
		sub.setBalanceStr(NumberUtil.getNumber(userBill.getBalance()));
		sub.setFrozenMoneyStr(NumberUtil.getNumber(userBill.getFrozenMoney()));
		return sub;
	}
}
