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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.exception.NoMatchingObjectsException;
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
import com.esoft.jdp2p.loan.LoanConstants.LoanStatus;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.loan.service.LoanCalculator;
import com.esoft.jdp2p.loan.service.LoanService;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.exception.TrusteeshipReturnException;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
import com.esoft.yeepay.trusteeship.exception.YeePayOperationException;
import com.esoft.yeepay.trusteeship.service.impl.YeePayOperationServiceAbs;

@Service
public class YeePayWeiXinInvestNofreezeOperation extends
		YeePayOperationServiceAbs<Invest> {
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
	@Resource
	LoanCalculator loanCalculator;
	@Resource
	LoanService loanService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public TrusteeshipOperation createOperation(Invest invest, FacesContext fc)
			throws IOException {

		try {
			String loanId = invest.getLoan().getId();
			invest.setInvestMoney(invest.getMoney());
			// 防止并发出现
			Loan loan = ht.get(Loan.class, loanId);
			ht.evict(loan);
			loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);
			if (!loan.getStatus().equals(LoanStatus.RAISING)) {
				throw new IllegalLoanStatusException(loan.getStatus());
			}

			// 判断项目尚未认购的金额，如果用户想认购的金额大于此金额，则。。。
			double remainMoney = loanCalculator.calculateMoneyNeedRaised(loan
					.getId());

			if (invest.getMoney() > remainMoney) {
				throw new ExceedMoneyNeedRaised();
			}

			// 是否大于用户账户可用余额
			if (invest.getMoney() > ubs.getBalance(invest.getUser().getId())) {
				throw new InsufficientBalance();
			}

			invest.setStatus(InvestConstants.InvestStatus.CANCEL);
			invest.setRate(loan.getRate());
			invest.setTime(new Date());

			// 投资成功以后，判断项目是否有尚未认购的金额，如果没有，则更改项目状态。
			invest.setId(investService.generateId(invest.getLoan().getId()));
			if (invest.getTransferApply() == null
					|| StringUtils.isEmpty(invest.getTransferApply().getId())) {
				invest.setTransferApply(null);
			}
		} catch (InsufficientBalance e) {
			throw new YeePayOperationException("账户余额不足，请充值！", e);
		} catch (ExceedMoneyNeedRaised e) {
			throw new YeePayOperationException("投资金额不能大于尚未募集的金额！", e);
		} catch (IllegalLoanStatusException e) {
			throw new YeePayOperationException("当前借款不可投资", e);
		} catch (NoMatchingObjectsException e) {
			throw new YeePayOperationException("找不到匹配配置", e);
		}
		// invest.setStatus(InvestConstants.InvestStatus.WAIT_AFFIRM);
		invest.setLoan(ht.get(Loan.class, invest.getLoan().getId()));
		invest.setUser(ht.get(User.class, invest.getUser().getId()));
		ht.saveOrUpdate(invest);

		StringBuffer content = new StringBuffer();
		content.append("<?xml version='1.0' encoding='utf-8'?>");
		// 商户编号:商户在易宝唯一标识
		content.append("<request platformNo='"
				+ YeePayConstants.Config.MER_CODE + "'>");
		// 商户平台会员标识:会员在商户平台唯一标识
		content.append("<platformUserNo>" + invest.getUser().getId()
				+ "</platformUserNo>");
		// 请求流水号:接入方必须保证参数内的 requestNo 全局唯一，并且需要保存，留待后续业务使用
		content.append("<requestNo>" + YeePayConstants.RequestNoPre.INVEST
				+ invest.getId() + "</requestNo>");
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
				+ YeePayConstants.ResponseWeiXinWebUrl.PRE_RESPONSE_URL
				+ YeePayConstants.OperationType.INVEST + "</callbackUrl>");
		// 服务器通知 URL:服务器通知 URL
		content.append("<notifyUrl>"
				+ YeePayConstants.ResponseWeiXinS2SUrl.PRE_RESPONSE_URL
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
		to.setRequestUrl(YeePayConstants.RequestUrl.MOBILE_INVEST);
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
			String requestNo = resultMap.get("requestNo").replaceFirst(
					YeePayConstants.RequestNoPre.INVEST, "");
			String code = resultMap.get("code");
			String description = resultMap.get("description");

			Invest invest = ht.get(Invest.class, requestNo);
			Loan loan = invest.getLoan();
			ht.evict(loan);
			loan = ht.get(Loan.class, invest.getLoan().getId(),
					LockMode.UPGRADE);
			TrusteeshipOperation to = trusteeshipOperationBO.get(
					YeePayConstants.OperationType.INVEST, requestNo, requestNo,
					"yeepay");
			to.setResponseData(respXML);
			to.setResponseTime(new Date());
			if("1".equals(code)
					&& TrusteeshipConstants.Status.PASSED
					.equals(to.getStatus())){
				return;
			}
			if ("1".equals(code)
					&& TrusteeshipConstants.Status.SENDED
							.equals(to.getStatus())) {
				double remainMoney = -1;
				try {
					remainMoney = loanCalculator.calculateMoneyNeedRaised(loan
							.getId());
				} catch (NoMatchingObjectsException e) {
				}
				if (invest.getMoney() > remainMoney
						|| !loan.getStatus().equals(LoanStatus.RAISING)) {
					cancel(to, invest);
					if (FacesUtil.getCurrentInstance() != null) {
						if (!loan.getStatus().equals(LoanStatus.RAISING)) {
							FacesUtil.addInfoMessage("项目投标已结束，已流标。");
						} else {
							FacesUtil.addInfoMessage("投资金额超过可投金额，已流标。");
						}
					}
				} else if (invest.getStatus().equals(
						InvestConstants.InvestStatus.WAIT_AFFIRM)
						|| invest.getStatus().equals(
								InvestConstants.InvestStatus.CANCEL)) {
					try {
						success(to, invest, invest.getLoan());
					} catch (NoMatchingObjectsException e) {
						throw new RuntimeException(e);
					} catch (InsufficientBalance e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				if (invest.getStatus().equals(
						InvestConstants.InvestStatus.BID_SUCCESS)
						|| invest.getStatus().equals(
								InvestConstants.InvestStatus.WAIT_AFFIRM)) {
					fail(to);
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
			String requestNo = resultMap.get("requestNo").replaceFirst(
					YeePayConstants.RequestNoPre.INVEST, "");

			Invest invest = ht.get(Invest.class, requestNo);
			Loan loan = invest.getLoan();
			ht.evict(loan);
			loan = ht.get(Loan.class, invest.getLoan().getId(),
					LockMode.UPGRADE);
			TrusteeshipOperation to = trusteeshipOperationBO.get(
					YeePayConstants.OperationType.INVEST, requestNo, requestNo,
					"yeepay");
			to.setResponseTime(new Date());
			to.setResponseData(notifyXML);
			if ("1".equals(code)
					&& TrusteeshipConstants.Status.SENDED
							.equals(to.getStatus())) {
				double remainMoney = -1;
				try {
					remainMoney = loanCalculator.calculateMoneyNeedRaised(loan
							.getId());
				} catch (NoMatchingObjectsException e) {
				}
				if (invest.getMoney() > remainMoney
						|| !loan.getStatus().equals(LoanStatus.RAISING)) {
					cancel(to, invest);
					if (FacesUtil.getCurrentInstance() != null) {
						if (!loan.getStatus().equals(LoanStatus.RAISING)) {
							FacesUtil.addInfoMessage("项目投标已结束，已流标。");
						} else {
							FacesUtil.addInfoMessage("投资金额超过可投金额，已流标。");
						}
					}
				} else if (invest.getStatus().equals(
						InvestConstants.InvestStatus.WAIT_AFFIRM)
						|| invest.getStatus().equals(
								InvestConstants.InvestStatus.CANCEL)) {
					try {
						success(to, invest, invest.getLoan());
					} catch (NoMatchingObjectsException e) {
						throw new RuntimeException(e);
					} catch (InsufficientBalance e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				if (invest.getStatus().equals(
						InvestConstants.InvestStatus.BID_SUCCESS)
						|| invest.getStatus().equals(
								InvestConstants.InvestStatus.WAIT_AFFIRM)
						|| invest.getStatus().equals(
								InvestConstants.InvestStatus.CANCEL)) {
					fail(to);
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

	/** 成功 */
	public void success(TrusteeshipOperation to, Invest invest, Loan loan)
			throws NoMatchingObjectsException, InsufficientBalance {
		/**caijinmin 修改一次投标记录两次交易记录的问题   20150421 begin*/
		if (invest.getStatus().equals(
				InvestConstants.InvestStatus.WAIT_AFFIRM)
				|| invest.getStatus().equals(
						InvestConstants.InvestStatus.CANCEL)) {
		/**caijinmin 修改一次投标记录两次交易记录的问题   20150421 end*/
			invest.setStatus(InvestConstants.InvestStatus.BID_SUCCESS);
			to.setStatus(TrusteeshipConstants.Status.PASSED);
			loanService.dealRaiseComplete(loan.getId());
			ubs.freezeMoney(
					invest.getUser().getId(),
					invest.getMoney(),
					OperatorInfo.INVEST_SUCCESS,
					"投资成功：冻结金额。借款ID:" + loan.getId() + "  投资id:"
							+ invest.getId());
			ht.update(to);
			ht.update(invest);
		}
	}

	/** 调用流标接口 */
	public void cancel(TrusteeshipOperation to, Invest invest) {
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
		content.append("<requestNo>" + YeePayConstants.RequestNoPre.INVEST
				+ invest.getId() + "</requestNo>");
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
			if (code.equals("1"))// 如果取消投标成功，执行业务逻辑。
			{
				fail(to);
			}
		} catch (HttpException e) {
			log.error("Fatal protocol violation: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			log.error("Fatal transport error: " + e.getMessage());
			throw new RuntimeException(e);
		} finally {
			postMethod.releaseConnection();
		}
	}

	public void fail(TrusteeshipOperation to) {
		Invest invest = ht.get(Invest.class, to.getMarkId(), LockMode.UPGRADE);
		if (invest.getStatus().equals(InvestConstants.InvestStatus.WAIT_AFFIRM)) {
			invest.setStatus(InvestConstants.InvestStatus.CANCEL);
			ht.update(invest);
		}
		to.setStatus(TrusteeshipConstants.Status.REFUSED);
		ht.update(to);
	}
}
