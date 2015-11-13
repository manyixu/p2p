package com.esoft.archer.experience.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
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
import com.esoft.core.util.ArithUtil;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.Dom4jUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.loan.exception.ExistWaitAffirmInvests;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.risk.service.SystemBillService;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.exception.TrusteeshipReturnException;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.yeepay.platformtransfer.service.YeePayPlatformTransferService;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
import com.esoft.yeepay.trusteeship.exception.YeePayOperationException;
import com.esoft.yeepay.trusteeship.service.impl.YeePayOperationServiceAbs;
@Component
@Service("experienceinvestmentMapper")
public class ExperienceinvestmentMapperImpl extends
YeePayOperationServiceAbs<Experienceinvestment> implements ExperienceinvestmentMapper {

	@Resource
	private ExperienceinvestmentBO experienceinvestmentBO;
	@Resource
	HibernateTemplate ht;
	@Resource
	private UserBillBO userBillBO;
	@Resource
	TrusteeshipOperationBO trusteeshipOperationBO;
	@Resource
	YeePayPlatformTransferService yeePayPlatformTransferService;
	@Resource
	private ExperienceinvestmentMapper experienceinvestmentMapper;
	@Resource
	private ExperiencegoldMapper experiencegoldMapper;
	@Logger
	static Log log;
	@Resource
	SystemBillService sbs;
	
	@Override
	public int save(Experienceinvestment record) {
		experienceinvestmentBO.save(record);
		return 0;
	}

	@Override
	public List find(String esql) {
		return experienceinvestmentBO.find(esql);
	}

	@Override
	public int update(Experienceinvestment record) {
		experienceinvestmentBO.update(record);
		return 0;
	}
	/**
	 * 平台划款
	 * 这个测试用到了，现在没用。有用的是下面的sendPlatformTransfer2
	 * @throws ExistWaitAffirmInvests 
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public String sendPlatformTransfer(List<Experienceinvestment> eList,FacesContext fc)  throws YeePayOperationException{
		return "N";
		
	}
	/**
	 * 平台划款
	 * 
	 * @throws ExistWaitAffirmInvests 
	 */
	@Override
	@Transactional(readOnly=false)
	public void sendPlatformTransfer2()  throws YeePayOperationException, ExistWaitAffirmInvests{
		String dateStr2=DateUtil.DateToString(DateUtil.addDay(new Date(),-1), "yyyy-MM-dd");//当前日期
		String strID=IdGenerator.randomUUID();
		//当前日期与结束日期比
		String hsql="from Experienceinvestment where substring(enddate,1,10)<='"+dateStr2+"' and etype='Y'";
		log.debug(hsql);
		List<Experienceinvestment> eList=experienceinvestmentMapper.find(hsql);
		if(eList.size()==0){
			log.debug("没有到期的体验金。");
		}else{
		HttpClient client = new HttpClient();
		
		// 创建一个post方法
		PostMethod postMethod = new PostMethod(
				YeePayConstants.RequestUrl.RequestDirectUrl);
		
		if(eList.size()>0){
		StringBuffer content = new StringBuffer();
		content.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
		content.append("<request platformNo='"+YeePayConstants.Config.MER_CODE+"'>");
		content.append("<requestNo>"+strID+YeePayConstants.RequestNoPre.REPAY2+"</requestNo>");
		content.append("<userType>MERCHANT</userType>");
		content.append("<bizType>TRANSFER</bizType>");
		content.append("<platformUserNo>"+YeePayConstants.Config.MER_CODE+"</platformUserNo>");
		
		content.append("<details>");
		for (Experienceinvestment exper:eList) {
			content.append("<detail>");
			content.append("<targetUserType>MEMBER</targetUserType>");
			content.append("<targetPlatformUserNo>"+exper.getUserid()+"</targetPlatformUserNo>");
			content.append("<amount>"+exper.getProfit()+"</amount>");
			content.append("<bizType>TRANSFER</bizType>");
			content.append("</detail>");
		}
		content.append("</details>");
		
		/*content.append("<callbackUrl>"+YeePayConstants.ResponseWebUrl.PRE_RESPONSE_URL+YeePayConstants.OperationType.RECHARGE2+"</callbackUrl>");*/
		// 服务器通知 URL
		content.append("<notifyUrl>"+YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL+"/"+YeePayConstants.OperationType.RECHARGE2+"</notifyUrl>");
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
		
		// to表中的主键
		to.setId(strID);
		// 操作的唯一标识（与回调的唯一标识一致，用于查询某一条操作记录）
		to.setMarkId(strID);
		// Qr2mui67rUra操作者（如果是开户，此字段为userId；如果为充值，此字段为rechargeId）
		to.setOperator(strID);
		//url
		to.setRequestUrl(YeePayConstants.RequestUrl.RequestDirectUrl+"/"+YeePayConstants.OperationType.RECHARGE2);
		to.setResponseData(content.toString());
		to.setRequestData(MapUtil.mapToString(params));
		// 第一次保存数据库中的数据为un_send,等待发送(董改了成功)
		to.setStatus(TrusteeshipConstants.Status.SENDED);
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
				
				Map<String, String> resultMap = Dom4jUtil.xmltoMap(new String(
						responseBody, "UTF-8"));
				
				String code = resultMap.get("code");
				String description = resultMap.get("description");
				
				//TrusteeshipOperation to2 = ht.get(TrusteeshipOperation.class,strID);
				if (code.equals("1")&&to.getStatus().equals(TrusteeshipConstants.Status.SENDED)) {
					//to2.setRequestTime(new Date());
					to.setStatus(TrusteeshipConstants.Status.PASSED);
				//	ht.update(to2);
					//变更余额
					for (Experienceinvestment exper2:eList) {
						try {
							sbs.transferOut(exper2.getProfit(),OperatorInfo.WITHDRAWALS_SUCCESS, "体验金活动充值划账：用户ID"+exper2.getUserid());
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
							lb.setDetail("体验金转入成功 "+" 充值编号："+strID+YeePayConstants.RequestNoPre.REPAY2);
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
							lb.setDetail("体验金转入成功"+" 充值编号："+strID+YeePayConstants.RequestNoPre.REPAY2);
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
						
						
						
						String hsqlexp="from Experiencegold where userid='"+exper2.getUserid()+"' and projectid='"+exper2.getEid()+"'";
						log.debug(hsqlexp);
						List<Experiencegold> list = experiencegoldMapper.find(hsqlexp);
						for (int j = 0; j < list.size(); j++) {
							Experiencegold experiencegold=list.get(j);
							experiencegold.setUtype("N");
							experiencegoldMapper.update(experiencegold);
							
						}
						
						
						
					}
				} else {
					//to2.setRequestTime(new Date());
					//to2.setStatus(TrusteeshipConstants.Status.REFUSED);
					//ht.update(to2);
					System.out.println("体验金直接转账失败：code="+code+";description=" + description);
					log.debug("体验金直接转账失败：code="+code+";description=" + description);;
					to.setStatus(TrusteeshipConstants.Status.REFUSED);
				}
				ht.update(to);
				
				
				
				
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
		}
	}

	@Override
	public TrusteeshipOperation createOperation(Experienceinvestment t,
			FacesContext facesContext) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void receiveOperationPostCallback(ServletRequest request)
			throws TrusteeshipReturnException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveOperationS2SCallback(ServletRequest request,
			ServletResponse response) {
		// TODO Auto-generated method stub
		
	}

	
}