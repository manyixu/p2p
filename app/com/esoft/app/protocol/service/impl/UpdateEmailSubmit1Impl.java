package com.esoft.app.protocol.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.InvestSub;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.IdCardUtil;
import com.esoft.app.protocol.util.InvestUtil;
import com.esoft.app.protocol.util.ListUtil;
import com.esoft.app.protocol.util.exception.InvestorException;
import com.esoft.app.protocol.util.exclusionStrategy.UserExclusionStrategy;
import com.esoft.archer.common.CommonConstants;
import com.esoft.archer.common.exception.AuthInfoAlreadyActivedException;
import com.esoft.archer.common.exception.AuthInfoOutOfDateException;
import com.esoft.archer.common.exception.NoMatchingObjectsException;
import com.esoft.archer.common.service.AuthService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.user.exception.UserNotFoundException;
import com.esoft.archer.user.model.Role;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.UserService;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.jdp2p.invest.model.Invest;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 *  修改邮箱匹配验证码
 *
 */
@Service("updateEmailSubmit1")
public class UpdateEmailSubmit1Impl implements RequestHandler{
	
	@Resource
	private HibernateTemplate ht;
	@Resource
	private UserService userService;
	@Resource
	private LoginUserInfo loginUserInfo;
	@Resource
	private AuthService authService;
	
	
	@Override
	public Out handle(In in, Out out) {
		try{
			JSONObject json=new JSONObject(in.getValue());
			String loanId=json.getString("userId");//用户id
			String authCode=json.getString("authCode");// 认证码
			//发送验证码
			try {
				User user=userService.getUserById(loanId);
				//验证认证码        CommonConstants.AuthInfoType.CHANGE_BINDING_EMAIL(类型) 修改绑定邮箱
				authService.verifyAuthInfo(user.getId(), user.getEmail(), authCode, CommonConstants.AuthInfoType.CHANGE_BINDING_EMAIL);
				// 认证码
				authCode = null;
				out.encryptResult("验证码正确！");
				out.sign();
				out.setResultCode(ResponseMsgKey.SUCCESS);
				out.setResultMsg("处理成功");
				
			} catch (UserNotFoundException e) {
				out.encryptResult("用户未登录！");
				out.sign();
				out.setResultCode(ResponseMsgKey.ERROR);
				out.setResultMsg("请确认信息正确！");
			} catch (NoMatchingObjectsException e) {
				out.encryptResult("输入验证码错误，请重新输入！");
				out.sign();
				out.setResultCode(ResponseMsgKey.ERROR);
				out.setResultMsg("验证码错误，请确认信息正确！");
			} catch (AuthInfoOutOfDateException e) {
				out.encryptResult("验证码已过期！");
				out.sign();
				out.setResultCode(ResponseMsgKey.ERROR);
				out.setResultMsg("验证码错误，请确认信息正确！");
			} catch (AuthInfoAlreadyActivedException e) {
				out.encryptResult("认证码已经使用！");
				out.sign();
				out.setResultCode(ResponseMsgKey.ERROR);
				out.setResultMsg("验证码错误，请确认信息正确！");
			}
			/*Gson gson=new Gson();
			String str=gson.toJson(subList);*/
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg("失败，请确认信息正确！");
			e.printStackTrace();
		}
		return out;
	}

}
