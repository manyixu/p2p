package com.esoft.app.protocol.service.impl;

import java.util.List;

import javax.annotation.Resource;

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
import com.esoft.app.protocol.util.exception.PwdException;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.UserService;
import com.esoft.core.util.HashCrypt;

/**
 *
 * 修改密码接口
 *
 */
@Service("pwdUpdateHandler")
public class PwdUpdateHandlerImpl implements RequestHandler{

	@Resource
	HibernateTemplate ht;
	@Resource
	private UserService userService;
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			String oldPwd=json.getString("oldPwd");//旧密码
			String newPwd=json.getString("newPwd");//新密码
			String newPwd1=json.getString("newPwd1");//确认新密码
			if(userId!=null&&userId.length()>0){
			String hql="from User where id=? and password=?";
				List<User> list=ht.find(hql,new Object[]{userId,HashCrypt.getDigestHash(oldPwd)});
				if(list!=null&&list.size()>0){
					User user=list.get(0);
					if(newPwd!=null&&newPwd.length()>0){
						if(newPwd.equals(newPwd1)){
							user.setPassword(HashCrypt.getDigestHash(newPwd));
							ht.update(user);
							out.sign();
							out.setResultCode(ResponseMsgKey.SUCCESS);
							out.setResultMsg("修改密码成功");
						}else{
							throw new PwdException("密码不一致");
						}
					}else{
						throw new PwdException("新密码为空");
					}
				}else{
					throw new PwdException("原密码有误");
				}
			}else{
				throw new PwdException("用户编号为空");
			}
		}catch(PwdException e){
			System.out.println(e.getMessage());
			out.setResultCode(ResponseMsgKey.PWD_ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch(JSONException e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch(Exception e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.ERROR)));
			e.printStackTrace();
		}
		return out;
	}

}
