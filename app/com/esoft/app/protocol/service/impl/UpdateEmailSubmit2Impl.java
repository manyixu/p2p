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
import org.primefaces.context.RequestContext;
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
 * 修改邮箱
 *
 */
@Service("updateEmailSubmit2")
public class UpdateEmailSubmit2Impl implements RequestHandler{
	
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
			String newEmail=json.getString("newEmail");// 邮箱
			//发送验证码
			try {
					//根据当前登录用户的id,获得当前用户
					User user=userService.getUserById(loanId);
					User userE=userService.getUserByEmail2(newEmail);
					
					if(userE!=null){
						out.encryptResult("邮箱已存在！");
						out.sign();
						out.setResultCode(ResponseMsgKey.ERROR);
						out.setResultMsg("邮箱已存在！");
					}else{
						//发送绑定新邮箱接口 、 新邮箱需要验证唯一性()    发邮件(认证码)给新邮箱
						userService.sendBindingEmail(user.getId(), newEmail);
					out.encryptResult("验证码已经发送至新邮箱！");
					out.sign();
					out.setResultCode(ResponseMsgKey.SUCCESS);
					out.setResultMsg("成功发送！");
					}
			} catch (UserNotFoundException e) {
				out.encryptResult("用户未登录！");
				out.sign();
				out.setResultCode(ResponseMsgKey.ERROR);
				out.setResultMsg("验证失败，请确认信息正确！");
			}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		out.setResultCode(ResponseMsgKey.ERROR);
		out.setResultMsg("失败，请确认信息正确！");
		e.printStackTrace();
	}
	return out;
		
	}
}
