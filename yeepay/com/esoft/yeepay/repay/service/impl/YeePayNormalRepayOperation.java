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
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.experience.model.Experiencegold;
import com.esoft.archer.experience.model.Experienceinvestment;
import com.esoft.archer.experience.service.ExperiencegoldMapper;
import com.esoft.archer.experience.service.ExperienceinvestmentMapper;
import com.esoft.archer.user.UserBillConstants;
import com.esoft.archer.user.UserBillConstants.OperatorInfo;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.model.UserBill;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.annotations.Logger;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.ArithUtil;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.Dom4jUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;

import com.esoft.jdp2p.invest.InvestConstants.TransferStatus;
import com.esoft.jdp2p.invest.model.TransferApply;
import com.esoft.jdp2p.invest.service.TransferService;
import com.esoft.jdp2p.loan.LoanConstants;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.repay.exception.NormalRepayException;
import com.esoft.jdp2p.repay.model.InvestRepay;
import com.esoft.jdp2p.repay.model.LoanRepay;
import com.esoft.jdp2p.repay.model.Repay;
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
@Service("yeePayNormalRepayOperation")
public class YeePayNormalRepayOperation extends
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
	private ExperienceinvestmentMapper experienceinvestmentMapper;
	@Resource
	private ExperiencegoldMapper experiencegoldMapper;
	@Resource
	RepayService repayService;
	
	
	@Resource
	TransferService transferService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public TrusteeshipOperation createOperation(LoanRepay loanRepay,
			FacesContext fc) throws IOException {
		// FIXME:验证
		LoanRepay repay = ht.get(LoanRepay.class, loanRepay.getId());
		List<InvestRepay> irs = ht
				.find("from InvestRepay ir where ir.invest.loan.id=? and ir.period=?",
						new Object[] { repay.getLoan().getId(),
								repay.getPeriod() });
		
		// 取消投资下面的所有正在转让的债权
		String hql = "from TransferApply ta where ta.invest.loan.id=? and ta.status=?";
		List<TransferApply> tas = ht.find(hql, new String[] { repay.getLoan().getId(),
				TransferStatus.WAITCONFIRM });
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
		content.append("<requestNo>"+YeePayConstants.RequestNoPre.REPAY + repay.getId() + "</requestNo>");
		// 标的号:标识一笔要自动还款的标的的标的号
		content.append("<orderNo>" + repay.getLoan().getId() + "</orderNo>");
		content.append("<repayments>");
		double irRepayMoney = 0D;
		double irFee = 0D;
		for (InvestRepay ir : irs) {
			// 借款人还款手续费
			if (irs.get(0).equals(ir)) {
				irRepayMoney = ArithUtil.add(ArithUtil.add(ir.getCorpus(), ir.getInterest()),
						repay.getFee());
				irFee = ArithUtil.add(ir.getFee(), repay.getFee());
			} else {
				irRepayMoney = ArithUtil.add(ir.getCorpus(), ir.getInterest());
				irFee = ir.getFee();
			}
			content.append("<repayment>");
			// 转账请求流水号
			/**caijinmin 有债权转让的还款添加14前缀 20150502 begin*/
			if(ir.getInvest().getId().length() == 14){
			/**caijinmin 有债权转让的还款添加14前缀 20150502 end*/
//				Long count = (Long) ht.find("select count(to) from TrusteeshipOperation to where to.type=? and to.markId=? and to.requestData like ?", YeePayConstants.OperationType.INVEST, ir.getInvest().getId(), "%<requestNo>03"+ir.getInvest().getId()+"</requestNo>%").get(0);
//				Long count1 = (Long) ht.find("select count(to) from TrusteeshipOperation to where to.type=? and to.markId=? and to.requestData like ?", YeePayConstants.OperationType.AUTO_INVEST,ir.getInvest().getId(), "%<requestNo>13"+ir.getInvest().getId()+"</requestNo>%").get(0);
//				if (count == 0) {
//					if(count1 == 0){
//						//未添加流水号前缀
//						content.append("<paymentRequestNo>" + ir.getInvest().getId()
//								+ "</paymentRequestNo>");
//					}else{
//						//自动投标
//						content.append("<paymentRequestNo>" + YeePayConstants.RequestNoPre.AUTO_INVEST+ir.getInvest().getId()
//								+ "</paymentRequestNo>");
//					}
//				} else if (count == 1){
//					content.append("<paymentRequestNo>" + YeePayConstants.RequestNoPre.INVEST+ir.getInvest().getId()
//							+ "</paymentRequestNo>");
//				}
				if(ir.getInvest().getIsAutoInvest()){
					content.append("<paymentRequestNo>" + YeePayConstants.RequestNoPre.AUTO_INVEST+ir.getInvest().getId()
							+ "</paymentRequestNo>");					
				}else{
					content.append("<paymentRequestNo>" + YeePayConstants.RequestNoPre.INVEST+ir.getInvest().getId()
					+ "</paymentRequestNo>");					
				}
			/**caijinmin 有债权转让的还款添加14前缀 20150502 begin*/
			}else{
				content.append("<paymentRequestNo>" + YeePayConstants.RequestNoPre.TRANSFER+ir.getInvest().getId()
						+ "</paymentRequestNo>");
			}
			/**caijinmin 有债权转让的还款添加14前缀 20150502 end*/
			// 投资人会员编号
			content.append("<targetUserNo>" + ir.getInvest().getUser().getId()
					+ "</targetUserNo>");
			// 还款金额
			content.append("<amount>" + currentNumberFormat.format(irRepayMoney)
					+ "</amount>");
			// 还款平台提成
			content.append("<fee>" + currentNumberFormat.format(irFee)
					+ "</fee>");
			content.append("</repayment>");
		}
		content.append("</repayments>");
		content.append("<callbackUrl>"
				+ YeePayConstants.ResponseWebUrl.PRE_RESPONSE_URL
				+ YeePayConstants.OperationType.REPAY + "</callbackUrl>");
		// 服务器通知 URL
		content.append("<notifyUrl>"
				+ YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL
				+ YeePayConstants.OperationType.REPAY + "</notifyUrl>");
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
		to.setType(TrusteeshipConstants.OperationType.REPAY);
		to.setTrusteeship("yeepay");
		trusteeshipOperationBO.save(to);
		try {
			sendOperation(to.getId(), "utf-8", fc);
		} catch (IOException e) {
			throw new YeePayOperationException("网络连接不通畅！", e);
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
			String requestNo = resultMap.get("requestNo").replaceFirst(YeePayConstants.RequestNoPre.REPAY, "");
			log.debug(respXML);
			LoanRepay repay = ht.get(LoanRepay.class, requestNo,LockMode.UPGRADE);
			if(LoanConstants.RepayStatus.COMPLETE.equals(repay.getStatus())){
				return;
			}
			TrusteeshipOperation to = trusteeshipOperationBO.get(
					YeePayConstants.OperationType.REPAY, requestNo, requestNo,
					"yeepay");
			to.setResponseTime(new Date());
//			if(TrusteeshipConstants.Status.PASSED.equals(to.getStatus())){
//				return ;
//			}
			if ("1".equals(code)) {
				try {
					log.debug("repayservice.normalRepay(repayID" + requestNo
							+ ")");
					if (repay.getStatus().equals(
							LoanConstants.RepayStatus.REPAYING)) {
						repayService.normalRepay(requestNo);
					}
				} catch (InsufficientBalance e) {
					throw new YeePayOperationException(e.getMessage(), e);
				} catch (NormalRepayException e) {
					throw new YeePayOperationException(e.getMessage(), e);
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
			String requestNo = resultMap.get("requestNo").replaceFirst(YeePayConstants.RequestNoPre.REPAY, "");
			log.debug(notifyXML);
			LoanRepay repay = ht.get(LoanRepay.class, requestNo,LockMode.UPGRADE);
			if(LoanConstants.RepayStatus.COMPLETE.equals(repay.getStatus())){
				return;
			}
			TrusteeshipOperation to = trusteeshipOperationBO.get(
					YeePayConstants.OperationType.REPAY, requestNo, requestNo,
					"yeepay");
			to.setResponseTime(new Date());
//			if(TrusteeshipConstants.Status.PASSED.equals(to.getStatus())){
//				return ;
//			}
			if ("1".equals(code)) {
				try {
					log.debug("repayservice.normalRepay(repayID" + requestNo
							+ ")");
					if (repay.getStatus().equals(
							LoanConstants.RepayStatus.REPAYING)) {
						repayService.normalRepay(requestNo);
					}
				} catch (InsufficientBalance e) {
					throw new YeePayOperationException(e.getMessage(), e);

				} catch (NormalRepayException e) {
					throw new YeePayOperationException(e.getMessage(), e);
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
	/**
	 * 直接打款异步回调
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void experienceinvesRechargeS2SCallback(ServletRequest request,
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
		log.debug("notifyXML:" + notifyxml + "sign:" + sign + "flag" + flag);
		if (flag) {
			// 处理账户开通成功
			@SuppressWarnings("unchecked")
			Map<String, String> resultMap = Dom4jUtil.xmltoMap(notifyxml);
			String code = resultMap.get("code");
			String requestNo = resultMap.get("requestNo");
			String message = resultMap.get("message");
			String platformUserNo = resultMap.get("platformUserNo");
			log.debug(notifyxml);
			TrusteeshipOperation to = trusteeshipOperationBO.get(
					YeePayConstants.OperationType.RECHARGE2,
					requestNo, requestNo, "yeepay");
			ht.evict(to);
			to = ht.get(TrusteeshipOperation.class, to.getId(),
					LockMode.UPGRADE);
			to.setResponseTime(new Date());
			to.setResponseData(notifyxml);
			
			if ("1".equals(code)&&to.getStatus().equals(TrusteeshipConstants.Status.SENDED)) {
				String dateStr2=DateUtil.DateToString(new Date(), "yyyy-MM-dd");//当前日期
				//当前日期与结束日期比
				String hsql="from Experienceinvestment where DATE_FORMAT(enddate,'%Y-%m-%d')='"+dateStr2+"' and etype='Y'";
				List<Experienceinvestment> eList=experienceinvestmentMapper.find(hsql);
				if(eList.size()==0){
					log.debug("没有到期的体验金。");
				}
				
				//复制的以前的东西

				to.setStatus(TrusteeshipConstants.Status.PASSED);
				//变更余额
				for (Experienceinvestment exper2:eList) {
					try {
						systemBillService.transferOut(exper2.getProfit(),OperatorInfo.WITHDRAWALS_SUCCESS, "体验金活动充值划账：用户ID"+exper2.getUserid());
					} catch (InsufficientBalance e) {
						log.debug("平台账户不足！");
						System.out.println("************平台账户不足*****************");
						e.printStackTrace();
					}
					//给user加锁，保证user_bill顺序更新
					User u = ht.get(User.class, exper2.getUserid(), LockMode.UPGRADE);
					UserBill ibLastest = userBillBO.getLastestBill(exper2.getUserid());
					UserBill lb = new UserBill();
					if(ibLastest == null){
						lb.setId(IdGenerator.randomUUID());
						lb.setMoney(exper2.getProfit());
						lb.setTime(new Date());
						lb.setDetail("体验金转入成功 "+" 充值编号："+requestNo);
						lb.setType(UserBillConstants.Type.TI_BALANCE);
						lb.setTypeInfo(OperatorInfo.WITHDRAWALS_SUCCESS);
						//这个自己添加的用不了lb.setTypeInfo(OperatorInfo.WITHDRAWALS_SUCCESS);
						lb.setUser(u);
						lb.setSeqNum(0L);
						// 余额=上一条余额+money
						lb.setBalance(exper2.getProfit());
						// 最新冻结金额=上一条冻结
						lb.setFrozenMoney(0.00);
					}else{
						lb.setId(IdGenerator.randomUUID());
						lb.setMoney(exper2.getProfit());
						lb.setTime(new Date());
						lb.setDetail("体验金转入成功"+" 充值编号："+requestNo);
						lb.setType(UserBillConstants.Type.TI_BALANCE);
						lb.setTypeInfo(OperatorInfo.WITHDRAWALS_SUCCESS);
						lb.setUser(u);
						lb.setSeqNum(ibLastest.getSeqNum() + 1);
						// 余额=上一条余额+money
						lb.setBalance(ArithUtil.add(ibLastest.getBalance(), exper2.getProfit()));
						// 最新冻结金额=上一条冻结
						lb.setFrozenMoney(ibLastest.getFrozenMoney());
					}
					
					ht.save(lb);
					//修改状态
					exper2.setEtype("N");
					experienceinvestmentMapper.update(exper2);
					String hsqlexp="from Experiencegold where userid='"+exper2.getUserid()+"' and projectid='"+exper2.getEname()+"'";
					List<Experiencegold> list = experiencegoldMapper.find(hsqlexp);
					for (int j = 0; j < list.size(); j++) {
						Experiencegold experiencegold=list.get(j);
						experiencegold.setUtype("N");
						experiencegoldMapper.update(experiencegold);
						
					}
				}
				
				
				ht.update(to);
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
