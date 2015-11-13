package com.esoft.app.protocol.service.yeepay.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.hibernate.ObjectNotFoundException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.ListUtil;
import com.esoft.app.protocol.util.YeepayUtil;
import com.esoft.app.protocol.util.exception.YeepayException;
import com.esoft.archer.config.service.ConfigService;
import com.esoft.archer.user.model.Role;
import com.esoft.archer.user.model.User;
import com.esoft.core.annotations.Logger;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.bankcard.controller.BankCardList;
import com.esoft.jdp2p.bankcard.model.BankCard;
import com.esoft.jdp2p.loan.model.Recharge;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.jdp2p.user.service.RechargeService;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
/**
 * 
 * @author Hch
 * 易宝充值
 *
 */
@Service("yeepayRechargeRequestHandler")
public class YeepayRechargeRequestHandlerImpl implements RequestHandler,Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3711392112723057820L;
	@Resource
	private HibernateTemplate ht;
	@Resource
	private RechargeService rechargeService;
	@Resource
	private ConfigService configService;
	@Resource
	private BankCardList bankCardList;
	@Logger
	private static Log log;
	@Resource
	private TrusteeshipOperationBO trusteeshipOperationBO;
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			Double actualMoney=json.getDouble("actualMoney");//真实金额
			Double fee=json.getDouble("fee");//充值费用
			String rechargeWay="rechargeWay";//充值方式(易宝:YeePay;)
			if(userId==null||userId.length()==0){
				throw new YeepayException("用户编号不能为空");
			}
			User user=ht.get(User.class, userId);
			if(user==null){
				throw new YeepayException("该用户序号不存在");
			}
			/*List<BankCard> blist = bankCardList.getBankCardListbyUser(userId);
			if(blist!=null&&blist.size()>0){
				if("BOCO".equals(blist.get(0).getBankNo())){
					out.sign();
					out.setResultCode(ResponseMsgKey.ERROR);
					out.setResultMsg("因交通银行系统升级维护，暂时无法提供提现功能！将于9月27日24:00以后恢复。如有疑问，请联系客服。");
					return out;
				}
			}*/
			Map<String, Role> roleMap=ListUtil.getRoleMap(user.getRoles());
			if(roleMap.get("INVESTOR")==null){
				throw new YeepayException("该用户未实名认证");
			}
			Recharge recharge=new Recharge();
			recharge.setUser(user);
			recharge.setActualMoney(actualMoney);
			recharge.setFee(fee);
			recharge.setRechargeWay(rechargeWay);
	        // 保存一个充值订单
			String id = rechargeService.createRechargeOrder(recharge);
			log.debug(id);
			// 根据id获取刚刚保存的充值订单
			recharge = ht.get(Recharge.class, recharge.getId());
		    log.debug(user.getUsername());
			recharge.setUser(user);

			StringBuffer content = new StringBuffer();
			content.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
			content.append("<request platformNo='"+YeePayConstants.Config.MER_CODE+"'>");

			content.append("<platformUserNo>" + recharge.getUser().getId()
					+ "</platformUserNo>");
			content.append("<requestNo>"+YeePayConstants.RequestNoPre.RECHARGE + recharge.getId() + "</requestNo>");
			content.append("<amount>" + recharge.getActualMoney() + "</amount>");
			
			// 谁付手续费 1：平台支付 2：用户支付
			String feeType = "1";
			try {
				feeType = configService.getConfigValue("yeepay.recharge_fee_type");
			} catch (ObjectNotFoundException e) {
				if (log.isDebugEnabled()) {
					log.debug(e);
				}
			}
			if ("2".equals(feeType)) {
				content.append("<feeMode>" + "USER" + "</feeMode>");					
			} else {
				content.append("<feeMode>" + "PLATFORM" + "</feeMode>");					
			}

			content.append("<callbackUrl>"
					+ YeePayConstants.ResponseWebUrl.PRE_RESPONSE_URL
					+ YeePayConstants.OperationType.RECHARGE
					+ "</callbackUrl>");
			// 服务器通知 URL
			content.append("<notifyUrl>"
					+ YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL
					+ YeePayConstants.OperationType.RECHARGE
					+ "</notifyUrl>");
			content.append("</request>"); 
			log.debug(content.toString());
			// 生成cfca签名
			String sign = CFCASignUtil.sign(content.toString());

			// 包装参数
			Map<String, String> params = new HashMap<String, String>();
			params.put("req", content.toString());
			params.put("sign", sign);
			log.debug("createRechargeOrder的" + params);

			// 保存到本地数据库中
			TrusteeshipOperation to = new TrusteeshipOperation();
			// to表中的主键
			to.setId(IdGenerator.randomUUID());
			// 操作的唯一标识（与回调的唯一标识一致，用于查询某一条操作记录）
			to.setMarkId(recharge.getId());
			// Qr2mui67rUra
			to.setOperator(recharge.getId());
			to.setRequestUrl(YeePayConstants.RequestUrl.MOBILE_RECHARGE);
			to.setRequestData(MapUtil.mapToString(params));
			// 第一次保存数据库中的数据为un_send,等待发送
			to.setStatus(TrusteeshipConstants.Status.UN_SEND);
			// 类型,充值
			to.setType(YeePayConstants.OperationType.RECHARGE);
			// 托管方
			to.setTrusteeship("yeepay");
			// 将to对象保存到数据库中
			trusteeshipOperationBO.save(to);
			log.debug("app端保存TrusteeshipOperation成功"+YeePayConstants.OperationType.RECHARGE);
			String form=YeepayUtil.getFormStr(to.getId(), "utf-8", ht);
			out.encryptResult(form);
			out.sign();
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
			log.debug("app端充值End");
		}catch(JSONException e){
			log.error(e);
			e.printStackTrace();
		}catch(YeepayException e){
			log.error(e);
			e.printStackTrace();
		}catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
		return out;
	}
}
