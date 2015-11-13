package com.esoft.app.protocol.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.model.UserSub;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.exclusionStrategy.UserExclusionStrategy;
import com.esoft.archer.system.controller.DictUtil;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.util.HashCrypt;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * 
 * 登陆
 *
 */
@Service("loginRequestHandler")
public class LoginRequestHandlerImpl implements RequestHandler{

	@Resource
	HibernateTemplate ht;
	@Resource
	private UserBillBO userBillBO;
	@Override
	public Out handle(In in,Out out) {
		// TODO Auto-generated method stub
		try {
			JSONObject json=new JSONObject(in.getValue());
			
			String username=json.getString("username");
			String password=json.getString("password");
			if(username!=null&&password!=null&&username.length()>0&&password.length()>0){
				password = HashCrypt.getDigestHash(password);
				List<User> list=ht.find("from User where (username=? or email=? or mobileNumber=? )and password=? ",new Object[]{username,username,username,password});
				if(list!=null&&list.size()>0){
					UserSub userSub = new UserSub();
					User user=list.get(0);
					user.setSex(DictUtil.getValue("user_sex",user.getSex()));
					userSub.setIsRealName("0");
					List<User> list1=ht.find("Select u from User u join u.roles where u.id='"+user.getId()+"'");
					for (int i = 0; i < list1.size(); i++) {
						User u = list1.get(i);
						if("INVESTOR".equals(u.getRoles().get(0).getId())){
							userSub.setIsRealName("1");
						}
					}
					//可用余额
					double balcance=userBillBO.getBalance(username);
					userSub.setAvailablebalance(balcance);
					//累计收益
					double myInvestsInterest=getInvestsInterest(username);
					userSub.setTotalIncome(myInvestsInterest);
					//BeanUtils.copyProperties(userSub, user);
					userSub.setId(user.getId());
					userSub.setEmail(user.getEmail());
					userSub.setMobileNumber(user.getMobileNumber());
					userSub.setPhoto(user.getPhoto());
					ExclusionStrategy exclusionStrategy=new UserExclusionStrategy();
					Gson gson=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setExclusionStrategies(exclusionStrategy).serializeNulls().create();
					String str=gson.toJson(userSub);
					out.encryptResult(str);
					out.sign();
					out.setResultCode(ResponseMsgKey.SUCCESS);
					out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
				}else{
					out.setResultCode(ResponseMsgKey.LOGIN_NOT_FIND);
					out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.LOGIN_NOT_FIND)));
				}
			}else{
				out.setResultCode(ResponseMsgKey.PARAMETER_INVALID);
				out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.PARAMETER_INVALID)));
			}
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
	
	/**
	 * 
	 * @param userId
	 * @return 获取个人已经投资成功的总的收益
	 */
	public double getInvestsInterest(String userId){
		StringBuilder hql =new StringBuilder("select sum(interest+defaultInterest-fee)");
		hql.append(" from InvestRepay where time is not null and invest.id in(select id");
		hql.append(" from Invest where user.id=?)");
		double money=0;
		List<Object> result=ht.find(hql.toString(),userId);
		if(result!=null&&result.get(0)!=null){
			money=(Double)result.get(0);
		}
		return money;
	}

}
