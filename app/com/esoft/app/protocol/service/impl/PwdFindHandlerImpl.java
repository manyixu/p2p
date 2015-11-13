package com.esoft.app.protocol.service.impl;

import javax.annotation.Resource;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.archer.common.CommonConstants;
import com.esoft.archer.common.exception.AuthInfoAlreadyActivedException;
import com.esoft.archer.common.exception.AuthInfoOutOfDateException;
import com.esoft.archer.common.exception.NoMatchingObjectsException;
import com.esoft.archer.common.service.AuthService;

/**
 *
 * 密码找回接口
 * 
 */
@Service("pwdFindHandler")
public class PwdFindHandlerImpl implements RequestHandler {

	//信息认证service，生成和验证认证信息。例如：手机短信认证，邮箱认证等
	@Resource
	private AuthService authService;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub   
		try {
			JSONObject json = new JSONObject(in.getValue());
			String phone=json.getString("mobileNumber");//手机号码
			String code=json.getString("authCode");//验证码
			String userId=json.getString("userId");//用户编号
			authService.verifyAuthInfo(userId,phone,code
			,CommonConstants.AuthInfoType.FIND_LOGIN_PASSWORD_BY_MOBILE);
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
		}catch (AuthInfoOutOfDateException e) {
			out.setResultCode(ResponseMsgKey.PWD_ERROR);
			out.setResultMsg("认证码已过期！");
		}catch (NoMatchingObjectsException e) {
			out.setResultCode(ResponseMsgKey.PWD_ERROR);
			out.setResultMsg("输入验证码错误，请重新输入！");
		}catch (AuthInfoAlreadyActivedException e) {
			out.setResultCode(ResponseMsgKey.PWD_ERROR);
			out.setResultMsg("认证码已经使用！");
		} catch (JSONException e) {
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps
					.get(ResponseMsgKey.ERROR)));
			e.printStackTrace();
		}
		return out;
	}

}
