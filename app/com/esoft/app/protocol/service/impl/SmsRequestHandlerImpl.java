package com.esoft.app.protocol.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.exception.SmsException;
import com.esoft.archer.common.CommonConstants;
import com.esoft.archer.common.service.AuthService;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.UserService;
import com.esoft.core.util.DateStyle;
import com.esoft.core.util.DateUtil;
import com.esoft.jdp2p.message.MessageConstants;
import com.esoft.jdp2p.message.model.UserMessageTemplate;
import com.esoft.jdp2p.message.service.impl.MessageBO;
/**
 *
 * 短信接口
 *
 */
@Service("smsRequestHandler")
public class SmsRequestHandlerImpl implements RequestHandler{

	@Resource
	private UserService userService;
	@Resource
	HibernateTemplate ht;
	@Resource
	private MessageBO messageBO;
	//信息认证service，生成和验证认证信息。例如：手机短信认证，邮箱认证等
	@Resource
	private AuthService authService;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String phone=json.getString("mobileNumber");//手机号,必选
			String op=json.getString("op");//操作,必选;(zhuce:注册;zhaohui:找回密码;update_phone1:修改手机号第一步;update_phone2:修改手机号第二步;)
			String userId=json.getString("userId");//用户编号,有些操作必须有,注册,找回不需要值
			if(op!=null&&op.length()>0){
				if("zhuce".equals(op)){
					User user = new User();
					user.setMobileNumber(phone);
					List<User> user_list = ht.findByExample(user);
					if(user_list.size()>0) {
						out.setResultCode(ResponseMsgKey.ERROR);
						out.setResultMsg("该手机已注册!");
						return out;
						
					}else {
						userService.sendRegisterByMobileNumberSMS(phone);
					}
					
				}else if("zhaohui".equals(op)){
					List<User> list=ht.find("from User where mobileNumber=?",phone);
					if(list!=null&&list.size()>0){
						User user=list.get(0);
						// 发送手机验证码
						Map<String, String> params = new HashMap<String, String>();
						params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
						params.put("authCode", authService.createAuthInfo(user.getId(), phone, null, CommonConstants.AuthInfoType.FIND_LOGIN_PASSWORD_BY_MOBILE).getAuthCode());
						messageBO.sendSMS(ht.get(UserMessageTemplate.class, MessageConstants.UserMessageNodeId.FIND_LOGIN_PASSWORD_BY_MOBILE + "_sms"), params, phone);
						StringBuilder str=new StringBuilder("{");
						str.append("\"userId\":\"").append(user.getUsername()).append("\",");
						str.append("\"mobileNumber\":\"").append(user.getMobileNumber()).append("\"");
						str.append("}");
						out.encryptResult(str.toString());
						out.sign();
					}else{
						throw new SmsException("该手机号未被注册");
					}
				}else if("update_phone1".equals(op)){
					if(userId!=null&&userId.length()>0&&phone!=null&&phone.length()>0){
						userService.sendChangeBindingMobileNumberSMS(userId,phone);
					}else{
						throw new SmsException("userId或mobileNumber的值不能为空!");
					}
				}else if("update_phone2".equals(op)){
					if(userId!=null&&userId.length()>0&&phone!=null&&phone.length()>0){
						userService.sendBindingMobileNumberSMS(userId,phone);
					}else{
						throw new SmsException("userId或mobileNumber的值不能为空!");
					}
				}else{
					throw new SmsException("op参数值异常");
				}
			}else{
				throw new SmsException("op参数值异常");
			}
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg("发送成功");
		}catch(SmsException e){
			out.setResultCode(ResponseMsgKey.SMS_ERROR);
			out.setResultMsg(e.getMessage());
		}catch(JSONException e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}
		return out;
	}

}
