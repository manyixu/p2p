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
import com.esoft.archer.experience.model.Experienceproject;
import com.esoft.archer.experience.service.ExperienceprojectMapper;
import com.google.gson.Gson;

/**
 * 
 * 手机端体验金功能项目查询
 *
 */
@Service("experienceproject")
public class ExperienceprojectImpl implements RequestHandler{
	
	@Resource
	private ExperienceprojectMapper experienceprojectMapper;
	
	
	@Override
	public Out handle(In in, Out out) {
		try{
			JSONObject json=new JSONObject(in.getValue());
			String loanId=json.getString("userId");//用户id
			//
				String hsql="from Experienceproject";
				List<Experienceproject> projectlist=experienceprojectMapper.find(hsql);
				Gson gson=new Gson();
				String str=gson.toJson(projectlist.get(0));
				
				out.encryptResult(str);
				out.sign();
				out.setResultCode(ResponseMsgKey.SUCCESS);
				out.setResultMsg("处理成功");
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg("发送失败，请确认信息正确！");
		}
		return out;
	}

}
