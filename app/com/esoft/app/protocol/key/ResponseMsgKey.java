package com.esoft.app.protocol.key;

public class ResponseMsgKey {
	/**
	 * 验证失败
	 */
	public static final String ILLEGAL_SIGN = "ILLEGAL_SIGN";
	/**
	 * 请求参数不合法
	 */
	public static final String PARAMETER_INVALID = "PARAMETER_INVALID";
	/**
	 * 请求方法不存在
	 */
	public static final String METHOD_NOT_FOUND = "METHOD_NOT_FOUND";
	/**
	 * 解密失败,请检查加密字段
	 */
	public static final String ILLEGAL_DECRYPT = "ILLEGAL_DECRYPT";
	/**
	 * 处理成功
	 */
	public static final String SUCCESS = "SUCCESS";
	/**
	 * 处理失败
	 */
	public static final String ERROR="ERROR";
	/**
	 * 没有找到用户或密码
	 */
	public static final String LOGIN_NOT_FIND="LOGIN_NOT_FIND";
	/**
	 * 数据库中没找到项目
	 */
	public static final String LOAN_NOT_FIND="LOAN_NOT_FIND";
	/**
	 * 对象没找到
	 */
	public static final String OBJ_NOT_FIND="OBJ_NOT_FIND";
	/**
	 * 注册异常
	 */
	public static final String REGIST_ERROR="REGIST_ERROR";
	/**
	 * 投资失败
	 */
	public static final String INVEST_ERROR="INVEST_ERROR";
	/**
	 * 密码操作异常
	 */
	public static final String PWD_ERROR="PWD_ERROR";
	/**
	 * 短信异常
	 */
	public static final String SMS_ERROR="SMS_ERROR";
	/**
	 * 修改手机号异常
	 */
	public static final String PHONE_UP_ERROR="PHONE_UP_ERROR";
	/**
	 * 操作银行卡异常
	 */
	public static final String BANK_CARD_ERROR="BANK_CARD_ERROR";
	/**
	 * 该用户没有投资人权限
	 */
	public static final String NO_INVESTOR="NO_INVESTOR";
	/**
	 * 投资者异常
	 */
	public static final String INVESTOR_ERROR="INVESTOR_ERROR";
	/**
	 * 计算手续费和罚金异常
	 */
	public static final String CALCULATE_FEE_ERROR="CALCULATE_FEE_ERROR";
	/**
	 * 提现异常
	 */
	public static final String WITHDRAW_ERROR="WITHDRAW_ERROR";
	/**
	 * 易宝异常
	 */
	public static final String YEEPAY_ERROR="YEEPAY_ERROR";
}
