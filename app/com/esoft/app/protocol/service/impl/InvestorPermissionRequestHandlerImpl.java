package com.esoft.app.protocol.service.impl;

import java.text.SimpleDateFormat;
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
import com.esoft.app.protocol.model.UserInvestor;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.ListUtil;
import com.esoft.app.protocol.util.exception.InvestorException;
import com.esoft.app.protocol.util.exception.NoInvestorException;
import com.esoft.archer.system.controller.DictUtil;
import com.esoft.archer.user.model.Role;
import com.esoft.archer.user.model.User;
import com.google.gson.Gson;

/**
 * 
 * 验证投资权限
 *
 */
@Service("investorPermissionRequestHandler")
public class InvestorPermissionRequestHandlerImpl implements RequestHandler{
	@Resource
	HibernateTemplate ht;
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			User user=ht.get(User.class, userId);
			if(user==null){
				throw new InvestorException("该用户编号不存在");
			}
			List<Role> roleList=user.getRoles();
			if(roleList!=null&&roleList.size()>0){
				Map<String,Role> roleMap=ListUtil.getRoleMap(roleList);
				if(roleMap.get("INVESTOR")!=null){
					SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
					UserInvestor ui=new UserInvestor();
					ui.setId(user.getId());
					ui.setRealname(user.getRealname());
					ui.setSex(DictUtil.getValue("user_sex",user.getSex()));
					ui.setIdCard(user.getIdCard());
					//电话
					ui.setMobileNumber(user.getMobileNumber());
					//邮箱
					ui.setEmail(user.getEmail());
					String birthDayStr="";
					if(user.getBirthday()!=null){
						birthDayStr=format.format(user.getBirthday());
					}
					ui.setBirthday(birthDayStr);
					out.encryptResult(new Gson().toJson(ui));
					out.sign();
					out.setResultCode(ResponseMsgKey.SUCCESS);
					out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
				}else{
					throw new NoInvestorException();
				}
			}else{
				throw new NoInvestorException();
			}
			
		}catch(NoInvestorException e){
			out.setResultCode(ResponseMsgKey.NO_INVESTOR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.NO_INVESTOR)));
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
