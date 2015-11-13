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
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.user.exception.UserNotFoundException;
import com.esoft.archer.user.model.Role;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.UserService;
import com.esoft.jdp2p.invest.model.Invest;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 *  修改邮箱获得验证码
 *
 */
@Service("updateEmail")
public class UpdateEmailImpl implements RequestHandler{
	
	@Resource
	private HibernateTemplate ht;
	@Resource
	private UserService userService;
	@Resource
	private LoginUserInfo loginUserInfo;
	
	
	@Override
	public Out handle(In in, Out out) {
		try{
			JSONObject json=new JSONObject(in.getValue());
			String loanId=json.getString("userId");//用户id
			//发送验证码
			try {
				User user=userService.getUserById(loanId);
				//发邮件(认证码)给原邮箱
				userService.sendChangeBindingEmail(user.getId(), user.getEmail());
				out.encryptResult("验证码已经发送至邮箱！");
				out.sign();
				out.setResultCode(ResponseMsgKey.SUCCESS);
				out.setResultMsg("处理成功");
			} catch (UserNotFoundException e) {
				// 发送不成功
				out.encryptResult("用户id为空！");
				out.sign();
				out.setResultCode(ResponseMsgKey.ERROR);
				out.setResultMsg("发送失败，请确认用户信息正确！");
			}
			/*Gson gson=new Gson();
			String str=gson.toJson(subList);*/
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg("发送失败，请确认信息正确！");
			e.printStackTrace();
		}
		return out;
	}

}
