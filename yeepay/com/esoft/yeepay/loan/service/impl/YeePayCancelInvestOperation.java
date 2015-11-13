package com.esoft.yeepay.loan.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.user.UserBillConstants.OperatorInfo;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.annotations.Logger;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.Dom4jUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.jdp2p.invest.InvestConstants;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.loan.LoanConstants;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.loan.model.OperationLog;
import com.esoft.jdp2p.loan.service.impl.LoanServiceImpl;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
@Service("yeePayCancelInvestOperation")
public class YeePayCancelInvestOperation{
	@Resource
	TrusteeshipOperationBO trusteeshipOperationBO;

	@Logger
	static Log log;

	@Resource
	private HibernateTemplate ht;

	@Resource
	UserBillBO ubs;
	

	
	@Transactional(rollbackFor = Exception.class)
	public void createOperation(String loanId, String operatorId) {
		HttpClient client = new HttpClient();
		Loan loan = ht.get(Loan.class, loanId);
		ht.lock(loan, LockMode.UPGRADE);
		loan.setStatus(LoanConstants.LoanStatus.CANCEL);
		loan.setCancelTime(new Date());
		List<Invest> invests = loan.getInvests();
		boolean flag = true;
		for (Invest invest : invests) {
			if(!InvestConstants.InvestStatus.BID_SUCCESS.equals(invest.getStatus())){
				continue;
			}
			// 判断标识 如果存在没有取消投标的投资 则不执行项目流标
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
//			Long count = (Long) ht.find("select count(to) from TrusteeshipOperation to where to.type=? and to.markId=? and to.responseData like ?", YeePayConstants.OperationType.INVEST, invest.getId(), "%<requestNo>03"+invest.getId()+"</requestNo>%").get(0);
//			if (count == 0) {
				//未添加流水号前缀
				content.append("<requestNo>"+(invest.getIsAutoInvest()?YeePayConstants.RequestNoPre.AUTO_INVEST:YeePayConstants.RequestNoPre.INVEST)+ invest.getId() + "</requestNo>");					
//			} else if (count == 1){
//				content.append("<requestNo>"+invest.getId() + "</requestNo>");										
//			}
			content.append("</request>");

			// 保存操作信息
			TrusteeshipOperation to = new TrusteeshipOperation();
			to.setId(IdGenerator.randomUUID());
			to.setMarkId(invest.getId());
			to.setOperator(invest.getId());
			to.setRequestUrl(YeePayConstants.RequestUrl.RequestDirectUrl);
			to.setRequestData(content.toString());
			to.setStatus(TrusteeshipConstants.Status.PASSED);
			to.setType(TrusteeshipConstants.OperationType.CANCEL_LOAN);
			to.setTrusteeship("yeepay");
			trusteeshipOperationBO.save(to);
			
			log.debug(content);
			postMethod.setParameter("req", content.toString());
			String sign = CFCASignUtil.sign(content.toString());
			log.debug("sign:"+sign);
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
					// FIXME:是否需要判断投资的状态
					ubs.unfreezeMoney(invest.getUser().getId(),
							invest.getMoney(),OperatorInfo.CANCEL_LOAN,"借款" + loan.getId()
									+ "流标，解冻投资金额"+invest.getMoney() );
					// 更改投资状态
					invest.setStatus(InvestConstants.InvestStatus.CANCEL);
					ht.update(invest);
				} else {
					flag = false;
				}
			} catch (HttpException e) {
				log.error("Fatal protocol violation: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				log.error("Fatal transport error: " + e.getMessage());
				e.printStackTrace();
			} catch (InsufficientBalance e) {
				throw new RuntimeException(e);
			} finally {
				/* Release the connection. */
				postMethod.releaseConnection();
			}
		}
		// FIXME:如果有一笔没有没有取消流标成功的话 ,下面是否继续处理
		try {
			if (flag) {
				ubs.unfreezeMoney(loan.getUser().getId(), loan.getDeposit(),OperatorInfo.CANCEL_LOAN
						,"借款" + loan.getId() + "流标，解冻保证金"+loan.getDeposit());
			}
		} catch (InsufficientBalance ib) {
			throw new RuntimeException(ib);
		}
		ht.update(loan);
		
	}
}
