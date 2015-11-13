package com.esoft.yeepay.autoinvest.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.config.service.ConfigService;
import com.esoft.archer.user.UserBillConstants.OperatorInfo;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.annotations.Logger;
import com.esoft.core.util.Dom4jUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.jdp2p.invest.InvestConstants;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.invest.service.InvestService;
import com.esoft.jdp2p.loan.LoanConstants;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.loan.service.LoanService;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.exception.TrusteeshipReturnException;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
import com.esoft.yeepay.trusteeship.service.impl.YeePayOperationServiceAbs;
import com.esoft.yeepay.trusteeship.exception.YeePayOperationException;

@Service("yeePayAutoInvestOperation")
public class YeePayAutoInvestOperation extends
		YeePayOperationServiceAbs<Invest> {
	@Resource
	private HibernateTemplate ht;
	@Resource
	TrusteeshipOperationBO trusteeshipOperationBO;
	@Resource
	private UserBillBO userBillBO;
	@Resource
	private ConfigService configService;
	@Resource
	private LoanService loanService;
	@Resource
	private InvestService investService;
	@Logger
	static Log log;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public TrusteeshipOperation createOperation(Invest invest, FacesContext fc)
			throws IOException {

		StringBuffer content = new StringBuffer();
		content.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		// 商户编号:商户在易宝唯一标识
		content.append("<request platformNo=\""
				+ YeePayConstants.Config.MER_CODE + "\">");
		// 商户平台会员标识:会员在商户平台唯一标识
		content.append("<platformUserNo>" + invest.getUser().getId()
				+ "</platformUserNo>");
		// 请求流水号:接入方必须保证参数内的 requestNo 全局唯一，并且需要保存，留待后续业务使用
		content.append("<requestNo>"+ YeePayConstants.RequestNoPre.AUTO_INVEST + invest.getId() + "</requestNo>");
		// 标的号:标识要自动还款的标的号
		content.append("<orderNo>" + invest.getLoan().getId() + "</orderNo>");
		// 标的金额
		content.append("<transferAmount>" + invest.getMoney()
				+ "</transferAmount>");
		// 目标会员编号
		content.append("<targetPlatformUserNo>"
				+ invest.getLoan().getUser().getId()
				+ "</targetPlatformUserNo>");
		// 冻结金额
		content.append("<paymentAmount>" + invest.getMoney()
				+ "</paymentAmount>");
		// 服务器通知 URL:服务器通知 URL
		content.append("<notifyUrl>"
				+ YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL
				+ YeePayConstants.OperationType.AUTO_INVEST + "</notifyUrl>");
		// 提现金额:如果传入此，将使用该金额提现且用户将不可更改
		content.append("</request>");
		log.debug(content.toString());

		HttpClient client = new HttpClient();
		/* 创建一个post方法 */
		PostMethod postMethod = new PostMethod(
				YeePayConstants.RequestUrl.RequestDirectUrl);
		postMethod.setParameter("req", content.toString());
		String sign = CFCASignUtil.sign(content.toString());
		postMethod.setParameter("sign", sign);
		postMethod.setParameter("service", "AUTO_TRANSFER");
		// /*执行post方法*/
		try {
			int statusCode = client.executeMethod(postMethod);
			if (statusCode != HttpStatus.SC_OK) {
				log.debug("Method failed: " + postMethod.getStatusLine());
			}
			/* 获得返回的结果 */
			byte[] responseBody = postMethod.getResponseBody();
			log.debug(new String(responseBody, "UTF-8"));
			@SuppressWarnings("unchecked")
			Map<String, String> respMap = Dom4jUtil.xmltoMap(new String(
					responseBody, "UTF-8"));

			String code = respMap.get("code");
			String description = respMap.get("description");

			// 保存本地
			TrusteeshipOperation to = new TrusteeshipOperation();
			to.setId(IdGenerator.randomUUID());
			to.setMarkId(invest.getId());
			to.setOperator(invest.getId());
			to.setRequestUrl(YeePayConstants.RequestUrl.RequestDirectUrl);
			to.setRequestData(content.toString());
			to.setStatus(TrusteeshipConstants.Status.UN_SEND);
			to.setType(YeePayConstants.OperationType.AUTO_INVEST);
			to.setTrusteeship("yeepay");
			trusteeshipOperationBO.save(to);
			if ("1".equals(code)) {
				if (invest.getStatus().equals(
						InvestConstants.InvestStatus.WAIT_AFFIRM)) {
					invest.setStatus(InvestConstants.InvestStatus.BID_SUCCESS);
					ht.update(invest);
					to.setStatus(TrusteeshipConstants.Status.PASSED);
					ht.update(to);
				}
			} else {
				if (invest.getStatus().equals(
						InvestConstants.InvestStatus.BID_SUCCESS)) {
					invest.setStatus(InvestConstants.InvestStatus.CANCEL);
					ht.update(invest);
					// 改项目状态，项目如果是等待复核的状态，改为募集中
					if (invest.getLoan().getStatus()
							.equals(LoanConstants.LoanStatus.RECHECK)) {
						invest.getLoan().setStatus(
								LoanConstants.LoanStatus.RAISING);
						ht.update(invest.getLoan());
					}
					// 解冻投资金额
					try {
						userBillBO.unfreezeMoney(invest.getUser().getId(),
								invest.getMoney(), OperatorInfo.CANCEL_INVEST,
								"自动投标：" + invest.getId() + "失败，解冻投资金额");
					} catch (InsufficientBalance e) {
						throw new RuntimeException("自动投标失败，解冻投资金额，冻结金额不足!", e);
					}
					to.setStatus(TrusteeshipConstants.Status.REFUSED);
					ht.update(to);
				}
			}
		} catch (HttpException e) {
			log.debug("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.debug("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			/* Release the connection. */
			postMethod.releaseConnection();
		}
		return null;
	}

	@Transactional(rollbackFor = Exception.class)
	public void receiveOperationPostCallback(ServletRequest request)
			throws TrusteeshipReturnException {
		// TODO Auto-generated method stub

	}

	@Transactional(rollbackFor = Exception.class)
	public void receiveOperationS2SCallback(ServletRequest request,
			ServletResponse response) {

		// 响应的参数 为xml格式
		String notifyXML = request.getParameter("notify");
		// 签名
		String sign = request.getParameter("sign");
		boolean flag = CFCASignUtil.isVerifySign(notifyXML, sign);
		log.debug(notifyXML);
		log.debug(sign);
		log.debug(flag);
		if (flag) {
			if (log.isDebugEnabled()) {
				log.debug("TrustheeshipAutoInvest receiveOperationS2SCallback respXML:"
						+ notifyXML);
			}
			@SuppressWarnings("unchecked")
			Map<String, String> resultMap = Dom4jUtil.xmltoMap(notifyXML);
			String code = resultMap.get("code");
			String message = resultMap.get("message");
			String requestNo = resultMap.get("requestNo").replaceFirst(YeePayConstants.RequestNoPre.AUTO_INVEST, "");

			Invest invest = ht.get(Invest.class, requestNo);
			TrusteeshipOperation to = trusteeshipOperationBO.get(
					YeePayConstants.OperationType.AUTO_INVEST, requestNo,
					requestNo, "yeepay");

			to.setResponseTime(new Date());
			to.setResponseData(notifyXML);
			if ("1".equals(code)) {
				if (invest.getStatus().equals(
						InvestConstants.InvestStatus.WAIT_AFFIRM)) {
					invest.setStatus(InvestConstants.InvestStatus.BID_SUCCESS);
					ht.update(invest);
					to.setStatus(TrusteeshipConstants.Status.PASSED);
					ht.update(to);
				}
			} else {
				if (invest.getStatus().equals(
						InvestConstants.InvestStatus.WAIT_AFFIRM)) {
					invest.setStatus(InvestConstants.InvestStatus.CANCEL);
					ht.update(invest);
					// 改项目状态，项目如果是等待复核的状态，改为募集中
					if (invest.getLoan().getStatus()
							.equals(LoanConstants.LoanStatus.RECHECK)) {
						invest.getLoan().setStatus(
								LoanConstants.LoanStatus.RAISING);
						ht.update(invest.getLoan());
					}
					// 解冻投资金额
					try {
						userBillBO.unfreezeMoney(invest.getUser().getId(),
								invest.getMoney(), "自动投标：" + invest.getId()
										+ "失败，解冻投资金额", "借款ID："
										+ invest.getLoan().getId());
					} catch (InsufficientBalance e) {
						throw new RuntimeException(e);
					}
					to.setStatus(TrusteeshipConstants.Status.REFUSED);
					ht.update(to);
				}
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
