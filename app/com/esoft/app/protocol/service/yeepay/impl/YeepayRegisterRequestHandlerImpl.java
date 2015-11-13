package com.esoft.app.protocol.service.yeepay.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import com.esoft.app.protocol.util.IdCardUtil;
import com.esoft.app.protocol.util.ListUtil;
import com.esoft.app.protocol.util.YeepayUtil;
import com.esoft.app.protocol.util.exception.RegistException;
import com.esoft.app.protocol.util.exception.YeepayException;
import com.esoft.archer.user.model.InvestorPermissionCount;
import com.esoft.archer.user.model.Role;
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
 * 
 * @author Hch
 * 易宝实名认证注册
 */
@Service("yeepayRegisterRequestHandler")
public class YeepayRegisterRequestHandlerImpl implements RequestHandler,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4549340630303275026L;
	@Logger
	static Log log;
	@Resource
	TrusteeshipOperationBO trusteeshipOperationBO;
	@Resource
	private HibernateTemplate ht;
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			String realName=json.getString("realname");//真实姓名
			String idCardNo=json.getString("idCard");//身份证号
			String mobile=json.getString("mobile");//手机号
			//优化代码String email=json.getString("email");//邮箱
			if(userId==null||userId.length()==0){
				throw new YeepayException("用户编号不能为空");
			}
			if(realName==null||realName.length()==0){
				throw new YeepayException("真实姓名不能为空");
			}
			if(idCardNo==null||idCardNo.length()==0){
				throw new YeepayException("身份证号不能为空");
			}
			List<User> list=ht.find("from User u left join u.roles role where u.idCard=? and role.id=?",idCardNo,"INVESTOR");
			//List<User> list=ht.find("from User where idCardNo=?",idCardNo);
			if(list!=null&&list.size()>0){
				throw new RegistException("身份证号已存在");
			}
			if(mobile==null||mobile.length()==0){
				throw new YeepayException("手机号不能为空");
			}
			/*优化代码
			 * if(email==null||email.length()==0){
				throw new YeepayException("邮箱不能为空");
			}*/
			User user=ht.get(User.class, userId);
			if(user==null){
				throw new YeepayException("该用户不存在");
			}
			Map<String, Role> roleMap=ListUtil.getRoleMap(user.getRoles());
			if(roleMap.get("INVESTOR")!=null){
				throw new YeepayException("该用户已经实名认证");
			}
			String sex=IdCardUtil.getSex(idCardNo);
			if(sex==null){
				throw new YeepayException("身份证号有误");
			}
			Date birthday=IdCardUtil.getDate(idCardNo);
			if(birthday==null){
				throw new YeepayException("身份证号有误");
			}
			user.setRealname(realName);
			user.setIdCard(idCardNo);
			user.setMobileNumber(mobile);
			//优化代码user.setEmail(email);
			user.setSex(sex);
			user.setBirthday(birthday);
			ht.update(user);
			InvestorPermissionCount ipc = ht.get(InvestorPermissionCount.class, user.getId());
			if(ipc==null){
				ipc = new InvestorPermissionCount();
				ipc.setId(user.getId());
				ipc.setCount(1);
			}else{
				ipc.setCount(ipc.getCount()+1);
			}
			ht.saveOrUpdate(ipc);
			StringBuffer content = new StringBuffer();
			content.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
			content.append("<request platformNo='"
					+ YeePayConstants.Config.MER_CODE + "'>");
			// 商户平台会员标识:会员在商户平台唯一标识
			content.append("<platformUserNo>" + userId + "</platformUserNo>");
			// 请求流水号
			content.append("<requestNo>"+YeePayConstants.RequestNoPre.CREATE_ACCOUNT + userId + "</requestNo>");
			// 会员真实姓名
			content.append("<realName>" + realName + "</realName>");
			// 身份证类型
			content.append("<idCardType>G2_IDCARD</idCardType>");
			// 身份证号
			content.append("<idCardNo>" + idCardNo + "</idCardNo>");
			// 手机号
			content.append("<mobile>" + mobile + "</mobile>");
			// 邮箱
			//优化代码content.append("<email>" + email + "</email>");
			// 回调通知 URL
			content.append("<callbackUrl>"
					+ YeePayConstants.ResponseWebUrl.PRE_RESPONSE_URL
					+ YeePayConstants.OperationType.CREATE_ACCOUNT
					+ "</callbackUrl>");
			// 服务器通知 URL
			content.append("<notifyUrl>"
					+ YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL
					+ YeePayConstants.OperationType.CREATE_ACCOUNT + "</notifyUrl>");
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
			to.setMarkId(userId);
			to.setOperator(userId);
			to.setRequestUrl(YeePayConstants.RequestUrl.MOBILE_CREATE_YEEPAY_ACCT);
			//to.setRequestUrl(YeePayConstants.RequestUrl.CREATE_YEEPAY_ACCT);
			to.setRequestData(MapUtil.mapToString(params));
			to.setStatus(TrusteeshipConstants.Status.UN_SEND);
			to.setType(YeePayConstants.OperationType.CREATE_ACCOUNT);
			to.setTrusteeship("yeepay");
			trusteeshipOperationBO.save(to);
			log.debug("app端保存TrusteeshipOperation成功"+YeePayConstants.OperationType.CREATE_ACCOUNT);
			String form=YeepayUtil.getFormStr(to.getId(), "utf-8", ht);
			out.encryptResult(form);
			out.sign();
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
			log.debug("实名认证End");
		}catch(JSONException e){
			log.error(e);
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch(YeepayException e){
			log.error(e);
			out.setResultCode(ResponseMsgKey.YEEPAY_ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch (Exception e) {
			log.error(e);
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}
		return out;
	}
}
