package com.esoft.archer.autoRepayment.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.autoRepayment.model.AutoRepayments;
import com.esoft.archer.experience.model.Experienceinvestment;
import com.esoft.archer.user.UserBillConstants;
import com.esoft.archer.user.UserBillConstants.OperatorInfo;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.model.UserBill;
import com.esoft.archer.user.service.UserBillService;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.annotations.Logger;
import com.esoft.core.util.ArithUtil;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.Dom4jUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.bankcard.BankCardConstants.BankCardStatus;
import com.esoft.jdp2p.bankcard.model.BankCard;
import com.esoft.jdp2p.loan.LoanConstants;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.repay.model.InvestRepay;
import com.esoft.jdp2p.repay.model.LoanRepay;
import com.esoft.jdp2p.repay.service.RepayService;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;

@Service("autoRepaymentBO")
public class AutoRepaymentBO {

	@Resource
	HibernateTemplate ht;
	@Resource
	TrusteeshipOperationBO trusteeshipOperationBO;
	@Resource
	private UserBillBO userBillBO;
	@Resource
	private UserBillService ubs;
	@Resource
	private RepayService repayService;
	@Logger
	static Log log;
	
	public int saveOrUpdate(AutoRepayments autoRepayments){
		ht.saveOrUpdate(autoRepayments);
		return 0;
	}
	public List<AutoRepayments> find(String hsql) {
		
		return ht.find(hsql);
	}

	public int update(BankCard bc) {
		ht.update(bc);
		return 0;
	}
	public int update(AutoRepayments record) {
		ht.update(record);
		return 0;
	}
	@Transactional(readOnly=false)
	public String huakou(List<AutoRepayments> autoList){
		//System.out.println("====================================自动划扣===================");
		// 创建一个post方法
		PostMethod postMethod = new PostMethod(YeePayConstants.RequestUrl.RequestDirectUrl);
		HttpClient client = new HttpClient();
		for (AutoRepayments autoRepayments : autoList) {
			//根据人员查询借款用户订单号
			List<Loan> lrsList = ht.find("from Loan where user.id =? and status='repaying' ",autoRepayments.getUserid());
			//根据借款订单id查还款表的信息
			for ( Loan loans : lrsList) {
				//条件:还款中的，订单号一样的,日期等于当天的
				String dateStr=DateUtil.DateToString(new Date(), "yyyy-MM-dd");//当前日期
				String hsql2="from LoanRepay where status =? and loan.id=? and DATE_FORMAT(repayDay,'%Y-%m-%d')=?";
				List<LoanRepay> lrlist = ht.find(hsql2,new Object[] { LoanConstants.RepayStatus.REPAYING,loans.getId(),dateStr});
				//查找用户银行卡
				String hsqluser="from BankCard where user.id =? and status=?";
				List<BankCard> bandlist = ht.find(hsqluser,new Object[] { loans.getUser().getId(),"VERIFIED"});
				//如果有两张银行卡则不能这样写
				for (LoanRepay loanRepay : lrlist) {
					//查询帐户余额是否充足，如果充足则自动还款，否则运行代充值
					double balance = loanRepay.getCorpus()+loanRepay.getDefaultInterest()+loanRepay.getInterest();//要还的钱数
					double payMoney = ubs.getBalance(loans.getUser().getId());//帐户可用钱数
					if (payMoney >= balance) {
						System.out.println("***********代充值失败：用户帐户余额有足够资金可扣除*********进行自动还款********");
						log.debug("*************代充值失败：用户帐户余额有足够资金可扣除***********进行自动还款*********");
						repayService.autoRepay();
						return null;
					}
					
					
					/**
					 * 开始
					 */
					//要还的金额

					StringBuffer content = new StringBuffer();
					content.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
					content.append("<request platformNo='"+YeePayConstants.Config.MER_CODE+"'>");
					String dateStr2=DateUtil.DateToString(new Date(), "yyyyMMddhhmmss");//当前日期
					content.append("<requestNo>"+YeePayConstants.RequestNoPre.HUAKOU+dateStr2+"</requestNo>");
					//用户id
					content.append("<platformUserNo>"+loans.getUser().getId()+"</platformUserNo>");
					//银行商家编号
					content.append("<payWay>"+bandlist.get(0).getBankNo()+"</payWay>");
					//还款的金额
					//content.append("<amount>"+loanRepay.getInterest()+"</amount>");
					content.append("<amount>"+1+"</amount>");
					content.append("<feeMode>PLATFORM</feeMode>");
					//真姓
					content.append("<realName>"+bandlist.get(0).getUser().getRealname()+"</realName>");
					//身份证号
					content.append("<idCardNo>"+bandlist.get(0).getUser().getIdCard()+"</idCardNo>");
					//银行卡号
					content.append("<bankCardNo>"+bandlist.get(0).getCardNo()+"</bankCardNo>");
					// 服务器通知 URL
					content.append("<notifyUrl>"+YeePayConstants.RequestUrl.RequestDirectUrl+"/"+YeePayConstants.OperationType.HUAKOU+"</notifyUrl>");
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
					postMethod.setParameter("service", "WHDEBITNOCARD_RECHARGE");
					// 保存到本地数据库中
					TrusteeshipOperation to = new TrusteeshipOperation();
					String strID=IdGenerator.randomUUID();
					// to表中的主键
					to.setId(strID);
					// 操作的唯一标识（与回调的唯一标识一致，用于查询某一条操作记录）
					to.setMarkId(YeePayConstants.RequestNoPre.HUAKOU+dateStr2);
					// Qr2mui67rUra操作者（如果是开户，此字段为userId；如果为充值，此字段为rechargeId）
					to.setOperator(YeePayConstants.Config.MER_CODE);
					//url
					to.setRequestUrl(YeePayConstants.RequestUrl.RequestDirectUrl+"/"+YeePayConstants.OperationType.HUAKOU);
					
					to.setRequestData(MapUtil.mapToString(params));
					// 第一次保存数据库中的数据为un_send,等待发送
					to.setStatus(TrusteeshipConstants.Status.UN_SEND);
					//to.setStatus(TrusteeshipConstants.Status.PASSED);
					// 类型,充值
					to.setType(YeePayConstants.OperationType.HUAKOU);
					// 托管方
					to.setTrusteeship("yeepay");
					to.setRequestTime(new Date());
					// 将to对象保存到数据库中
					trusteeshipOperationBO.save(to);
					try {
						int statusCode = client.executeMethod(postMethod);
						//System.out.println(statusCode);
						byte[] responseBody = postMethod.getResponseBody();
						log.debug(new String(responseBody, "UTF-8"));
						
						// 响应信息
						String respInfo = new String(new String(responseBody, "UTF-8"));
						try {
							Document respXML = DocumentHelper.parseText(respInfo);
							
							Map<String, String> resultMap = Dom4jUtil.xmltoMap(new String(
									responseBody, "UTF-8"));
							String code = resultMap.get("code");
							
							String description = resultMap.get("description");
							
							if (code.equals("1")) {
								TrusteeshipOperation to2 = (TrusteeshipOperation) ht.find("from TrusteeshipOperation to where to.id="+strID).get(0);
								to2.setStatus(TrusteeshipConstants.Status.PASSED);
								ht.update(to2);
								
								//变更余额
									//给user加锁，保证user_bill顺序更新
									User u = ht.get(User.class, loans.getUser().getId(), LockMode.UPGRADE);
									UserBill ibLastest = userBillBO.getLastestBill(loans.getUser().getId());
									UserBill lb = new UserBill();
									lb.setId(IdGenerator.randomUUID());
									lb.setMoney(loanRepay.getInterest());
									lb.setTime(new Date());
									lb.setDetail("划扣转入成功");
									lb.setType(UserBillConstants.Type.TI_BALANCE);
									lb.setTypeInfo(OperatorInfo.HUAKOU_SUCCESS);
									lb.setUser(u);
									lb.setSeqNum(ibLastest.getSeqNum() + 1);
									// 余额=上一条余额+money
									lb.setBalance(ArithUtil.add(ibLastest.getBalance(), loanRepay.getInterest()));
									// 最新冻结金额=上一条冻结
									lb.setFrozenMoney(ibLastest.getFrozenMoney());
									
									ht.save(lb);
							
								} else {
								//to2.setRequestTime(new Date());
								//to2.setStatus(TrusteeshipConstants.Status.REFUSED);
								//ht.update(to2);
								System.out.println("代充值失败：code="+code+";description=" + description);
								log.debug("代充值失败：code="+code+";description=" + description);
							}
							
							
							
							
						} catch (DocumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					
					/**
					 * 结束
					 */
				}
			}
		}
		
		return null;
		
	}
}