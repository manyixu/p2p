package com.esoft.yeepay.loan.service.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.hibernate.LockMode;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.config.ConfigConstants;
import com.esoft.archer.config.service.ConfigService;
import com.esoft.archer.user.UserBillConstants.OperatorInfo;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.UserBillService;
import com.esoft.core.annotations.Logger;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.ArithUtil;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.Dom4jUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.invest.InvestConstants;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.loan.LoanConstants;
import com.esoft.jdp2p.loan.exception.BorrowedMoneyTooLittle;
import com.esoft.jdp2p.loan.exception.ExistWaitAffirmInvests;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.loan.service.LoanService;
import com.esoft.jdp2p.message.model.UserMessageTemplate;
import com.esoft.jdp2p.message.service.impl.MessageBO;
import com.esoft.jdp2p.repay.RepayConstants.RepayUnit;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.jdp2p.user.service.RechargeService;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
import com.esoft.yeepay.trusteeship.exception.YeePayOperationException;

@Service("yeePayRecheckOperation")
public class YeePayRecheckOperation{
	@Resource
	LoanService loanService;
	@Resource
	TrusteeshipOperationBO trusteeshipOperationBO;
	@Logger
	static Log log;
	@Resource
	HibernateTemplate ht;
	@Resource
	private MessageBO messageBO;
	@Resource
	ConfigService configService;
	@Resource
	RechargeService rechargeService;
	@Resource
	private UserBillService userBillService;
	
	
	/**
	 * 放款
	 * 
	 * @throws ExistWaitAffirmInvests 
	 */
	@Transactional(rollbackFor = Exception.class)
	public void createOperation(Loan instance) throws ExistWaitAffirmInvests {
		Loan loan = ht.get(Loan.class, instance.getId());
		ht.evict(loan);
		loan = ht.get(Loan.class, instance.getId());	
		List<Invest> invests = loan.getInvests();
		
		DecimalFormat currentNumberFormat = new DecimalFormat("#0.00");
		//FIXME:需要限制只取小数点后两位
//		//double managementCost = loanService.calculateManagementFee(loan.getMoney(),loan.getType(), loan.getDeadline(), loan.getRiskLevel());
//		log.debug("LoanHomeSpecial 借款管理费"+managementCost);
		if(null==loan.getLoanGuranteeFee()){
			loan.setLoanGuranteeFee(0.0);
		}
		
		double fee= loan.getLoanGuranteeFee();
		loan.setLoanGuranteeFee(fee);
		ht.update(loan);
		StringBuffer content = new StringBuffer();
		content.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		// platformNo:商户编号
		content.append("<request platformNo=\""+YeePayConstants.Config.MER_CODE+"\">");
		// 标的号
		content.append("<orderNo>" + loan.getId() + "</orderNo>");
		content.append("<requestNo>"+YeePayConstants.RequestNoPre.GIVE_MOENY_TO_BORROWER + loan.getId() + "</requestNo>");
		// 平台方收取费用
		content.append("<fee>"+currentNumberFormat.format(fee)+"</fee>");
		content.append("<transfers>");

		for (Invest invest : invests) {
			if(invest.getStatus().equals(InvestConstants.InvestStatus.BID_SUCCESS)){
				content.append("<transfer>");
				// 之前转账请求时，转账请求流水号
//				/**caijinmin 当responseData为null时获取不到数据   20150512  begin*/
//				Long count = (Long) ht.find("select count(to) from TrusteeshipOperation to where to.type=? and to.markId=? and to.requestData like ?", YeePayConstants.OperationType.INVEST, invest.getId(), "%<requestNo>03"+invest.getId()+"</requestNo>%").get(0);
//				Long count1 = (Long) ht.find("select count(to) from TrusteeshipOperation to where to.type=? and to.markId=? and to.requestData like ?", YeePayConstants.OperationType.AUTO_INVEST, invest.getId(), "%<requestNo>13"+invest.getId()+"</requestNo>%").get(0);
//				/**caijinmin 当responseData为null时获取不到数据   20150512  end*/
//				if (count == 0) {
//					if(count1==0){
//						//未添加流水号前缀
//						content.append("<requestNo>"+ invest.getId() + "</requestNo>");					
//					}else if(count1 == 1){
//						content.append("<requestNo>"+YeePayConstants.RequestNoPre.AUTO_INVEST + invest.getId() + "</requestNo>");	
//					}
//				} else if (count == 1){
//					content.append("<requestNo>"+YeePayConstants.RequestNoPre.INVEST + invest.getId() + "</requestNo>");										
//				}
				if(invest.getIsAutoInvest()){
					content.append("<requestNo>"+YeePayConstants.RequestNoPre.AUTO_INVEST + invest.getId() + "</requestNo>");	
				}else{
					content.append("<requestNo>"+YeePayConstants.RequestNoPre.INVEST + invest.getId() + "</requestNo>");
				}
				/*if (count == 0) {
					//未添加流水号前缀
					content.append("<requestNo>"+ invest.getId() + "</requestNo>");					
				} else if (count == 1){
					content.append("<requestNo>"+YeePayConstants.RequestNoPre.INVEST + invest.getId() + "</requestNo>");										
				}*/
				// 转账请求转账金额
				content.append("<transferAmount>" + invest.getMoney()
						+ "</transferAmount>");
				// 投资人会员类型
				content.append("<sourceUserType>MEMBER</sourceUserType>");
				// 投资人会员编号
				content.append("<sourcePlatformUserNo>" + invest.getUser().getId()
						+ "</sourcePlatformUserNo>");
				// 借款人会员类型
				content.append("<targetUserType>MEMBER</targetUserType>");
				// 借款人会员编号
				content.append("<targetPlatformUserNo>" + loan.getUser().getId()
						+ "</targetPlatformUserNo>");
				content.append("</transfer>");
			}
		}
		content.append("</transfers>");
		// 服务器通知 URL
		content.append("<notifyUrl>"
				+ YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL
				+ YeePayConstants.OperationType.GIVE_MOENY_TO_BORROWER
				+ "</notifyUrl>");
		content.append("</request>");
		log.debug(content.toString());
		
		//保存本地
		TrusteeshipOperation to = new TrusteeshipOperation();
		to.setId(IdGenerator.randomUUID());
		to.setMarkId(loan.getId());
		to.setOperator(loan.getId());
		to.setRequestUrl(YeePayConstants.RequestUrl.RequestDirectUrl);
		to.setRequestData(content.toString());
		to.setStatus(TrusteeshipConstants.Status.UN_SEND);
		to.setType(YeePayConstants.OperationType.GIVE_MOENY_TO_BORROWER);
		to.setTrusteeship("yeepay");
		trusteeshipOperationBO.save(to);
		
		HttpClient client = new HttpClient();
		/* 创建一个post方法 */
		PostMethod postMethod = new PostMethod(
				YeePayConstants.RequestUrl.RequestDirectUrl);
		postMethod.setParameter("req", content.toString());
		String sign = CFCASignUtil.sign(content.toString());
		postMethod.setParameter("sign", sign);
		postMethod.setParameter("service", "LOAN");
		// /*执行post方法*/
		try {
			int statusCode = client.executeMethod(postMethod);
			if (statusCode != HttpStatus.SC_OK) {
				log.debug("Method failed: " + postMethod.getStatusLine());
			}
			/* 获得返回的结果 */
			byte[] responseBody = postMethod.getResponseBody();
			log.debug(new String(responseBody, "UTF-8"));
			String respInfo = new String(new String(responseBody, "UTF-8"));
			@SuppressWarnings("unchecked")
			Map<String, String> respMap = Dom4jUtil.xmltoMap(new String(
					responseBody, "UTF-8"));

			String code = respMap.get("code");
			String description = respMap.get("description");
			log.debug("放款直接响应");
			TrusteeshipOperation to1 = ht.load(TrusteeshipOperation.class, to.getId());
			if("1".equals(code)
					&& TrusteeshipConstants.Status.PASSED
					.equals(to1.getStatus())){
				return;
			}
			
			//请求已发送
			to.setStatus(TrusteeshipConstants.Status.SENDED);
			ht.update(to);
			
			log.debug(invests.size());
			
			if (code.equals("1")) {
				to.setResponseData(respInfo);
				to.setResponseTime(new Date());
				to.setStatus(TrusteeshipConstants.Status.PASSED);
				ht.update(to);
				if (loan.getStatus().equals(LoanConstants.LoanStatus.RECHECK)) {
					try {
						loanService.giveMoneyToBorrower(loan.getId());

						//发送短信放款成功
						String enableRepayAlert = "0";
						try {
							enableRepayAlert = configService.getConfigValue(ConfigConstants.Schedule.LOAN_MESSAGE_REMIND);
						} catch (ObjectNotFoundException onfe) {
							onfe.printStackTrace();
						}
						//获取当前项目借款人
						//Loan loan = ht.get(Loan.class, loan.getId());
						//如果放款短信为1开启则运行
						log.debug("放款短信提醒====="+enableRepayAlert);
						if (enableRepayAlert.equals("1")) {
							Map<String, String> params = new HashMap<String, String>();
							params.put("username", loan.getUser().getMobileNumber());
							System.out.println(ConfigConstants.Schedule.LOAN_MESSAGE_REMIND + "_sms");
							UserMessageTemplate umt=ht.get(UserMessageTemplate.class,ConfigConstants.Schedule.LOAN_MESSAGE_REMIND_ALERT + "_sms");
							//发送短信
							messageBO.sendSMS(umt,
									params, loan.getUser().getMobileNumber());

						}
						try {
							sendPlatformTransfer2(loan);
						} catch (YeePayOperationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExistWaitAffirmInvests e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (BorrowedMoneyTooLittle e) {
						log.debug(e.getMessage());
						e.printStackTrace();
					}
				}
				
			} else {
				to.setResponseData(respInfo);
				to.setResponseTime(new Date());
				to.setStatus(TrusteeshipConstants.Status.REFUSED);
				ht.update(to);
				//throw new YeePayOperationException(description);
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
		
	}
	
	/**
	 * 邀请返利平台划款
	 * 
	 * @throws ExistWaitAffirmInvests 
	 */
	public void sendPlatformTransfer2(Loan loan)  throws YeePayOperationException, ExistWaitAffirmInvests{
			HttpClient client = new HttpClient();
			PostMethod postMethod = new PostMethod(YeePayConstants.RequestUrl.RequestDirectUrl);
			//拿到投资人
			//Loan loan = ht.get(Loan.class, instance.getId());	
			List<Invest> invests = loan.getInvests();
			String userId1 = null;//一级推荐人
			String userId2 = null;//二级推荐人
			double userMoney1 = 0;//一级推荐人的钱数
			double userMoney2 = 0;//二级推荐人的钱数
			StringBuffer content = new StringBuffer();
			content.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
			content.append("<request platformNo='"+YeePayConstants.Config.MER_CODE+"'>");
			content.append("<requestNo>"+loan.getId()+YeePayConstants.RequestNoPre.REPAY2+"</requestNo>");
			content.append("<userType>MERCHANT</userType>");
			content.append("<bizType>TRANSFER</bizType>");
			content.append("<platformUserNo>"+YeePayConstants.Config.MER_CODE+"</platformUserNo>");
			content.append("<details>");
			Date d = new Date();
			
			int day = loan.getDeadline();
			if(loan.getType().equals(RepayUnit.MONTH)){
				day = DateUtil.getIntervalDays(d,DateUtil.addMonth(d, loan.getDeadline()));
			}
			for (Invest invest : invests) {
				if(InvestConstants.InvestStatus.REPAYING.equals(invest.getStatus())||InvestConstants.InvestStatus.BID_SUCCESS.equals(invest.getStatus())){
					//拿到投资用户
					User investUser = invest.getUser();
					log.debug("userId=="+investUser.getId());
					//判断投资用户的推荐人是否存在（二级推荐人）
					if(investUser.getReferrer()!=null&&!investUser.getReferrer().equals("")){
						userId2 = investUser.getReferrer();
						//查询投资用户的推荐人（二级推荐人）
						User user2 = ht.get(User.class, userId2);
						//判断投资用户的推荐人的推荐人是否存在（一级推荐人）
						if(user2.getReferrer()!=null&&!user2.getReferrer().equals("")){
							userId1 = user2.getReferrer();
							//计算一级推荐人获得的钱数，投资金额*1%*投资天数/365
							userMoney1 = ArithUtil.round(invest.getInvestMoney()*0.005*day/365,2);
							content.append("<detail>");
							content.append("<targetUserType>MEMBER</targetUserType>");
							content.append("<targetPlatformUserNo>"+userId1+"</targetPlatformUserNo>");
							content.append("<amount>"+userMoney1+"</amount>");
							content.append("<bizType>TRANSFER</bizType>");
							content.append("</detail>");
							//计算二级推荐人获得的钱数，投资金额*0.5%*投资天数/365
							userMoney2 = ArithUtil.round(invest.getInvestMoney()*0.01*day/365,2);
							content.append("<detail>");
							content.append("<targetUserType>MEMBER</targetUserType>");
							content.append("<targetPlatformUserNo>"+userId2+"</targetPlatformUserNo>");
							content.append("<amount>"+userMoney2+"</amount>");
							content.append("<bizType>TRANSFER</bizType>");
							content.append("</detail>");
						}else{
							//计算二级推荐人获得的钱数，投资金额*0.5%*投资天数/365
							userMoney2 = ArithUtil.round(invest.getInvestMoney()*0.01*day/365,2);
							content.append("<detail>");
							content.append("<targetUserType>MEMBER</targetUserType>");
							content.append("<targetPlatformUserNo>"+userId2+"</targetPlatformUserNo>");
							content.append("<amount>"+userMoney2+"</amount>");
							content.append("<bizType>TRANSFER</bizType>");
							content.append("</detail>");
						}
						
					}
					
				}
				
			}
			
			content.append("</details>");
			// 服务器通知 URL
			content.append("<notifyUrl>"+YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL+"/"+YeePayConstants.OperationType.RECHARGE3+"</notifyUrl>");
			content.append("</request>");
			log.debug(content.toString());
			
			// 生成密文
			String sign = CFCASignUtil.sign(content.toString());
			// 包装参数
			Map<String, String> params = new HashMap<String, String>();
			params.put("req", content.toString());
			params.put("sign", sign);
			log.debug("createRechargeOrder的" + params);
			postMethod.setParameter("req", content.toString());
			postMethod.setParameter("sign", sign);
			postMethod.setParameter("service", "DIRECT_TRANSACTION");
			// 保存到本地数据库中
			TrusteeshipOperation to = new TrusteeshipOperation();
			String strID=IdGenerator.randomUUID();
			// to表中的主键
			to.setId(strID);
			// 操作的唯一标识（与回调的唯一标识一致，用于查询某一条操作记录）
			to.setMarkId(loan.getId());
			// Qr2mui67rUra操作者（如果是开户，此字段为userId；如果为充值，此字段为rechargeId）
			to.setOperator(loan.getId());
			//url
			to.setRequestUrl(YeePayConstants.RequestUrl.RequestDirectUrl+"/"+YeePayConstants.OperationType.RECHARGE3);
			to.setResponseData(content.toString());
			to.setRequestData(MapUtil.mapToString(params));
			// 第一次保存数据库中的数据为un_send,等待发送(董改了成功)
			to.setStatus(TrusteeshipConstants.Status.SENDED);
			// 类型,充值
			to.setType(YeePayConstants.OperationType.RECHARGE3);
			// 托管方
			to.setTrusteeship("yeepay");
			to.setRequestTime(new Date());
			// 将to对象保存到数据库中
			trusteeshipOperationBO.save(to);
			try {
				int statusCode = client.executeMethod(postMethod);
				if (statusCode != HttpStatus.SC_OK) {
					log.debug("Method failed: " + postMethod.getStatusLine());
				}
				byte[] responseBody = postMethod.getResponseBody();
				log.debug(new String(responseBody, "UTF-8"));
				
				@SuppressWarnings("unchecked")
				Map<String, String> respMap = Dom4jUtil.xmltoMap(new String(
						responseBody, "UTF-8"));

				String code = respMap.get("code");
				String description = respMap.get("description");
				
				// 响应信息
				if (code.equals("1")&&to.getStatus().equals(TrusteeshipConstants.Status.SENDED)) {
					to.setStatus(TrusteeshipConstants.Status.PASSED);
					for (Invest invest : invests) {
						if(InvestConstants.InvestStatus.REPAYING.equals(invest.getStatus())||InvestConstants.InvestStatus.BID_SUCCESS.equals(invest.getStatus())){
							//拿到投资用户
							User investUser = invest.getUser();
							log.debug("userId=="+investUser.getId());
							//判断投资用户的推荐人是否存在（二级推荐人）
							if(investUser.getReferrer()!=null&&!investUser.getReferrer().equals("")){
								userId2 = investUser.getReferrer();
								//查询投资用户的推荐人（二级推荐人）
								User user2 = ht.get(User.class, userId2);
								//判断投资用户的推荐人的推荐人是否存在（一级推荐人）
								if(user2.getReferrer()!=null&&!user2.getReferrer().equals("")){
									userId1 = user2.getReferrer();
									User user1 = ht.get(User.class, userId1);
									//计算一级推荐人获得的钱数，投资金额*1%*投资天数/365
									userMoney1 = ArithUtil.round(invest.getInvestMoney()*0.005*day/365,2);
									userBillService.transferIntoBalance(userId1,userMoney1, OperatorInfo.REFERRER_SUCCESS,"用户："+user1.getMobileNumber()+"，邀请返利成功，金额：" + userMoney1);
									//计算二级推荐人获得的钱数，投资金额*0.5%*投资天数/365
									userMoney2 = ArithUtil.round(invest.getInvestMoney()*0.01*day/365,2);
									userBillService.transferIntoBalance(userId2,userMoney2, OperatorInfo.REFERRER_SUCCESS,"用户："+user2.getMobileNumber()+"，邀请返利成功，金额：" + userMoney2);
								}else{
									//计算二级推荐人获得的钱数，投资金额*0.5%*投资天数/365
									userMoney2 = ArithUtil.round(invest.getInvestMoney()*0.01*day/365,2);
									userBillService.transferIntoBalance(userId2,userMoney2, OperatorInfo.REFERRER_SUCCESS,"用户："+user2.getMobileNumber()+"，邀请返利成功，金额：" + userMoney2);
								}
								
							}
						}
					}
				} else {
					System.out.println("邀请返利处理失败：code="+code+";description=" + description);
					log.debug("邀请返利处理失败：code="+code+";description=" + description);;
					to.setStatus(TrusteeshipConstants.Status.REFUSED);
				}
				ht.update(to);
					
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	
	/**
	 * 邀请返利平台划款 异步回调
	 * 
	 * @throws ExistWaitAffirmInvests 
	 */
	public void sendPlatformTransfer2S2SCallback(ServletRequest request,ServletResponse response){
		String notifyXML = request.getParameter("notify");
		String sign = request.getParameter("sign");
		log.debug("邀请返利平台划款 异步回调异步响应");
		boolean flag = CFCASignUtil.isVerifySign(notifyXML, sign);
		if(!flag){//验签未通过
			log.debug("邀请返利平台划款 异步回调S2S回调通知时，验签失败，notifyXML："+notifyXML);
			return;
		}else{
			log.debug("邀请返利平台划款 异步回调S2S回调通知时，验签成功，notifyXML："+notifyXML);
			try {//易宝对接收的请求已做处理
				response.getWriter().print("SUCCESS");
				FacesUtil.getCurrentInstance().responseComplete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		@SuppressWarnings("unchecked")
		Map<String, String> resultMap = Dom4jUtil.xmltoMap(notifyXML);
		String loanId = resultMap.get("orderNo");
		String requestNo = resultMap.get("requestNo").replaceFirst(YeePayConstants.RequestNoPre.REPAY2, "");
		TrusteeshipOperation to = trusteeshipOperationBO.get(
				YeePayConstants.OperationType.RECHARGE3, requestNo,
				requestNo, "yeepay");
		String toId = to.getId();
		TrusteeshipOperation to1 = ht.load(TrusteeshipOperation.class, toId);
		ht.evict(to1);
		to1 =  ht.load(TrusteeshipOperation.class, toId,LockMode.UPGRADE);
		String code = resultMap.get("code");
		String userId1 = null;//一级推荐人
		String userId2 = null;//二级推荐人
		double userMoney1 = 0;//一级推荐人的钱数
		double userMoney2 = 0;//二级推荐人的钱数
		// 响应信息
		if (code.equals("1")&&to.getStatus().equals(TrusteeshipConstants.Status.SENDED)) {
			to1.setStatus(TrusteeshipConstants.Status.PASSED);
			ht.update(to1);
			Loan loan = ht.get(Loan.class, to.getMarkId());	
			List<Invest> invests = loan.getInvests();
			Date d = new Date();
			int day = loan.getDeadline();
			if(loan.getType().equals(RepayUnit.MONTH)){
				day = DateUtil.getIntervalDays(d,DateUtil.addMonth(d, loan.getDeadline()));
			}
			for (Invest invest : invests) {
				if(InvestConstants.InvestStatus.REPAYING.equals(invest.getStatus())||InvestConstants.InvestStatus.BID_SUCCESS.equals(invest.getStatus())){
					//拿到投资用户
					User investUser = invest.getUser();
					log.debug("userId=="+investUser.getId());
					//判断投资用户的推荐人是否存在（二级推荐人）
					if(investUser.getReferrer()!=null&&!investUser.getReferrer().equals("")){
						userId2 = investUser.getReferrer();
						//查询投资用户的推荐人（二级推荐人）
						User user2 = ht.get(User.class, userId2);
						//判断投资用户的推荐人的推荐人是否存在（一级推荐人）
						if(user2.getReferrer()!=null&&!user2.getReferrer().equals("")){
							userId1 = user2.getReferrer();
							User user1 = ht.get(User.class, userId1);
							//计算一级推荐人获得的钱数，投资金额*1%*投资天数/365
							userMoney1 = ArithUtil.round(invest.getInvestMoney()*0.005*day/365,2);
							userBillService.transferIntoBalance(userId1,userMoney1, OperatorInfo.REFERRER_SUCCESS,"用户："+user1.getMobileNumber()+"，邀请返利成功，金额：" + userMoney1);
							//计算二级推荐人获得的钱数，投资金额*0.5%*投资天数/365
							userMoney2 = ArithUtil.round(invest.getInvestMoney()*0.01*day/365,2);
							userBillService.transferIntoBalance(userId2,userMoney2, OperatorInfo.REFERRER_SUCCESS,"用户："+user2.getMobileNumber()+"，邀请返利成功，金额：" + userMoney2);
						}else{
							//计算二级推荐人获得的钱数，投资金额*0.5%*投资天数/365
							userMoney2 = ArithUtil.round(invest.getInvestMoney()*0.01*day/365,2);
							userBillService.transferIntoBalance(userId2,userMoney2, OperatorInfo.REFERRER_SUCCESS,"用户："+user2.getMobileNumber()+"，邀请返利成功，金额：" + userMoney2);
						}
						
					}
				}
			}
		} else {
			log.debug("邀请返利平台划款 异步回调"+loanId+"异常返回码code="+code+",返回信息："+resultMap.get("message"));
			to1.setStatus(TrusteeshipConstants.Status.REFUSED);
			ht.update(to1);
		}
					
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void receiveOperationS2SCallback(ServletRequest request,ServletResponse response) {
		
		String notifyXML = request.getParameter("notify");
		String sign = request.getParameter("sign");
		log.debug("放款异步响应");
		boolean flag = CFCASignUtil.isVerifySign(notifyXML, sign);
		if(!flag){//验签未通过
			log.debug("放款S2S回调通知时，验签失败，notifyXML："+notifyXML);
			return;
		}else{
			log.debug("放款S2S回调通知时，验签成功，notifyXML："+notifyXML);
			try {//易宝对接收的请求已做处理
				response.getWriter().print("SUCCESS");
				FacesUtil.getCurrentInstance().responseComplete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		@SuppressWarnings("unchecked")
		Map<String, String> resultMap = Dom4jUtil.xmltoMap(notifyXML);
		String loanId = resultMap.get("orderNo");
		String requestNo = resultMap.get("requestNo").replaceFirst(YeePayConstants.RequestNoPre.GIVE_MOENY_TO_BORROWER, "");
		TrusteeshipOperation to = trusteeshipOperationBO.get(
				YeePayConstants.OperationType.GIVE_MOENY_TO_BORROWER, requestNo,
				requestNo, "yeepay");
		String toId = to.getId();
		TrusteeshipOperation to1 = ht.load(TrusteeshipOperation.class, toId);
		ht.evict(to1);
		to1 =  ht.load(TrusteeshipOperation.class, toId,LockMode.UPGRADE);
		
		
		if(TrusteeshipConstants.Status.SENDED.equals(to1.getStatus())){
			
			String code = resultMap.get("code");
			
			if("1".equals(code)){
				to1.setStatus(TrusteeshipConstants.Status.PASSED);
				ht.update(to1);
				
				Loan loan = ht.get(Loan.class, loanId);
				ht.evict(loan);
				loan = ht.get(Loan.class, loanId);	
				
				if(LoanConstants.LoanStatus.RECHECK.equals(loan.getStatus())){
					try {
						loanService.giveMoneyToBorrower(loanId);

						//发送短信放款成功
						String enableRepayAlert = "0";
						try {
							enableRepayAlert = configService.getConfigValue(ConfigConstants.Schedule.LOAN_MESSAGE_REMIND);
						} catch (ObjectNotFoundException onfe) {
							onfe.printStackTrace();
						}
						//获取当前项目借款人
						//Loan loan = ht.get(Loan.class, loan.getId());
						//如果放款短信为1开启则运行
						log.debug("放款短信提醒====="+enableRepayAlert);
						if (enableRepayAlert.equals("1")) {
							Map<String, String> params = new HashMap<String, String>();
							params.put("username", loan.getUser().getMobileNumber());
							System.out.println(ConfigConstants.Schedule.LOAN_MESSAGE_REMIND + "_sms");
							UserMessageTemplate umt=ht.get(UserMessageTemplate.class,ConfigConstants.Schedule.LOAN_MESSAGE_REMIND_ALERT + "_sms");
							//发送短信
							messageBO.sendSMS(umt,
									params, loan.getUser().getMobileNumber());

						}
						try {
							sendPlatformTransfer2(loan);
						} catch (YeePayOperationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExistWaitAffirmInvests e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						log.debug("标的"+loanId+"放款成功");
					} catch (BorrowedMoneyTooLittle e) {
						log.debug("标的"+loanId+"放款失败");
						log.debug(e.getMessage());
						e.printStackTrace();
					} catch (ExistWaitAffirmInvests e) {
						log.debug("标的"+loanId+"放款失败");
						log.debug(e.getMessage());
						e.printStackTrace();
					} 
				}
				
				
			}else{
				log.debug("标的"+loanId+"异常返回码code="+code+",返回信息："+resultMap.get("message"));
				to1.setStatus(TrusteeshipConstants.Status.REFUSED);
				ht.update(to1);
			}
	
		}
		
	}

}
