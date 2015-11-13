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
import com.esoft.archer.experience.model.Experiencegold;
import com.esoft.archer.experience.service.ExperiencegoldMapper;
import com.google.gson.Gson;

/**
 * 
 *  手机端体验金功能查询
 *
 */
@Service("experiencegold")
public class ExperiencegoldImpl implements RequestHandler{
	
	@Resource
	private ExperiencegoldMapper experiencegoldMapper;
	
	
	@Override
	public Out handle(In in, Out out) {
		try{
			JSONObject json=new JSONObject(in.getValue());
			String loanId=json.getString("userId");//用户id
			String hsql="from Experiencegold where userid='"+loanId+"'";
			List<Experiencegold>goldList = experiencegoldMapper.find(hsql);
			 Gson gson=new Gson();
				String str=gson.toJson(goldList.get(0));
				// 
				out.encryptResult(str);
				out.sign();
				out.setResultCode(ResponseMsgKey.ERROR);
				out.setResultMsg("发送成功！");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg("发送失败，请确认信息正确！");
			e.printStackTrace();
		}
		return out;
	}

}
