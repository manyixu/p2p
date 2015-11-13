package com.esoft.app.protocol.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.esoft.app.protocol.util.IdCardUtil;
import com.esoft.app.protocol.util.ListUtil;
import com.esoft.app.protocol.util.exception.InvestorException;
import com.esoft.archer.user.model.Role;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.UserService;

/**
 * 
 * 创建实名认证
 *
 */
@Service("investorAddRequestHandler")
public class InvestorAddRequestHandlerImpl implements RequestHandler{
	
	@Resource
	private HibernateTemplate ht;
	@Resource
	private UserService userService;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			String realname=json.getString("realname");//真实姓名
			String idCard=json.getString("idCard");//身份证号
			User user=ht.get(User.class, userId);
			if(user==null){
				throw new InvestorException("该用户编号不存在");
			}
			List<Role> roleList=user.getRoles();
			if(roleList!=null&&roleList.size()>0){
				Map<String,Role> roleMap=ListUtil.getRoleMap(roleList);
				if(roleMap.get("INVESTOR")!=null){
					throw new InvestorException("无需再实名认证");
				}
			}
			if(realname==null||realname.length()==0){
				throw new InvestorException("真实姓名不能为空");
			}
			String sex=IdCardUtil.getSex(idCard);
			if(sex==null){
				throw new InvestorException("身份证号有误");
			}
			Date birthday=IdCardUtil.getDate(idCard);
			if(birthday==null){
				throw new InvestorException("身份证号有误");
			}
			user.setRealname(realname);
			user.setIdCard(idCard);
			user.setSex(sex);
			user.setBirthday(birthday);
			userService.realNameCertification(user);
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg("实名认证成功");
		}catch(InvestorException e){
			out.setResultCode(ResponseMsgKey.INVESTOR_ERROR);
			out.setResultMsg(e.getMessage());
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
