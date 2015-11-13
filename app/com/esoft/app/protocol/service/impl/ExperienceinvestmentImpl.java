package com.esoft.app.protocol.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.archer.experience.model.Experienceinvestment;
import com.esoft.archer.experience.service.ExperienceinvestmentMapper;
import com.google.gson.Gson;

/**
 * 
 *  手机端体验金功能投资后的查询
 *
 */
@Service("experienceinvestment")
public class ExperienceinvestmentImpl implements RequestHandler{
	
	@Resource
	private ExperienceinvestmentMapper experienceinvestmentMapper;
	
	
	@Override
	public Out handle(In in, Out out) {
		try{
			JSONObject json=new JSONObject(in.getValue());
			String loanId=json.getString("userId");//用户id
			String hsql="from Experienceinvestment where userid='"+loanId+"'";
			List<Experienceinvestment> tmentlist=experienceinvestmentMapper.find(hsql);
			Gson gson=new Gson();
			if(tmentlist.size()>0){
			String str=gson.toJson(tmentlist.get(0));
			out.encryptResult(str);
			out.sign();
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg("投资成功");
			}else{
				out.encryptResult("");
				out.sign();
				out.setResultCode(ResponseMsgKey.ERROR);
				out.setResultMsg("用户没有投资信息");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg("失败，请确认参数！");
			e.printStackTrace();
		}
		return out;
	}

}
