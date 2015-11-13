package com.esoft.yeepay.invest.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.user.UserBillConstants.OperatorInfo;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.annotations.Logger;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.Dom4jUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.coupon.exception.ExceedDeadlineException;
import com.esoft.jdp2p.coupon.exception.UnreachedMoneyLimitException;
import com.esoft.jdp2p.invest.InvestConstants;
import com.esoft.jdp2p.invest.exception.ExceedMaxAcceptableRate;
import com.esoft.jdp2p.invest.exception.ExceedMoneyNeedRaised;
import com.esoft.jdp2p.invest.exception.IllegalLoanStatusException;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.invest.service.InvestService;
import com.esoft.jdp2p.loan.LoanConstants;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.exception.TrusteeshipReturnException;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
import com.esoft.yeepay.trusteeship.exception.YeePayOperationException;
import com.esoft.yeepay.trusteeship.service.impl.YeePayOperationServiceAbs;

@Service("yeePayInvestOperation")
public class YeePayInvestOperation extends YeePayOperationServiceAbs<Invest> {
	@Resource
	InvestService investService;
	@Resource
	HibernateTemplate ht;
	@Resource
	TrusteeshipOperationBO trusteeshipOperationBO;
	@Resource
	UserBillBO ubs;
	@Logger
	static Log log;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public TrusteeshipOperation createOperation(Invest invest, FacesContext fc)
			throws IOException {

		try {
			investService.create(invest);
		} catch (InsufficientBalance e) {
			throw new YeePayOperationException("账户余额不足，请充值！", e);
		} catch (ExceedMoneyNeedRaised e) {
			throw new YeePayOperationException("投资金额不能大于尚未募集的金额！", e);
		} catch (ExceedMaxAcceptableRate e) {
			throw new YeePayOperationException("竞标利率不能大于借款者可接受的最高利率！", e);
		} catch (ExceedDeadlineException e) {
			throw new YeePayOperationException("优惠券超过有效期！", e);
		} catch (UnreachedMoneyLimitException e) {
			throw new YeePayOperationException("金额未达到优惠券使用条件！", e);
		} catch (IllegalLoanStatusException e) {
			throw new YeePayOperationException("当前借款不可投资", e);
		}
		invest.setStatus(InvestConstants.InvestStatus.WAIT_AFFIRM);
		ht.saveOrUpdate(invest);
		invest.setLoan(ht.get(Loan.class, invest.getLoan().getId()));
		invest.setUser(ht.get(User.class, invest.getUser().getId()));

		StringBuffer content = new StringBuffer();
		content.append("<?xml version='1.0' encoding='utf-8'?>");
		// 商户编号:商户在易宝唯一标识
		content.append("<request platformNo='"
				+ YeePayConstants.Config.MER_CODE + "'>");
		// 商户平台会员标识:会员在商户平台唯一标识
		content.append("<platformUserNo>" + invest.getUser().getId()
				+ "</platformUserNo>");
		// 请求流水号:接入方必须保证参数内的 requestNo 全局唯一，并且需要保存，留待后续业务使用
		content.append("<requestNo>"+ YeePayConstants.RequestNoPre.INVEST + invest.getId() + "</requestNo>");
		// 标的号:标识要自动还款的标的号
		content.append("<orderNo>" + invest.getLoan().getId() + "</orderNo>");
		// 标的金额
		content.append("<transferAmount>" + invest.getLoan().getLoanMoney()
				+ "</transferAmount>");
		// 目标会员编号
		content.append("<targetPlatformUserNo>"
				+ invest.getLoan().getUser().getId()
				+ "</targetPlatformUserNo>");
		// 冻结金额
		content.append("<paymentAmount>" + invest.getMoney()
				+ "</paymentAmount>");
		Date date = DateUtil.addMinute(new Date(), 10);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		content.append("<expired>" + sdf.format(date) + "</expired>");
		// 页面回跳URL
		content.append("<callbackUrl>"
				+ YeePayConstants.ResponseWebUrl.PRE_RESPONSE_URL
				+ YeePayConstants.OperationType.INVEST + "</callbackUrl>");
		// 服务器通知 URL:服务器通知 URL
		content.append("<notifyUrl>"
				+ YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL
				+ YeePayConstants.OperationType.INVEST + "</notifyUrl>");
		// 提现金额:如果传入此，将使用该金额提现且用户将不可更改
		content.append("</request>");

		// 包装参数
		Map<String, String> params = new HashMap<String, String>();
		params.put("req", content.toString());
		String sign = CFCASignUtil.sign(content.toString());
		params.put("sign", sign);

		// 保存本地
		TrusteeshipOperation to = new TrusteeshipOperation();
		to.setId(IdGenerator.randomUUID());
		to.setMarkId(invest.getId());
		to.setOperator(invest.getId());
		if (FacesUtil.isMobileRequest((HttpServletRequest) fc.getExternalContext().getRequest())) {
			to.setRequestUrl(YeePayConstants.RequestUrl.MOBILE_INVEST);
		} else {
			to.setRequestUrl(YeePayConstants.RequestUrl.INVEST);
		}
		to.setRequestData(MapUtil.mapToString(params));
		to.setStatus(TrusteeshipConstants.Status.UN_SEND);
		to.setType(YeePayConstants.OperationType.INVEST);
		to.setTrusteeship("yeepay");
		trusteeshipOperationBO.save(to);
		try {
			sendOperation(to.getId(), "utf-8", fc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void receiveOperationPostCallback(ServletRequest request)
			throws TrusteeshipReturnException {
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		// 响应的参数 为xml格式
		String respXML = request.getParameter("resp");
		// 签名
		String sign = request.getParameter("sign");
		boolean flag = CFCASignUtil.isVerifySign(respXML, sign);
		log.debug(respXML);
		log.debug(sign);
		log.debug(flag);
		if (flag) {
			// 处理响应
			@SuppressWarnings("unchecked")
			Map<String, String> resultMap = Dom4jUtil.xmltoMap(respXML);
			// 商户编号
			// 请求流水号:注册不传入请求流水号，返回无流水号,为投资id
			String requestNo = resultMap.get("requestNo").replaceFirst(YeePayConstants.RequestNoPre.INVEST, "");
			String code = resultMap.get("code");
			String description = resultMap.get("description");

			Invest invest = ht.get(Invest.class, requestNo);
			TrusteeshipOperation to = trusteeshipOperationBO.get(
					YeePayConstants.OperationType.INVEST, requestNo, requestNo,
					"yeepay");

			to.setResponseData(respXML);
			to.setResponseTime(new Date());
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
						InvestConstants.InvestStatus.BID_SUCCESS)
						|| invest.getStatus().equals(
								InvestConstants.InvestStatus.WAIT_AFFIRM)) {
					invest.setStatus(InvestConstants.InvestStatus.CANCEL);
					ht.update(invest);
					// 改项目状态，项目如果是等待复核的状态，改为募集中
					if (invest.getLoan().getStatus()
							.equals(LoanConstants.LoanStatus.RECHECK) && invest.getLoan().getExpectTime().after(new Date())) {
						invest.getLoan().setStatus(
								LoanConstants.LoanStatus.RAISING);
						ht.update(invest.getLoan());
					}
					// 解冻投资金额
					try {
						ubs.unfreezeMoney(invest.getUser().getId(),
								invest.getMoney(), OperatorInfo.CANCEL_INVEST,
								"投资：" + invest.getId() + "失败，解冻投资金额, 借款ID："
										+ invest.getLoan().getId());
					} catch (InsufficientBalance e) {
						throw new RuntimeException(e);
					}
					to.setStatus(TrusteeshipConstants.Status.REFUSED);
					ht.update(to);
				}
				// 真实错误原因
				throw new TrusteeshipReturnException(description);
				// FacesUtil.addErrorMessage(description);

			}
		}

	}

	@Override
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
				log.debug("TrustheeshipInvest receiveOperationS2SCallback respXML:"
						+ notifyXML);
			}
			@SuppressWarnings("unchecked")
			Map<String, String> resultMap = Dom4jUtil.xmltoMap(notifyXML);
			String code = resultMap.get("code");
			String message = resultMap.get("message");
			String requestNo = resultMap.get("requestNo").replaceFirst(YeePayConstants.RequestNoPre.INVEST, "");

			Invest invest = ht.get(Invest.class, requestNo);
			TrusteeshipOperation to = trusteeshipOperationBO.get(
					YeePayConstants.OperationType.INVEST, requestNo, requestNo,
					"yeepay");

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
						ubs.unfreezeMoney(invest.getUser().getId(),
								invest.getMoney(), OperatorInfo.CANCEL_INVEST,
								"投资：" + invest.getId() + "失败，解冻投资金额, 借款ID："
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
