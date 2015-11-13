package com.esoft.yeepay.invest.controller;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.esoft.archer.user.service.UserBillService;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.Dom4jUtil;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;

@Component
@Scope(ScopeType.VIEW)
public class YeePayCancelInvestHome {

	@Logger
	Log log;

	@Resource
	HibernateTemplate ht;

	@Resource
	UserBillService ubs;

	private String investId;

	public void cancelInvest() {
		Invest invest = ht.get(Invest.class, investId);
		if (invest == null) {
			FacesUtil.addErrorMessage("该投标不存在！");
			return;
		}
		HttpClient client = new HttpClient();
		// 创建一个post方法
		PostMethod postMethod = new PostMethod(
				YeePayConstants.RequestUrl.RequestDirectUrl);
		StringBuffer content = new StringBuffer();
		content.append("<?xml version='1.0' encoding='utf-8'?>");
		// 商户编号:商户在易宝唯一标识
		content.append("<request platformNo='"
				+ YeePayConstants.Config.MER_CODE + "'>");
		content.append("<platformUserNo>" + invest.getUser().getId()
				+ "</platformUserNo>");
		Long count = (Long) ht
				.find("select count(to) from TrusteeshipOperation to where to.type=? and to.markId=? and to.requestData like ?",
						YeePayConstants.OperationType.INVEST, invest.getId(),
						"%<requestNo>03" + invest.getId() + "</requestNo>%")
				.get(0);
		if (count == 0) {
			// 未添加流水号前缀
			content.append("<requestNo>" + invest.getId() + "</requestNo>");
		} else if (count == 1) {
			content.append("<requestNo>" + YeePayConstants.RequestNoPre.INVEST
					+ invest.getId() + "</requestNo>");
		}
		content.append("</request>");

		postMethod.setParameter("req", content.toString());
		String sign = CFCASignUtil.sign(content.toString());
		postMethod.setParameter("sign", sign);
		postMethod.setParameter("service", "REVOCATION_TRANSFER");
		// 执行post方法
		try {
			int statusCode = client.executeMethod(postMethod);
			if (statusCode != HttpStatus.SC_OK) {
				log.error("Method failed: " + postMethod.getStatusLine());
			}
			// 获得返回的结果
			byte[] responseBody = postMethod.getResponseBody();
			log.debug(new String(responseBody, "UTF-8"));
			// 响应信息
			@SuppressWarnings("unchecked")
			Map<String, String> resultMap = Dom4jUtil.xmltoMap(new String(
					responseBody, "UTF-8"));
			// 返回码
			String code = resultMap.get("code");
			// 描述
			if (code.equals("1"))// 取消投标成功
			{
				FacesUtil.addInfoMessage("流标成功~~~");
			}
		} catch (HttpException e) {
			log.error("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.error("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			/* Release the connection. */
			postMethod.releaseConnection();
		}
	}

	public String getInvestId() {
		return investId;
	}

	public void setInvestId(String investId) {
		this.investId = investId;
	}

}
