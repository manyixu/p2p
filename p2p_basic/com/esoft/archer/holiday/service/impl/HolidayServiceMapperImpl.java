package com.esoft.archer.holiday.service.impl;

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

import com.esoft.archer.experience.model.Experienceinvestment;
import com.esoft.archer.holiday.model.HolidayTable;
import com.esoft.archer.holiday.service.HolidayServiceMapper;
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
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.risk.service.SystemBillService;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;

@Service("holidayServiceMapper")
public class HolidayServiceMapperImpl implements HolidayServiceMapper {
	@Resource
	TrusteeshipOperationBO trusteeshipOperationBO;
	@Resource
	HibernateTemplate ht;
	@Resource
	SystemBillService sbs;
	@Resource
	private UserBillBO userBillBO;
	@Logger
	static Log log;
	
	@Override
	@Transactional(readOnly=false)
	public String HolidaySendPlatformTransfer(HolidayTable holiday) {
		
		String dateStr=DateUtil.DateToString(new Date(), "yyyyMMddhhmmss");//当前日期
		HttpClient client = new HttpClient();
			
		// 创建一个post方法
		PostMethod postMethod = new PostMethod(YeePayConstants.RequestUrl.RequestDirectUrl);
			
		StringBuffer content = new StringBuffer();
		content.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
		content.append("<request platformNo='"+YeePayConstants.Config.MER_CODE+"'>");
		content.append("<requestNo>"+dateStr+YeePayConstants.RequestNoPre.REPAY2+"</requestNo>");
		content.append("<userType>MERCHANT</userType>");
		content.append("<bizType>TRANSFER</bizType>");
		content.append("<platformUserNo>"+YeePayConstants.Config.MER_CODE+"</platformUserNo>");
		
		content.append("<details>");
		content.append("<detail>");
		content.append("<targetUserType>MEMBER</targetUserType>");
		content.append("<targetPlatformUserNo>"+holiday.getUserid()+"</targetPlatformUserNo>");
		content.append("<amount>"+holiday.getMoney()+"</amount>");
		content.append("<bizType>TRANSFER</bizType>");
		content.append("</detail>");
		content.append("</details>");
		
		// 服务器通知 URL
		content.append("<notifyUrl>"+YeePayConstants.RequestUrl.RequestDirectUrl+"/"+YeePayConstants.OperationType.RECHARGE2+"</notifyUrl>");
		//content.append("<notifyUrl>"+YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL+YeePayConstants.OperationType.RECHARGE2+"</notifyUrl>");
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
		to.setMarkId(dateStr+YeePayConstants.RequestNoPre.REPAY2);
		// Qr2mui67rUra操作者（如果是开户，此字段为userId；如果为充值，此字段为rechargeId）
		to.setOperator(YeePayConstants.Config.MER_CODE);
		//url
		to.setRequestUrl(YeePayConstants.RequestUrl.RequestDirectUrl+"/"+YeePayConstants.OperationType.RECHARGE2);
		to.setResponseData(content.toString());
		to.setRequestData(MapUtil.mapToString(params));
		// 第一次保存数据库中的数据为un_send,等待发送
		to.setStatus(TrusteeshipConstants.Status.SENDED);
		//(董改了成功)
		//to.setStatus(TrusteeshipConstants.Status.PASSED);
		// 类型,充值
		to.setType(YeePayConstants.OperationType.RECHARGE2);
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
				
				Map<String, String> resultMap = Dom4jUtil.xmltoMap(new String(responseBody, "UTF-8"));
				
				String code = resultMap.get("code");
				String description = resultMap.get("description");
				
				if (code.equals("1")) {
					to.setRequestTime(new Date());
					to.setStatus(TrusteeshipConstants.Status.PASSED);
						ht.update(to);
					//变更余额
						//给user加锁，保证user_bill顺序更新
						User u = ht.get(User.class, holiday.getUserid(), LockMode.UPGRADE);
						UserBill ibLastest = userBillBO.getLastestBill(holiday.getUserid());
						UserBill lb = new UserBill();
						if(ibLastest == null){
							lb.setId(IdGenerator.randomUUID());
							lb.setMoney(holiday.getMoney());
							lb.setTime(new Date());
							lb.setDetail("平台活动回馈老客户转入成功 "+" 充值编号："+dateStr+YeePayConstants.RequestNoPre.REPAY2);
							lb.setType(UserBillConstants.Type.TI_BALANCE);
							lb.setTypeInfo(OperatorInfo.WITHDRAWALS_SUCCESS);
							//这个自己添加的用不了lb.setTypeInfo(OperatorInfo.WITHDRAWALS_SUCCESS);
							lb.setUser(u);
							lb.setSeqNum(0L);
							// 余额=上一条余额+money
							lb.setBalance(holiday.getMoney());
							// 最新冻结金额=上一条冻结
							lb.setFrozenMoney(0.00);
						}else{
							lb.setId(IdGenerator.randomUUID());
							lb.setMoney(holiday.getMoney());
							lb.setTime(new Date());
							lb.setDetail("平台活动回馈老客户转入成功 "+" 充值编号："+dateStr+YeePayConstants.RequestNoPre.REPAY2);
							lb.setType(UserBillConstants.Type.TI_BALANCE);
							lb.setTypeInfo(OperatorInfo.WITHDRAWALS_SUCCESS);
							lb.setUser(u);
							lb.setSeqNum(ibLastest.getSeqNum() + 1);
							// 余额=上一条余额+money
							lb.setBalance(ArithUtil.add(ibLastest.getBalance(), holiday.getMoney()));
							// 最新冻结金额=上一条冻结
							lb.setFrozenMoney(ibLastest.getFrozenMoney());
						}
						try {
							sbs.transferOut(holiday.getMoney(),OperatorInfo.WITHDRAWALS_SUCCESS, "领取现金活动充值划账：用户ID"+holiday.getUserid());
						} catch (InsufficientBalance e) {
							FacesUtil.addInfoMessage("领取成功,如没有到账请联系平台客服！");
							System.out.println("************平台账户不足*****************");
							e.printStackTrace();
							
						}
						ht.save(lb);
						return "Y";
					} else {
						to.setRequestTime(new Date());
						to.setStatus(TrusteeshipConstants.Status.REFUSED);
						ht.update(to);
						System.out.println("平台活动回馈老客户转入失败：code="+code+";description=" + description);
						log.debug("平台活动回馈老客户转入失败：code="+code+";description=" + description);
						return "N";
				}
				
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "N";
	}
	public List find(String sql){
		
		return ht.find(sql);
	}
}
