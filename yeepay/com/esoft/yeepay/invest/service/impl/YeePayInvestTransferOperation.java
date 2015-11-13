package com.esoft.yeepay.invest.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.user.UserBillConstants.OperatorInfo;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.annotations.Logger;
import com.esoft.core.util.ArithUtil;
import com.esoft.core.util.Dom4jUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.invest.InvestConstants;
import com.esoft.jdp2p.invest.InvestConstants.InvestStatus;
import com.esoft.jdp2p.invest.InvestConstants.TransferStatus;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.invest.model.TransferApply;
import com.esoft.jdp2p.invest.service.TransferService;
import com.esoft.jdp2p.loan.LoanConstants.RepayStatus;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.repay.model.InvestRepay;
import com.esoft.jdp2p.risk.FeeConfigConstants.FeePoint;
import com.esoft.jdp2p.risk.FeeConfigConstants.FeeType;
import com.esoft.jdp2p.risk.service.impl.FeeConfigBO;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.exception.TrusteeshipReturnException;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
import com.esoft.yeepay.trusteeship.exception.YeePayOperationException;
import com.esoft.yeepay.trusteeship.service.impl.YeePayOperationServiceAbs;

@Service("yeePayInvestTransferOperation")
public class YeePayInvestTransferOperation extends
		YeePayOperationServiceAbs<Invest> {
	@Resource
	TransferService transferService;
	@Resource
	HibernateTemplate ht;
	@Resource
	UserBillBO userBillBO;
	@Resource
	TrusteeshipOperationBO trusteeshipOperationBO;
	@Resource
	FeeConfigBO feeConfigBO;
	@Logger
	static Log log;

	@Override
	@Transactional(rollbackFor=Exception.class)
	public TrusteeshipOperation createOperation(Invest invest, FacesContext fc)
			throws IOException {
		String transferApplyId = invest.getTransferApply().getId();
		String userId = invest.getUser().getId();
		double transferCorpus = invest.getInvestMoney();
		// 购买债权，就是一笔投资啊，不过得考虑部分购买。
		double remainCorpus = transferService
				.calculateRemainCorpus(transferApplyId);
		// 出价必须大于0，小于可购买的金额
		if (transferCorpus <= 0 || transferCorpus > remainCorpus) {
			throw new YeePayOperationException("购买本金必须小于等于" + remainCorpus
					+ "且大于0");
		}
		TransferApply ta = ht.get(TransferApply.class, transferApplyId);
		// 购买的本金占剩余本金的比例。
		double corpusRate = ArithUtil.div(transferCorpus, remainCorpus);
		// 购买的本金占所有转让本金的比例。
	    double corpusRateInAll = ArithUtil.div(transferCorpus, ta.getCorpus());
		// 债权的购买金额：债权的价格*corpusRate
		double buyPrice = ArithUtil.mul(ta.getPrice(), corpusRateInAll, 2);
		
		Invest investNew = new Invest();
		investNew.setId(IdGenerator.randomUUID());
		investNew.setMoney(transferCorpus);
		investNew.setStatus(InvestConstants.InvestStatus.WAIT_AFFIRM);
		investNew.setRate(ta.getInvest().getRate());
		investNew.setTransferApply(ta);
		investNew.setLoan(ta.getInvest().getLoan());
		investNew.setTime(new Date());
		investNew.setUser(new User(userId));
		investNew.setInvestMoney(transferCorpus);
		//在这里添加投标状态是手动投标，
		investNew.setIsAutoInvest(false);
		ta.setStatus(InvestConstants.TransferStatus.WAITCONFIRM);
		ht.save(investNew);
		ht.update(ta);
		try {
			userBillBO.freezeMoney(userId, buyPrice, OperatorInfo.TRANSFER,"购买债权，冻结金额，债权ID："
					+ investNew.getId());
		} catch (InsufficientBalance e1) {
			throw new YeePayOperationException("冻结金额失败，余额不足！请充值！");
		}

		// 购买时候，扣除手续费，从转让人收到的金额中扣除。费用根据购买价格计算
		StringBuffer content = new StringBuffer();
		content.append("<?xml version='1.0' encoding='utf-8'?>");
		// 商户编号:商户在易宝唯一标识
		content.append("<request platformNo='"
				+ YeePayConstants.Config.MER_CODE + "'>");
		// 商户平台会员标识:会员在商户平台唯一标识
		content.append("<platformUserNo>" + userId + "</platformUserNo>");
		content.append("<requestNo>"+ YeePayConstants.RequestNoPre.TRANSFER + investNew.getId() + "</requestNo>");
		content.append("<orderNo>" + investNew.getLoan().getId() + "</orderNo>");
		Long count = (Long) ht.find("select count(to) from TrusteeshipOperation to where to.type=? and to.markId=? and to.requestData like ?", YeePayConstants.OperationType.INVEST, ta.getInvest().getId(), "%<requestNo>03"+ta.getInvest().getId()+"</requestNo>%").get(0);
		Long count1 = (Long) ht.find("select count(to) from TrusteeshipOperation to where to.type=? and to.markId=? and to.requestData like ?", YeePayConstants.OperationType.AUTO_INVEST, ta.getInvest().getId(), "%<requestNo>13"+ta.getInvest().getId()+"</requestNo>%").get(0);
		if (count == 0) {
			if(count1==0){
				//未添加流水号前缀
				content.append("<paymentRequestNo>" + ta.getInvest().getId()
						+ "</paymentRequestNo>");
			}else if(count1==1){
				content.append("<paymentRequestNo>" + YeePayConstants.RequestNoPre.AUTO_INVEST+ta.getInvest().getId()
						+ "</paymentRequestNo>");
			}
		} else if (count == 1){
			content.append("<paymentRequestNo>" + YeePayConstants.RequestNoPre.INVEST+ta.getInvest().getId()
					+ "</paymentRequestNo>");
		}
		content.append("<targetUserNo>" + ta.getInvest().getUser().getId()
				+ "</targetUserNo>");
		content.append("<amount>" + buyPrice + "</amount>");
		// 页面回跳URL
		content.append("<callbackUrl>"
				+ YeePayConstants.ResponseWebUrl.PRE_RESPONSE_URL
				+ YeePayConstants.OperationType.TRANSFER + "</callbackUrl>");
		// 服务器通知 URL:服务器通知 URL
		content.append("<notifyUrl>"
				+ YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL
				+ YeePayConstants.OperationType.TRANSFER + "</notifyUrl>");
		// 提现金额:如果传入此，将使用该金额提现且用户将不可更改
		content.append("</request>");

		Map<String, String> params = new HashMap<String, String>();
		params.put("req", content.toString());
		String sign = CFCASignUtil.sign(content.toString());
		params.put("sign", sign);

		// 保存本地
		TrusteeshipOperation to = new TrusteeshipOperation();
		to.setId(IdGenerator.randomUUID());
		to.setMarkId(investNew.getId());
		to.setOperator(Double.toString(buyPrice)+"&"+Double.toString(corpusRate)+"&"+userId);
		to.setRequestUrl(YeePayConstants.RequestUrl.TRANSFER);
		to.setRequestData(MapUtil.mapToString(params));
		to.setStatus(TrusteeshipConstants.Status.UN_SEND);
		to.setType(YeePayConstants.OperationType.TRANSFER);
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
		log.debug(respXML.toString());
		// 签名
		String sign = request.getParameter("sign");
		boolean flag = CFCASignUtil.isVerifySign(respXML, sign);
		if (flag) {
			// 处理账户开通成功
			@SuppressWarnings("unchecked")
			Map<String, String> resultMap = Dom4jUtil.xmltoMap(respXML);
			String platformUserNo = resultMap.get("platformUserNo");
			String requestNo = resultMap.get("requestNo").replaceFirst(YeePayConstants.RequestNoPre.TRANSFER, "");
			// 返回码
			String code = resultMap.get("code");
			String description = resultMap.get("description");
			TrusteeshipOperation to = trusteeshipOperationBO
					.get(YeePayConstants.OperationType.TRANSFER, requestNo,
							"yeepay");
			 Invest invest = ht.get(Invest.class, requestNo);
			 TransferApply ta = ht.get(TransferApply.class, invest
						.getTransferApply().getId());
			 ht.evict(invest);
			 ht.evict(ta);
			 invest = ht.get(Invest.class, requestNo,LockMode.UPGRADE);
			 ta = ht.get(TransferApply.class, invest
						.getTransferApply().getId(),LockMode.UPGRADE);
			String salor = invest.getTransferApply().getInvest().getUser()
					.getId();
			String[] params = to.getOperator().split("&");
			to.setResponseTime(new Date());
			to.setResponseData(respXML);
			double remainCorpus = transferService.calculateRemainCorpus(ta
					.getId());
			double buyPrice =Double.parseDouble(params[0]) ;
			// 购买的本金占剩余本金的比例。
			double corpusRate =Double.parseDouble(params[1]) ;
			if ("1".equals(code)) {
				if (invest != null && ta != null) {
					invest.setStatus(InvestConstants.InvestStatus.REPAYING);
					// 成交时间
					invest.setTime(new Date());
					// 减去invest中持有的本金
					ta.getInvest().setMoney(
							ArithUtil.sub(ta.getInvest().getMoney(),
									invest.getMoney()));
					if (ta.getInvest().getMoney() == 0) {
						// 投资全部被转让，则投资状态变为“完成”。
						ta.getInvest().setStatus(InvestStatus.COMPLETE);
					}
					// 判断ta是否都被购买了
					if (remainCorpus == 0) {
						// 债权全部被购买，债权转让完成
						ta.setStatus(TransferStatus.TRANSFED);
					} else {
						ta.setStatus(TransferStatus.TRANSFERING);
					}
					try {
						userBillBO.transferOutFromFrozen(invest.getUser().getId(),
								buyPrice,OperatorInfo.TRANSFER, "债权：" + invest.getId()
										+ "购买成功");
						userBillBO.transferIntoBalance(salor,
								buyPrice,OperatorInfo.TRANSFER, "债权：" + invest.getId()
										+ "转让成功");
					} catch (InsufficientBalance e) {
						throw new TrusteeshipReturnException("划款时出错！");
					}

					ht.update(invest);
					ht.update(ta);
					// 生成购买债权后的还款数据，调整之前的还款数据
					generateTransferRepay(ta.getInvest().getInvestRepays(),
							invest, corpusRate);
				}

				to.setStatus(TrusteeshipConstants.Status.PASSED);
				ht.update(to);
			} else {
				boolean unfreezeError = false;
				if (invest != null && ta != null) {
					invest.setStatus(InvestConstants.InvestStatus.CANCEL);
					ta.setStatus(TransferStatus.TRANSFERING);
					try {
						userBillBO.unfreezeMoney(invest.getUser().getId(),
								buyPrice,OperatorInfo.TRANSFER, "债权：" + invest.getId()
										+ "购买失败");
					} catch (InsufficientBalance e) {
						unfreezeError = true;
					}
					ht.update(invest);
					ht.update(ta);
				}
				to.setStatus(TrusteeshipConstants.Status.REFUSED);
				ht.update(to);
				if ("0".equals(code)) {
					throw new TrusteeshipReturnException(description);
				}
				if (unfreezeError) {
					throw new TrusteeshipReturnException("购买债权："
							+ invest.getId() + "失败，解冻金额时出错！");
				}
				// 真实错误原因
				throw new TrusteeshipReturnException(code + ":" + description);
			}
		}
	}

	@Override
	@Transactional( rollbackFor = Exception.class)
	public void receiveOperationS2SCallback(ServletRequest request,
			ServletResponse response) {
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
			String platformUserNo = resultMap.get("platformUserNo");
			String requestNo = resultMap.get("requestNo").replaceFirst(YeePayConstants.RequestNoPre.TRANSFER, "");
			// 返回码
			String code = resultMap.get("code");
			String description = resultMap.get("description");
			TrusteeshipOperation to = trusteeshipOperationBO
					.get(YeePayConstants.OperationType.TRANSFER, requestNo,
							"yeepay");
			 Invest invest = ht.get(Invest.class, requestNo);
			 TransferApply ta = ht.get(TransferApply.class, invest
						.getTransferApply().getId());
			 ht.evict(invest);
			 ht.evict(ta);
			 invest = ht.get(Invest.class, requestNo,LockMode.UPGRADE);
			 ta = ht.get(TransferApply.class, invest
						.getTransferApply().getId(),LockMode.UPGRADE);
			String salor = invest.getTransferApply().getInvest().getUser()
					.getId();
			String[] params = to.getOperator().split("&");
			to.setResponseTime(new Date());
			to.setResponseData(respXML);
			double remainCorpus = transferService.calculateRemainCorpus(ta
					.getId());
			double buyPrice =Double.parseDouble(params[0]) ;
			// 购买的本金占剩余本金的比例。
			double corpusRate =Double.parseDouble(params[1]) ;
			if ("1".equals(code)) {
				if (invest != null && ta != null) {
					invest.setStatus(InvestConstants.InvestStatus.REPAYING);
					// 成交时间
					invest.setTime(new Date());
					// 减去invest中持有的本金
					ta.getInvest().setMoney(
							ArithUtil.sub(ta.getInvest().getMoney(),
									invest.getMoney()));
					if (ta.getInvest().getMoney() == 0) {
						// 投资全部被转让，则投资状态变为“完成”。
						ta.getInvest().setStatus(InvestStatus.COMPLETE);
					}
					// 判断ta是否都被购买了
					if (remainCorpus == 0) {
						// 债权全部被购买，债权转让完成
						ta.setStatus(TransferStatus.TRANSFED);
					} else {
						ta.setStatus(TransferStatus.TRANSFERING);
					}

					try {
						userBillBO.transferOutFromFrozen(invest.getUser().getId(),
								buyPrice,OperatorInfo.TRANSFER, "债权：" + invest.getId()
										+ "购买成功");
						userBillBO.transferIntoBalance(salor,
								buyPrice,OperatorInfo.TRANSFER, "债权：" + invest.getId()
										+ "转让成功");
					} catch (InsufficientBalance e) {
						log.debug(e.getMessage());
					}

					ht.update(invest);
					ht.update(ta);
					// 生成购买债权后的还款数据，调整之前的还款数据
					generateTransferRepay(ta.getInvest().getInvestRepays(),
							invest, corpusRate);
				}

				to.setStatus(TrusteeshipConstants.Status.PASSED);
				ht.update(to);
			} else {
				if (invest != null && ta != null) {
					invest.setStatus(InvestConstants.InvestStatus.CANCEL);
					ta.setStatus(TransferStatus.TRANSFERING);
					try {
						userBillBO.unfreezeMoney(invest.getUser().getId(),
								buyPrice,OperatorInfo.TRANSFER, "债权：" + invest.getId()
										+ "购买失败");
					} catch (InsufficientBalance e) {
						log.debug(e.getMessage());
					}
					ht.update(invest);
					ht.update(ta);
				}
				to.setStatus(TrusteeshipConstants.Status.REFUSED);
				ht.update(to);
				// 真实错误原因
				 throw new RuntimeException(code+ ":" + description);
			}
		}
	}

	public void generateTransferRepay(List<InvestRepay> investRepays,
			Invest invest, double corpusRate) {
		for (Iterator iterator = investRepays.iterator(); iterator.hasNext();) {
			InvestRepay ir =  (InvestRepay) iterator.next();
			if (ir.getStatus().equals(RepayStatus.WAIT_REPAY_VERIFY)
					|| ir.getStatus().equals(RepayStatus.OVERDUE)
					|| ir.getStatus().equals(RepayStatus.BAD_DEBT)) {
				throw new RuntimeException("investRepay with status "
						+ RepayStatus.WAIT_REPAY_VERIFY + "exist!");
			} else if (ir.getStatus().equals(RepayStatus.REPAYING)) {
				// 根据购买本金比例，生成债权还款信息
			  InvestRepay irNew = new InvestRepay();
				try {
					BeanUtils.copyProperties(irNew, ir);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			    irNew.setId(IdGenerator.randomUUID());
				irNew.setCorpus(ArithUtil.mul(ir.getCorpus(), corpusRate, 2));
				irNew.setDefaultInterest(ArithUtil.mul(ir.getDefaultInterest(),
						corpusRate, 2));
				irNew.setFee(ArithUtil.mul(ir.getFee(), corpusRate, 2));
				irNew.setInterest(ArithUtil.mul(ir.getInterest(),
						corpusRate, 2));
				irNew.setInvest(invest);
				// 修改原投资的还款信息
				ir.setCorpus(ArithUtil.sub(ir.getCorpus(),
						irNew.getCorpus()));
				ir.setDefaultInterest(ArithUtil.sub(
						ir.getDefaultInterest(), irNew.getDefaultInterest()));
				ir.setFee(ArithUtil.sub(ir.getFee(), irNew.getFee()));
				ir.setInterest(ArithUtil.sub(ir.getInterest(),
						irNew.getInterest()));
				ht.merge(irNew);
				if (corpusRate == 1) {
					ht.delete(ir);
					iterator.remove();
				}else{
				    ht.update(ir);
				}
			}
		}

	}
}
