package com.esoft.app.protocol.service.impl;

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
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.IdCardUtil;
import com.esoft.app.protocol.util.ListUtil;
import com.esoft.app.protocol.util.exception.InvestorException;
import com.esoft.app.protocol.util.exclusionStrategy.UserExclusionStrategy;
import com.esoft.archer.user.model.Role;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.UserService;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.repay.model.LoanRepay;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * 还款计划
 *
 */
@Service("repayPlanRequestHandler")
public class RepayPlanRequestHandlerImpl implements RequestHandler{
	
	@Resource
	private HibernateTemplate ht;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			Session session=ht.getSessionFactory().openSession();
			JSONObject json=new JSONObject(in.getValue());
			String projectId=json.getString("projectId");//用户编号
			String hql = "from InvestRepay where invest.id=?";
			Query query=session.createQuery(hql.toString());
			query.setString(0, projectId);
			List<LoanRepay> list=query.list();
			//LoanRepay loanRepay = ht.get(LoanRepay.class, projectId);
			ExclusionStrategy exclusionStrategy=new UserExclusionStrategy();
			Gson gson=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setExclusionStrategies(exclusionStrategy).serializeNulls().create();
			String str=gson.toJson(list);
			out.encryptResult(str);
			out.sign();
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg("处理成功");
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
