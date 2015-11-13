package com.esoft.yeepay.user.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.UserService;
import com.esoft.archer.user.service.impl.UserBO;
import com.esoft.core.annotations.Logger;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.DateStyle;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.Dom4jUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.exception.TrusteeshipReturnException;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipAccount;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
import com.esoft.yeepay.trusteeship.service.impl.YeePayOperationServiceAbs;
/**
 * 绑定手机修改
 * @author Zhangwq20150818
 *
 */
@Service("yeePayWeiXinUserMobileOperation")
public class YeePayWeiXinUserMobileOperation extends YeePayOperationServiceAbs<User>{
	
	@Resource
	TrusteeshipOperationBO trusteeshipOperationBO;
	
	@Resource
	HibernateTemplate ht;

	@Resource
	UserService userService;
	
	@Resource
	private UserBO userBO;
	
	@Logger
	static Log log;
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public TrusteeshipOperation createOperation(User user,
			FacesContext fc) throws IOException {
		StringBuffer content = new StringBuffer();
		content.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");		
		//商户编号
		content.append("<request platformNo='"+ YeePayConstants.Config.MER_CODE + "'>");
		// 请求流水号
		content.append("<requestNo>"+YeePayConstants.RequestNoPre.UPDATE_MOBILE + user.getId() + "</requestNo>");
		// 出款人平台用户编号
		content.append("<platformUserNo>" + user.getId() + "</platformUserNo>");
		
		// 回调通知 URL
		content.append("<callbackUrl>"
				+ YeePayConstants.ResponseWeiXinWebUrl.PRE_RESPONSE_URL
				+ YeePayConstants.OperationType.UPDATE_MOBILE
				+ "</callbackUrl>");
		// 服务器通知 URL
		content.append("<notifyUrl>"
				+ YeePayConstants.ResponseWeiXinS2SUrl.PRE_RESPONSE_URL
				+ YeePayConstants.OperationType.UPDATE_MOBILE + "</notifyUrl>");
		content.append("</request>");
		
		log.debug(content.toString());
		// 包装参数
		Map<String, String> params = new HashMap<String, String>();
		params.put("req", content.toString());
		String sign = CFCASignUtil.sign(content.toString());
		log.debug(sign);
		params.put("sign", sign);
		TrusteeshipOperation to = new TrusteeshipOperation();
		to.setId(IdGenerator.randomUUID());
		to.setMarkId(user.getId());
		to.setOperator(user.getId());
		
		to.setRequestUrl(YeePayConstants.RequestUrl.MOBILE_UPDATE_BOUND_MOBILE);

		to.setRequestData(MapUtil.mapToString(params));
		to.setStatus(TrusteeshipConstants.Status.UN_SEND);
		to.setType(YeePayConstants.OperationType.UPDATE_MOBILE);
		to.setTrusteeship("yeepay");
		trusteeshipOperationBO.save(to);
		try {
			super.sendOperation(to.getId(), "utf-8", fc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void receiveOperationPostCallback(ServletRequest request)
			throws TrusteeshipReturnException {

		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		// 响应的参数 为xml格式
		String respXML = request.getParameter("resp");
		log.debug(respXML.toString());
		// 签名
		String sign = request.getParameter("sign");
		boolean flag = CFCASignUtil.isVerifySign(respXML, sign);
		if (flag) {
			// 处理账户开通成功
			@SuppressWarnings("unchecked")
			Map<String, String> resultMap = Dom4jUtil.xmltoMap(respXML);
			// 请求流水号 注册时传递的userId
			String requestNo = resultMap.get("requestNo").replaceFirst(YeePayConstants.RequestNoPre.UPDATE_MOBILE, "");
			// 返回码
			String code = resultMap.get("code");
			String description = resultMap.get("description");
			TrusteeshipOperation to = trusteeshipOperationBO.get(
					YeePayConstants.OperationType.UPDATE_MOBILE, requestNo,
					requestNo, "yeepay");
			ht.evict(to);
			to = ht.get(TrusteeshipOperation.class, to.getId(),
					LockMode.UPGRADE);

			to.setResponseTime(new Date());
			to.setResponseData(respXML);
			// 以服务器通知为准 服务器通知会再次做处理
			if ("1".equals(code)) {
				User user = ht.get(User.class, requestNo);
				if (user != null) {
					to.setStatus(TrusteeshipConstants.Status.PASSED);
					ht.merge(to);
				}
			} else {
				to.setStatus(TrusteeshipConstants.Status.REFUSED);
				ht.merge(to);
				if ("0".equals(code)) {
					throw new TrusteeshipReturnException(description);
				}
				// 真实错误原因
				throw new TrusteeshipReturnException(code + ":" + description);
			}
		}

	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void receiveOperationS2SCallback(ServletRequest request,
			ServletResponse response) {
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		// 响应的参数 为xml格式
		String notifyxml = request.getParameter("notify");
		// 签名
		String sign = request.getParameter("sign");
		boolean flag = CFCASignUtil.isVerifySign(notifyxml, sign);
		if (flag) {
			// 处理账户开通成功
			@SuppressWarnings("unchecked")
			Map<String, String> resultMap = Dom4jUtil.xmltoMap(notifyxml);
			String code = resultMap.get("code");
			String message = resultMap.get("message");
			String platformUserNo = resultMap.get("platformUserNo");
			String newMobileNumber=resultMap.get("mobile");//新手机号Zhangwq20150824
			TrusteeshipOperation to = trusteeshipOperationBO.get(
					YeePayConstants.OperationType.UPDATE_MOBILE,
					platformUserNo, platformUserNo, "yeepay");
			ht.evict(to);
			to = ht.get(TrusteeshipOperation.class, to.getId(),
					LockMode.UPGRADE);
			to.setResponseTime(new Date());
			to.setResponseData(notifyxml);

			if ("1".equals(code)) {
				User user = ht.get(User.class, platformUserNo);
				if (user != null) {
					to.setStatus(TrusteeshipConstants.Status.PASSED);
					ht.merge(to);
					user.setMobileNumber(newMobileNumber);
					userBO.update(user);
				}
			} else if ("0".equals(code)) {
				to.setStatus(TrusteeshipConstants.Status.REFUSED);
				ht.merge(to);
			} else {
				// 真实错误原因
				throw new RuntimeException(new TrusteeshipReturnException(code
						+ ":" + message));
			}
		}

		try {
			response.getWriter().write("SUCCESS");
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		
	}

}
