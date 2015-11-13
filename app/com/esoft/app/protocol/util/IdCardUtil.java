package com.esoft.app.protocol.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author Hch
 * 解析身份证工具类
 *
 */
public class IdCardUtil {
	/**
	 * 
	 * @param idCard
	 * @return 判断该身份证是否是男或女,当身份证错误的时候返回null
	 */
	public static String getSex(String idCard){
		if(idCard!=null&&idCard.length()>0){
			if(idCard.length()==18){
				String str=idCard.substring(16, 17);
				int number=Integer.parseInt(str);
				if(number%2==0){
					//return "女";
					return "F";
				}else{
					//return "男";
					return "M";
				}
			}else if(idCard.length()==15){
				String str=idCard.substring(14,15);
				int number=Integer.parseInt(str);
				if(number%2==0){
					//return "女";
					return "F";
				}else{
					//return "男";
					return "M";
				}
			}else{
				return null;
			}
		}else{
			return null;
		}
	}
	/**
	 * 
	 * @param idCard
	 * @return 获取身份证中的出生日期,身份证异常返回空
	 * @throws ParseException 
	 */
	public static Date getDate(String idCard) throws ParseException{
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
		if(idCard!=null&&idCard.length()>0){
			if(idCard.length()==15){
				String year="19"+idCard.substring(6,8);
				String month=idCard.substring(8,10);
				String day=idCard.substring(10, 12);
				return format.parse(getDateStr(year,month,day));
			}else if(idCard.length()==18){
				String year=idCard.substring(6,10);
				String month=idCard.substring(10,12);
				String day=idCard.substring(12, 14);
				return format.parse(getDateStr(year,month,day));
			}else{
				return null;
			}
		}
		return null;
	}
	private static String getDateStr(String year,String month,String day){
		return new StringBuilder().append(year).append("-").append(month).append("-").append(day).toString();
	}
	public static void main(String[] args) throws Exception{
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(getSex("22240119910818123X"));
		System.out.println(format.format(getDate("22240119910818123X")));
		System.out.println(getSex("330702197601286024"));
		System.out.println(format.format(getDate("330702197601286024")));
	}
}
