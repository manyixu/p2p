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
import com.esoft.app.protocol.util.exception.PhoneUpdateException;
import com.esoft.archer.common.CommonConstants;
import com.esoft.archer.common.exception.AuthInfoAlreadyActivedException;
import com.esoft.archer.common.exception.AuthInfoOutOfDateException;
import com.esoft.archer.common.exception.NoMatchingObjectsException;
import com.esoft.archer.common.service.AuthService;
import com.esoft.archer.user.exception.UserNotFoundException;
import com.esoft.archer.user.service.UserService;

/**
 * 
 * 修改绑定手机
 *
 */
@Service("phoneUpdateHandler")
public class PhoneUpdateHandlerImpl implements RequestHandler{

	@Resource
	private AuthService authService;
	@Resource
	private UserService userService;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			String phone=json.getString("phone");//手机号
			String code=json.getString("code");//验证码
			String op=json.getString("op");//操作(1:第一步;2:第二部)
			if(op!=null&&op.length()>0){
				if("1".equals(op)){
					authService.verifyAuthInfo(userId, phone,code,
					CommonConstants.AuthInfoType.CHANGE_BINDING_MOBILE_NUMBER);
					out.setResultCode(ResponseMsgKey.SUCCESS);
					out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
				}else if("2".equals(op)){
					userService.bindingMobileNumber(userId,phone,code);
					out.setResultCode(ResponseMsgKey.SUCCESS);
					out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
				}else{
					throw new PhoneUpdateException("op参数的值不在范围内");
				}
			}else{
				throw new PhoneUpdateException("op参数为空");
			}
		}catch(PhoneUpdateException e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
		}catch (AuthInfoOutOfDateException e) {
			out.setResultCode(ResponseMsgKey.PHONE_UP_ERROR);
			out.setResultMsg("认证码已过期！");
		}catch (UserNotFoundException e) {
			out.setResultCode(ResponseMsgKey.PHONE_UP_ERROR);
			out.setResultMsg("用户未登录");
		}catch (NoMatchingObjectsException e) {
			out.setResultCode(ResponseMsgKey.PHONE_UP_ERROR);
			out.setResultMsg("输入验证码错误，请重新输入！");
		}catch (AuthInfoAlreadyActivedException e) {
			out.setResultCode(ResponseMsgKey.PWD_ERROR);
			out.setResultMsg("认证码已经使用！");
		}catch(JSONException e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.ERROR)));
			e.printStackTrace();
		}
		return out;
	}

}
