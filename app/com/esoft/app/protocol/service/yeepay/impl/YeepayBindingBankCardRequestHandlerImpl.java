package com.esoft.app.protocol.service.yeepay.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
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
import com.esoft.app.protocol.util.YeepayUtil;
import com.esoft.app.protocol.util.exception.BankCardException;
import com.esoft.app.protocol.util.exception.WithdrawException;
import com.esoft.archer.config.service.ConfigService;
import com.esoft.archer.user.UserConstants;
import com.esoft.archer.user.UserBillConstants.OperatorInfo;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.annotations.Logger;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.DateStyle;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.bankcard.model.BankCard;
import com.esoft.jdp2p.loan.model.WithdrawCash;
import com.esoft.jdp2p.risk.FeeConfigConstants.FeePoint;
import com.esoft.jdp2p.risk.FeeConfigConstants.FeeType;
import com.esoft.jdp2p.risk.service.impl.FeeConfigBO;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.jdp2p.user.service.WithdrawCashService;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
/**
 *
 *@author hch
 *绑定银行卡
 *
 */
@Service("yeepayBindingBankCardRequestHandler")
public class YeepayBindingBankCardRequestHandlerImpl implements RequestHandler,Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6710613560227897924L;
	public static String regix_requestNo = "_";
	@Resource
	private HibernateTemplate ht;
	@Logger
	private static Log log;
	@Resource
	private TrusteeshipOperationBO trusteeshipOperationBO;
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Out handle(In in, Out out) {
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			String op = json.getString("op");//用户操作，unbinding解绑。。。binding绑卡;
			if(userId==null||userId.length()==0){
				throw new BankCardException("用户编号不能为空");
			}
			User user=ht.get(User.class, userId);
			if(user==null){
				throw new BankCardException("该用户编号不存在");
			}
			BankCard bankCard = new BankCard();
			bankCard.setUser(user);
			TrusteeshipOperation to = new TrusteeshipOperation();
			if(op.equals("unbinding")){
				// String resquestNo = IdGenerator.randomUUID();
				String hql = "from BankCard bc where bc.user.id = ? and bc.status = ?";
				List l = ht.find(hql, new String[]{userId,"VERIFIED"});
				BankCard bc = null;
				if(l.size() > 0){
					bc = (BankCard) l.get(0);
				}else{
					FacesUtil.addErrorMessage("未找到银行卡信息。");
					return null;
				}
				StringBuffer content = new StringBuffer();
				content.append("<?xml version='1.0' encoding='utf-8'?>");
				// 商户编号:商户在易宝唯一标识
				content.append("<request platformNo='"
						+ YeePayConstants.Config.MER_CODE + "'>");
				// 商户平台会员标识:会员在商户平台唯一标识
				content.append("<platformUserNo>" + userId
						+ "</platformUserNo>");
				// 请求流水号 银行卡的 id
				content.append("<requestNo>"+ YeePayConstants.RequestNoPre.UNBINDING_YEEPAY_BANKCARD + bc.getId() + "</requestNo>");
				// 页面回跳URL
				content.append("<callbackUrl>"
						+ YeePayConstants.ResponseWeiXinWebUrl.PRE_RESPONSE_URL
						+ YeePayConstants.OperationType.UNBINDING_YEEPAY_BANKCARD
						+ "</callbackUrl>");
				// 服务器通知 URL:服务器通知 URL
				content.append("<notifyUrl>"
						+ YeePayConstants.ResponseWeiXinS2SUrl.PRE_RESPONSE_URL
						+ YeePayConstants.OperationType.UNBINDING_YEEPAY_BANKCARD
						+ "</notifyUrl>");
				content.append("</request>");
				// 包装参数
				Map<String, String> params = new HashMap<String, String>();
				params.put("req", content.toString());
				String sign = CFCASignUtil.sign(content.toString());
				params.put("sign", sign);
				log.debug(content.toString());
				log.debug(sign);
				// 保存本地
				to.setId(IdGenerator.randomUUID());
				to.setMarkId(bc.getId());
				to.setOperator(bc.getId());
				to.setRequestUrl(YeePayConstants.RequestUrl.UNBINDING_YEEPAY_BANKCARD);
				to.setRequestData(MapUtil.mapToString(params));
				to.setStatus(TrusteeshipConstants.Status.UN_SEND);
				to.setType(YeePayConstants.OperationType.UNBINDING_YEEPAY_BANKCARD);
				to.setTrusteeship("yeepay");
				trusteeshipOperationBO.save(to);
			}else if(op.equals("binding")){
				/** caijinmin 修改requestNo 201504221255 begin*/
				String resquestNo_random = IdGenerator.randomUUID();
				/** caijinmin 修改requestNo 201504221255 end*/
				StringBuffer content = new StringBuffer();
				content.append("<?xml version='1.0' encoding='utf-8'?>");
				// 商户编号:商户在易宝唯一标识
				content.append("<request platformNo='"
						+ YeePayConstants.Config.MER_CODE + "'>");
				// 商户平台会员标识:会员在商户平台唯一标识
				content.append("<platformUserNo>" + bankCard.getUser().getId()
						+ "</platformUserNo>");
				// 请求流水号 银行卡的 id
				/** caijinmin 修改requestNo 201504221255 begin*/
				content.append("<requestNo>"+ YeePayConstants.RequestNoPre.BINDING_YEEPAY_BANKCARD + bankCard.getUser().getId() + regix_requestNo + resquestNo_random.substring(0,13)
						+ "</requestNo>");
				/** caijinmin 修改requestNo 201504221255 end*/
				// 页面回跳URL
				content.append("<callbackUrl>"
						+ YeePayConstants.ResponseWebUrl.PRE_RESPONSE_URL
						+ YeePayConstants.OperationType.BINDING_YEEPAY_BANKCARD
						+ "</callbackUrl>");
				// 服务器通知 URL:服务器通知 URL
				content.append("<notifyUrl>"
						+ YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL
						+ YeePayConstants.OperationType.BINDING_YEEPAY_BANKCARD
						+ "</notifyUrl>");
				// 提现金额:如果传入此，将使用该金额提现且用户将不可更改
				content.append("</request>");
				
				// 包装参数
				Map<String, String> params = new HashMap<String, String>();
				params.put("req", content.toString());
				String sign = CFCASignUtil.sign(content.toString());
				params.put("sign", sign);
				log.debug(content.toString());
				log.debug(sign);
				// 保存本地
				to.setId(bankCard.getUser().getId());
				to.setMarkId(bankCard.getUser().getId());
				to.setOperator(bankCard.getUser().getId());
				to.setRequestUrl(YeePayConstants.RequestUrl.MOBILE_BINDING_YEEPAY_BANKCARD);
				to.setRequestData(MapUtil.mapToString(params));
				to.setStatus(TrusteeshipConstants.Status.UN_SEND);
				to.setType(YeePayConstants.OperationType.BINDING_YEEPAY_BANKCARD);
				to.setTrusteeship("yeepay");
				trusteeshipOperationBO.save(to);
				ht.evict(bankCard);
			}else{
				log.debug("op的值不在范围内");
				throw new BankCardException("op的值不在范围内");
			}
			log.debug("app端保存TrusteeshipOperation成功"+to.getType());
			String form=YeepayUtil.getFormStr(to.getId(), "utf-8", ht);
			System.out.println("form:"+form);
			out.encryptResult(form);
			out.sign();
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
			log.debug("app端绑卡End");
		}catch(BankCardException e){
			log.debug(e);
			out.setResultCode(ResponseMsgKey.BANK_CARD_ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch(JSONException e){
			log.debug(e);
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			log.debug(e);
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.ERROR)));
			e.printStackTrace();
		}
		return out;
	}
	/**
	 * 生成id
	 * 
	 * @return
	 */
	private String generateId() {
		String gid = DateUtil.DateToString(new Date(), DateStyle.YYYYMMDD);
		String hql = "select withdraw from WithdrawCash withdraw where withdraw.id = (select max(withdrawM.id) from WithdrawCash withdrawM where withdrawM.id like ?)";
		List<WithdrawCash> contractList = ht.find(hql, gid + "%");
		Integer itemp = 0;
		if (contractList.size() == 1) {
			WithdrawCash withdrawCash = contractList.get(0);
			ht.lock(withdrawCash, LockMode.UPGRADE);
			String temp = withdrawCash.getId();
			temp = temp.substring(temp.length() - 6);
			itemp = Integer.valueOf(temp);
		}
		itemp++;
		gid += String.format("%08d", itemp);
		return gid;
	}
}
