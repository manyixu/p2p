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
import com.esoft.archer.user.model.Role;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.UserService;
import com.esoft.jdp2p.invest.model.Invest;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * 获取项目投资列表
 *
 */
@Service("investRequestHandler")
public class InvestRequestHandlerImpl implements RequestHandler{
	
	@Resource
	private HibernateTemplate ht;
	
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		Session session=ht.getSessionFactory().openSession();
		try{
			JSONObject json=new JSONObject(in.getValue());
			String loanId=json.getString("loanId");//用户编号
			String hql = "from Invest where loan.id=?";
			Query query=session.createQuery(hql.toString());
			query.setString(0, loanId);
			List<Invest> list=query.list();
			ExclusionStrategy exclusionStrategy=new UserExclusionStrategy();
			Gson gson=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setExclusionStrategies(exclusionStrategy).serializeNulls().create();
			if(list!=null&&list.size()>0){
				//Gson gson=builder.create();
				List<InvestSub> subList=new ArrayList<InvestSub>();
				for(int i=0;i<list.size();i++){
					InvestSub sub=InvestUtil.getInvestSub(list.get(i));
					subList.add(sub);
				}
				String str=gson.toJson(subList);
				System.out.println(str);
				out.encryptResult(str);
				out.sign();
			}else{
				out.encryptResult(null);
				out.sign();
			}
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
		}finally{
			if(session!=null){
				session.flush();
				session.clear();
				session.close();
			}
		}
		return out;
	}

}
