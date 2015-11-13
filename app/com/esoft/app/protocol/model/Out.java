package com.esoft.app.protocol.model;

import org.apache.commons.codec.digest.DigestUtils;

import com.esoft.app.AppConstants.Config;
import com.esoft.core.util.ThreeDES;

/**
 * 从app端收到的一次内容
 * 
 * @author Administrator
 * 
 */
public class Out {
	/** 返回号码 （请求状态） */
	private String resultCode;
	/** 返回信息 （请求处理信息） */
	private String resultMsg;

	/** 设备编号 （请求设备编号） */
	private String deviceId;
	/** 请求编号 */
	private String requestId;
	/** 请求的方法 */
	private String method;
	/** 返回结果（需3des加密） */
	private String result;

	/** 请求时候的备注 */
	private String remark;
	/** MD5(deviceId+requestId+method+result) */
	private String sign;
	/** 返回时间 */
	private long time;

	public Out() {
	}

	public Out(String resultCode, String resultMsg, String deviceId,
			String requestId, String method, String result, String remark) {
		this.resultCode = resultCode;
		this.resultMsg = resultMsg;
		this.deviceId = deviceId;
		this.requestId = requestId;
		this.method = method;
		this.remark = remark;
		this.time = System.currentTimeMillis();
		encryptResult(result);
		sign();
	}

	/**
	 * 加密
	 * 
	 * @param result
	 */
	public void encryptResult(String result) {
		if (result != null) {
			this.result = ThreeDES.encrypt(result, Config.THREE_DES_BASE64_KEY,
					Config.THREE_DES_IV, Config.THREE_DES_ALGORITHM);
		}
	}

	/**
	 * 签名
	 */
	public void sign() {
		this.sign = DigestUtils.md5Hex(deviceId + requestId + method + result);
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		if (resultMsg != null) {
			this.resultMsg = ThreeDES.encrypt(resultMsg,
					Config.THREE_DES_BASE64_KEY, Config.THREE_DES_IV,
					Config.THREE_DES_ALGORITHM);
		}
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

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
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
