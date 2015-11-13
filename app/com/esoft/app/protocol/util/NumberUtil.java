package com.esoft.app.protocol.util;

import java.text.DecimalFormat;

/**
 * 
 * @author Hch
 * 数字工具类
 */
public class NumberUtil {
	public static String getNumber(Double number){
		if(number!=null){
			DecimalFormat df=new DecimalFormat("0.00");
			return df.format(number);
		}else{
			return "";
		}
	}
}
