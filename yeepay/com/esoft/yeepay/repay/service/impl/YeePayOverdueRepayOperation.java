package com.esoft.yeepay.repay.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.annotations.Logger;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.ArithUtil;
import com.esoft.core.util.Dom4jUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.invest.InvestConstants.TransferStatus;
import com.esoft.jdp2p.invest.model.TransferApply;
import com.esoft.jdp2p.invest.service.TransferService;
import com.esoft.jdp2p.loan.LoanConstants;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.repay.exception.OverdueRepayException;
import com.esoft.jdp2p.repay.model.InvestRepay;
import com.esoft.jdp2p.repay.model.LoanRepay;
import com.esoft.jdp2p.repay.service.RepayService;
import com.esoft.jdp2p.risk.service.SystemBillService;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.exception.TrusteeshipReturnException;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
import com.esoft.yeepay.trusteeship.exception.YeePayOperationException;
import com.esoft.yeepay.trusteeship.service.impl.YeePayOperationServiceAbs;

@Service("yeePayOverdueRepayOperation")
public class YeePayOverdueRepayOperation extends
		YeePayOperationServiceAbs<LoanRepay> {

	@Logger
	static Log log;

	@Resource
	HibernateTemplate ht;

	@Resource
	UserBillBO userBillBO;

	@Resource
	SystemBillService systemBillService;

	@Resource
	TrusteeshipOperationBO trusteeshipOperationBO;

	@Resource
	TransferService transferService;

	@Resource
	RepayService repayService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public TrusteeshipOperation createOperation(LoanRepay loanRepay,
			FacesContext fc) throws IOException {
		// FIXME:验证
		LoanRepay repay = ht.get(LoanRepay.class, loanRepay.getId());
		if (!repay.getStatus().equals(LoanConstants.RepayStatus.OVERDUE)
				&& !repay.getStatus()
						.equals(LoanConstants.RepayStatus.BAD_DEBT)) {
			throw new YeePayOperationException("还款不处于逾期还款状态！");
		}
		List<InvestRepay> irs = ht
				.find("from InvestRepay ir where ir.invest.loan.id=? and ir.period=?",
						new Object[] { repay.getLoan().getId(),
								repay.getPeriod() });

		// 取消投资下面的所有正在转让的债权
		String hql = "from TransferApply ta where ta.invest.loan.id=? and ta.status=?";
		List<TransferApply> tas = ht.find(hql, new String[] {
				repay.getLoan().getId(), TransferStatus.WAITCONFIRM });
		if (tas.size() > 0) {
			// 有购买了等待第三方确认的债权，所以不能还款。
			throw new YeePayOperationException("有等待第三方确认的债权转让，不能还款！");
		}
		tas = ht.find(hql, new String[] { repay.getLoan().getId(),
				TransferStatus.TRANSFERING });
		for (TransferApply ta : tas) {
			transferService.cancel(ta.getId());
		}

		// 判断标志 还款金额是否有小于1的投资
		DecimalFormat currentNumberFormat = new DecimalFormat("#0.00");

		// 拼接xml格式数据
		StringBuffer content = new StringBuffer();
		content.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
		// 商户编号:商户在易宝唯一标识
		content.append("<request platformNo='"
				+ YeePayConstants.Config.MER_CODE + "'>");
		// 商户平台会员标识:会员在商户平台唯一标识
		content.append("<platformUserNo>" + repay.getLoan().getUser().getId()
				+ "</platformUserNo>");
		// 请求流水号:接入方必须保证参数内的 requestNo 全局唯一，并且需要保存，留待后续业务使用
		content.append("<requestNo>"+YeePayConstants.RequestNoPre.OVERDUE_REPAY + repay.getId() + "</requestNo>");
		// 标的号:标识一笔要自动还款的标的的标的号
		content.append("<orderNo>" + repay.getLoan().getId() + "</orderNo>");
		content.append("<repayments>");
		double irRepayMoney = 0D;
		double irFee = 0D;

		// FIXME:给系统的罚金，没有计算。需要把罚金转给系统
		double defaultInterest = repay.getDefaultInterest();
		for (int i = 0; i < irs.size(); i++) {
			InvestRepay ir = irs.get(i);
			defaultInterest = ArithUtil.sub(defaultInterest,
					ir.getDefaultInterest());
			// 借款人还款手续费
			if (i == irs.size()-1) {
				irRepayMoney = ArithUtil.add(ir.getCorpus(), ir.getInterest(),
						ir.getDefaultInterest(), repay.getFee(),defaultInterest);
				irFee = ArithUtil.add(ir.getFee(), repay.getFee(),
						defaultInterest);
			} else {
				irRepayMoney = ArithUtil.add(ir.getCorpus(), ir.getInterest(),
						ir.getDefaultInterest());
				irFee = ir.getFee();
			}
			content.append("<repayment>");
			// 转账请求流水号
			if(ir.getInvest().getId().length() == 14){
			/**caijinmin 有债权转让的还款添加14前缀 20150502 end*/
			Long count = (Long) ht.find("select count(to) from TrusteeshipOperation to where to.type=? and to.markId=? and to.requestData like ?", YeePayConstants.OperationType.INVEST, ir.getInvest().getId(), "%<requestNo>03"+ir.getInvest().getId()+"</requestNo>%").get(0);
			Long count1 = (Long) ht.find("select count(to) from TrusteeshipOperation to where to.type=? and to.markId=? and to.requestData like ?", YeePayConstants.OperationType.AUTO_INVEST,ir.getInvest().getId(), "%<requestNo>13"+ir.getInvest().getId()+"</requestNo>%").get(0);
			if (count == 0) {
				if(count1 == 0){
					//未添加流水号前缀
					content.append("<paymentRequestNo>" + ir.getInvest().getId()
							+ "</paymentRequestNo>");
				}else{
					//自动投标
					content.append("<paymentRequestNo>" + YeePayConstants.RequestNoPre.AUTO_INVEST+ir.getInvest().getId()
							+ "</paymentRequestNo>");
				}
				
			} else if (count == 1){
				content.append("<paymentRequestNo>" + YeePayConstants.RequestNoPre.INVEST+ir.getInvest().getId()
						+ "</paymentRequestNo>");
			}
			}else{
				content.append("<paymentRequestNo>" + YeePayConstants.RequestNoPre.TRANSFER+ir.getInvest().getId()
						+ "</paymentRequestNo>");
			}
		/**caijinmin 有债权转让的还款添加14前缀 20150502 begin*/
			// 投资人会员编号
			content.append("<targetUserNo>" + ir.getInvest().getUser().getId()
					+ "</targetUserNo>");
			// 还款金额
			content.append("<amount>"
					+ currentNumberFormat.format(irRepayMoney) + "</amount>");
			// 还款平台提成
			content.append("<fee>" + currentNumberFormat.format(irFee)
					+ "</fee>");
			content.append("</repayment>");
		}
		content.append("</repayments>");
		content.append("<callbackUrl>"
				+ YeePayConstants.ResponseWebUrl.PRE_RESPONSE_URL
				+ YeePayConstants.OperationType.OVERDUE_REPAY
				+ "</callbackUrl>");
		// 服务器通知 URL
		content.append("<notifyUrl>"
				+ YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL
				+ YeePayConstants.OperationType.OVERDUE_REPAY + "</notifyUrl>");
		content.append("</request>");
		log.debug(content.toString());
		// 包装参数
		Map<String, String> params = new HashMap<String, String>();
		params.put("req", content.toString());
		String sign = CFCASignUtil.sign(content.toString());
		params.put("sign", sign);

		TrusteeshipOperation to = new TrusteeshipOperation();
		to.setId(IdGenerator.randomUUID());
		to.setMarkId(repay.getId());
		to.setOperator(repay.getId());
		to.setRequestUrl(YeePayConstants.RequestUrl.REPAY);
		to.setRequestData(MapUtil.mapToString(params));
		to.setStatus(TrusteeshipConstants.Status.UN_SEND);
		to.setType(YeePayConstants.OperationType.OVERDUE_REPAY);
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
			throw new YeePayOperationException(e.getMessage(), e);
		}
		// 响应的参数 为xml格式
		String respXML = request.getParameter("resp");
		log.debug(respXML.toString());
		// 签名
		String sign = request.getParameter("sign");
		boolean flag = CFCASignUtil.isVerifySign(respXML, sign);
		if (flag) {
			@SuppressWarnings("unchecked")
			Map<String, String> resultMap = Dom4jUtil.xmltoMap(respXML);
			String code = resultMap.get("code");
			String requestNo = resultMap.get("requestNo").replaceFirst(YeePayConstants.RequestNoPre.OVERDUE_REPAY, "");
			log.debug(respXML);
			LoanRepay repay = ht.get(LoanRepay.class, requestNo);
			TrusteeshipOperation to = trusteeshipOperationBO.get(
					YeePayConstants.OperationType.OVERDUE_REPAY, requestNo,
					requestNo, "yeepay");
			to.setResponseTime(new Date());
			if ("1".equals(code)) {
				try {
					log.debug("repayservice.overdueRepay(repayID" + requestNo
							+ ")");
					/**caijinmin 修改判断条件 201503021749 begin*/
					if (repay.getStatus().equals(
							LoanConstants.RepayStatus.OVERDUE)) {
						repayService.overdueRepay(requestNo);
					}
					/**caijinmin 修改判断条件 201503021749 end*/
				} catch (InsufficientBalance e) {
					throw new YeePayOperationException("用户余额不足!", e);
				} catch (OverdueRepayException e) {
					throw new YeePayOperationException("还款不处于逾期还款状态", e);
				}
				to.setStatus(TrusteeshipConstants.Status.PASSED);
				to.setResponseData(respXML);
				to.setResponseTime(new Date());
				ht.update(to);
			} else {
				to.setStatus(TrusteeshipConstants.Status.REFUSED);
				to.setResponseData(respXML);
				to.setResponseTime(new Date());
				ht.update(to);
			}
		}
		log.debug("respXML:" + respXML + "sign:" + sign + "flag" + flag);

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void receiveOperationS2SCallback(ServletRequest request,
			ServletResponse response) {

		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new YeePayOperationException(e.getMessage(), e);
		}

		// 响应的参数 为xml格式
		String notifyXML = request.getParameter("notify");
		// 签名
		String sign = request.getParameter("sign");
		boolean flag = CFCASignUtil.isVerifySign(notifyXML, sign);
		log.debug("notifyXML:" + notifyXML + "sign:" + sign + "flag" + flag);
		if (flag) {
			@SuppressWarnings("unchecked")
			Map<String, String> resultMap = Dom4jUtil.xmltoMap(notifyXML);
			String code = resultMap.get("code");
			String requestNo = resultMap.get("requestNo").replaceFirst(YeePayConstants.RequestNoPre.OVERDUE_REPAY, "");
			log.debug(notifyXML);
			LoanRepay repay = ht.get(LoanRepay.class, requestNo);
			TrusteeshipOperation to = trusteeshipOperationBO.get(
					YeePayConstants.OperationType.OVERDUE_REPAY, requestNo,
					requestNo, "yeepay");
			to.setResponseTime(new Date());

			if ("1".equals(code)) {
				try {
					log.debug("repayservice.overdueRepay(repayID" + requestNo
							+ ")");
					/**caijinmin 修改判断条件 201502271426 begin*/
					if (repay.getStatus().equals(
							LoanConstants.RepayStatus.OVERDUE)) {
						repayService.overdueRepay(requestNo);
					}
					/**caijinmin 修改判断条件 201502271426 end*/
				} catch (InsufficientBalance e) {
					throw new YeePayOperationException("用户余额不足！", e);
				} catch (OverdueRepayException e) {
					throw new YeePayOperationException("还款不处于逾期还款状态！", e);
				}
				to.setStatus(TrusteeshipConstants.Status.PASSED);
				to.setResponseData(notifyXML);
				to.setResponseTime(new Date());
				ht.update(to);
			} else {
				to.setStatus(TrusteeshipConstants.Status.REFUSED);
				to.setResponseData(notifyXML);
				to.setResponseTime(new Date());
				ht.update(to);
			}
		}
		try {
			response.getWriter().print("SUCCESS");
			FacesUtil.getCurrentInstance().responseComplete();
		} catch (IOException e) {
			throw new YeePayOperationException("网络连接不通畅！", e);
		}

	}

}
