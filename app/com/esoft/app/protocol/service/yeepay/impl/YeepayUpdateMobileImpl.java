package com.esoft.app.protocol.service.yeepay.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
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
import com.esoft.app.protocol.util.exception.YeepayException;
import com.esoft.archer.user.model.User;
import com.esoft.core.annotations.Logger;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;

/**
 * 手机端修改绑定手机号码
 * @author Zhangwq20150831
 */
@Service("yeepayUpdateMobile")
public class YeepayUpdateMobileImpl implements RequestHandler, Serializable {

	@Logger
	static Log log;
	@Resource
	TrusteeshipOperationBO trusteeshipOperationBO;
	@Resource
	private HibernateTemplate ht;
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Out handle(In in, Out out) {
		JSONObject json;
		try {
			json = new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			User user=ht.get(User.class, userId);
			if(user==null){
				throw new YeepayException("该用户不存在");
			}
			StringBuffer content = new StringBuffer();
			content.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");		
			//商户编号
			content.append("<request platformNo='"+ YeePayConstants.Config.MER_CODE + "'>");
			// 请求流水号
			content.append("<requestNo>"+YeePayConstants.RequestNoPre.UPDATE_MOBILE + user.getId() + "</requestNo>");
			// 出款人平台用户编号
			content.append("<platformUserNo>" + user.getId() + "</platformUserNo>");
			
			// 回调通知 URL
			content.append("<callbackUrl>"
					+ YeePayConstants.ResponseWebUrl.PRE_RESPONSE_URL
					+ YeePayConstants.OperationType.UPDATE_MOBILE
					+ "</callbackUrl>");
			// 服务器通知 URL
			content.append("<notifyUrl>"
					+ YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL
					+ YeePayConstants.OperationType.UPDATE_MOBILE + "</notifyUrl>");
			content.append("</request>");
			
			log.debug(content.toString());
			// 包装参数
			Map<String, String> params = new HashMap<String, String>();
			params.put("req", content.toString());
			String sign = CFCASignUtil.sign(content.toString());
			log.debug(sign);
			params.put("sign", sign);
			TrusteeshipOperation to = new TrusteeshipOperation();
			to.setId(IdGenerator.randomUUID());
			to.setMarkId(user.getId());
			to.setOperator(user.getId());
			
			to.setRequestUrl(YeePayConstants.RequestUrl.MOBILE_UPDATE_BOUND_MOBILE);

			to.setRequestData(MapUtil.mapToString(params));
			to.setStatus(TrusteeshipConstants.Status.UN_SEND);
			to.setType(YeePayConstants.OperationType.UPDATE_MOBILE);
			to.setTrusteeship("yeepay");
			trusteeshipOperationBO.save(to);			
			log.debug("app端保存TrusteeshipOperation成功"+YeePayConstants.OperationType.UPDATE_MOBILE);
			String form=YeepayUtil.getFormStr(to.getId(), "utf-8", ht);
			System.out.println("form:"+form);
			out.encryptResult(form);
			out.sign();
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
			log.debug("app端修改手机End");
		} catch (JSONException e) {
			log.debug(e);
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		} catch(YeepayException e){
			log.debug(e);
			out.setResultCode(ResponseMsgKey.YEEPAY_ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch (Exception e) {
			log.debug(e);
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}
		
		return out;
	}

}
