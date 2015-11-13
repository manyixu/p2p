package com.esoft.app.protocol.model;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;

import com.esoft.app.AppConstants.Config;
import com.esoft.core.util.ThreeDES;

/**
 * 从app端收到的一次内容
 * 
 * @author Administrator
 * 
 */
public class In {
	/** 设备编号（设备唯一标识） */
	private String deviceId;
	/** 请求编号（同一设备编号下，请求编号不能重复） */
	private String requestId;
	/** 请求方法 */
	private String method;
	/** 请求值（需3des加密） */
	private String value;

	/** 备注（原封不动返回） */
	private String remark;
	/** MD5(deviceId+requestId+method+value) */
	private String sign;
	/** 请求时间 */
	private long time;

	public In() {
	}

	public In(HttpServletRequest request) {
		this.deviceId = request.getParameter("deviceId");
		this.requestId = request.getParameter("requestId");
		;
		this.method = request.getParameter("method");
		;
		this.value = request.getParameter("value");
		;
		this.remark = request.getParameter("remark");
		;
		this.sign = request.getParameter("sign");
		;
		this.time = System.currentTimeMillis();	
		
	}

	/**
	 * 校验
	 * @return
	 */
	public Boolean verify() {
		return DigestUtils.md5Hex(deviceId + requestId + method + value)
				.equals(sign);
	}

	/**
	 * 解密
	 * @return
	 */
	public String decryptValue() {
		return ThreeDES.decrypt(value, Config.THREE_DES_BASE64_KEY,
				Config.THREE_DES_IV, Config.THREE_DES_ALGORITHM);
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

}
