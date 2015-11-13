package com.esoft.yeepay.query.controller;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.esoft.core.annotations.ScopeType;
import com.esoft.core.util.Dom4jUtil;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.yeepay.query.service.impl.YeePayAccountInfoOperation;
import com.esoft.yeepay.query.service.impl.YeePayQueryOperation;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;

@Component
@Scope(ScopeType.VIEW)
public class YeePayQueryHome {
	@Resource
	YeePayQueryOperation  yeePayQueryOperation;
	@Resource
	YeePayAccountInfoOperation  yeePayAccountInfoOperation;
	@Resource
	private HibernateTemplate ht;
	private String platformUserNo;
	private String orderId;
	private String orderType;
	private String result;
	public void query(){
		yeePayQueryOperation.handleSendedOperation(orderId, orderType);
	}
	public void queryAccountInfo(){
		yeePayAccountInfoOperation.handleSendedOperation(platformUserNo);
	}
	
	public void cancel(){
		Invest invest = ht.get(Invest.class, orderId);
		HttpClient client = new HttpClient();
		PostMethod postMethod = new PostMethod(
				YeePayConstants.RequestUrl.RequestDirectUrl);
		StringBuffer content = new StringBuffer();
		content.append("<?xml version='1.0' encoding='utf-8'?>");
		// 商户编号:商户在易宝唯一标识
		content.append("<request platformNo='"
				+ YeePayConstants.Config.MER_CODE + "'>");
		content.append("<platformUserNo>" + invest.getUser().getId()
				+ "</platformUserNo>");
		content.append("<requestNo>"+ YeePayConstants.RequestNoPre.INVEST + invest.getId() + "</requestNo>");
		content.append("</request>");

		postMethod.setParameter("req", content.toString());
		String sign = CFCASignUtil.sign(content.toString());
		postMethod.setParameter("sign", sign);
		postMethod.setParameter("service", "REVOCATION_TRANSFER");
		// 执行post方法
		try {
			int statusCode = client.executeMethod(postMethod);
			if (statusCode != HttpStatus.SC_OK) {
//				log.error("Method failed: " + postMethod.getStatusLine());
			}
			// 获得返回的结果
			byte[] responseBody = postMethod.getResponseBody();
//			log.debug(new String(responseBody, "UTF-8"));
			// 响应信息
			@SuppressWarnings("unchecked")
			Map<String, String> resultMap = Dom4jUtil.xmltoMap(new String(
					responseBody, "UTF-8"));
			// 返回码
			String code = resultMap.get("code");
			// 描述
			if (code.equals("1"))// 如果取消投标成功，执行业务逻辑。
			{
				result = "流标成功";
			}
		} catch (HttpException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			postMethod.releaseConnection();
		}
	}
	
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getPlatformUserNo() {
		return platformUserNo;
	}

	public void setPlatformUserNo(String platformUserNo) {
		this.platformUserNo = platformUserNo;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
}
