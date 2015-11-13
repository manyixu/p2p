package com.esoft.app.protocol.service.impl;

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
import com.esoft.app.protocol.util.ListUtil;
import com.esoft.app.protocol.util.exception.BankCardException;
import com.esoft.app.protocol.util.exception.NoInvestorException;
import com.esoft.app.protocol.util.exclusionStrategy.BankCardExclusionStrategy;
import com.esoft.archer.user.model.Role;
import com.esoft.archer.user.model.User;
import com.esoft.jdp2p.bankcard.BankCardConstants.BankCardStatus;
import com.esoft.jdp2p.bankcard.model.BankCard;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * 
 * 银行卡查询
 *
 */
@Service("bankCardRequestHandler")
public class BankCardRequestHandlerImpl implements RequestHandler{

	@Resource
	HibernateTemplate ht;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			String op=json.getString("op");//操作（query：查询）
			GsonBuilder builder=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").serializeNulls();
			User user=ht.get(User.class, userId);
			if(user!=null){
			}else{
				throw new BankCardException("该用户编号不存在");
			}
			List<Role> roleList=user.getRoles();
			if(roleList!=null&&roleList.size()>0){
				Map<String,Role> roleMap=ListUtil.getRoleMap(roleList);
				if(roleMap.get("INVESTOR")!=null){
					
				}else{
					throw new NoInvestorException();
				}
			}else{
				throw new NoInvestorException();
			}
			if("query".equals(op)){
				String hql="from BankCard where user.id=? and status=?";
				List<BankCard> list=ht.find(hql,new Object[]{userId,BankCardStatus.BINDING});
				if(list!=null&&list.size()>0){
					ExclusionStrategy exclusionStrategy=new BankCardExclusionStrategy();
					Gson gson=builder.setExclusionStrategies(exclusionStrategy).create();
					String str=gson.toJson(list);
					out.encryptResult(str);
					out.sign();
					out.setResultCode(ResponseMsgKey.SUCCESS);
					out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
				}else{
					throw new BankCardException("该用户不存在银行卡号");
				}
			}else{
				throw new BankCardException("op的值不在范围内");
			}
		}catch(NoInvestorException e){
			out.setResultCode(ResponseMsgKey.NO_INVESTOR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.NO_INVESTOR)));
		}catch(BankCardException e){
			out.setResultCode(ResponseMsgKey.BANK_CARD_ERROR);
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
