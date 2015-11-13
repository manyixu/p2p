package com.esoft.app.protocol.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.exception.RegistException;
import com.esoft.archer.common.exception.AuthInfoAlreadyActivedException;
import com.esoft.archer.common.exception.AuthInfoOutOfDateException;
import com.esoft.archer.common.exception.NoMatchingObjectsException;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.UserService;
import com.esoft.core.util.IdGenerator;

/**
 *
 * 注册接口
 *
 */
@Service("registRequestHandler")
public class RegistRequestHandlerImpl implements RequestHandler{

	@Resource
	private UserService userService;
	@Resource
	HibernateTemplate ht;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			System.out.println(in.getValue());
			JSONObject json=new JSONObject(in.getValue());
			//优化注册String username=json.getString("username");//用户名,必填
			//优化注册String email=json.getString("email");//邮箱,必填
			//String username=json.getString("mobileNumber");//用户名,必填
			String password=json.getString("password");//密码
			String mobileNumber=json.getString("mobileNumber");//手机号,必填
			String authCode=json.getString("authCode");//验证码
			String referrer=json.getString("referrer");//推荐人
			User user=new User();
			//user.setUsername(username);
			user.setPassword(password);
			//优化注册user.setEmail(email);
			user.setMobileNumber(mobileNumber);
			
			
			/*if(username!=null&&username.length()>0){
				List<User> list=ht.find("from User where username=?",username);
				if(list!=null&&list.size()>0){
					throw new RegistException("用户名已经存在");
				}
			}else{
				throw new RegistException("用户名不能为空");
			}*/
			if(referrer!=null&&referrer.length()>0){
				List<User> list=ht.find("from User where mobileNumber=?",referrer);
				if(list==null||list.size()==0){
					throw new RegistException("推荐人不存在");
				}
				user.setReferrer(list.get(0).getId());
			}
			/*优化注册
			 * if(email!=null&&email.length()>0){
				List<User> list=ht.find("from User where email=?",email);
				if(list!=null&&list.size()>0){
					throw new RegistException("邮箱已经存在");
				}
			}else{
				throw new RegistException("邮箱不能为空");
			}*/
			if(mobileNumber!=null&&mobileNumber.length()>0){
				List<User> list=ht.find("from User where mobileNumber=?",mobileNumber);
				if(list!=null&&list.size()>0){
					throw new RegistException("联系方式已经存在");
				}
			}else{
				throw new RegistException("联系方式不能为空");
			}
			userService.registerByMobileNumber(user, authCode,referrer);
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg(String.valueOf(ResponseMsgKey.SUCCESS));
		}catch(RegistException e){
			System.out.println(e.getMessage());
			out.setResultCode(ResponseMsgKey.REGIST_ERROR);
			out.setResultMsg(e.getMessage());
		}catch (NoMatchingObjectsException e) {
			out.setResultCode(ResponseMsgKey.REGIST_ERROR);
			out.setResultMsg("输入的验证码错误，验证失败！");
		} catch (AuthInfoOutOfDateException e) {
			out.setResultCode(ResponseMsgKey.REGIST_ERROR);
			out.setResultMsg("验证码已过期！");
		} catch (AuthInfoAlreadyActivedException e) {
			out.setResultCode(ResponseMsgKey.REGIST_ERROR);
			out.setResultMsg("验证码已被使用！");
		}catch(JSONException e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch(IllegalArgumentException e){
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
