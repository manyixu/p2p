package com.esoft.app.protocol.service.impl;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang.time.DateUtils;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.archer.experience.model.Experiencegold;
import com.esoft.archer.experience.model.Experienceinvestment;
import com.esoft.archer.experience.model.Experienceproject;
import com.esoft.archer.experience.service.ExperiencegoldMapper;
import com.esoft.archer.experience.service.ExperienceinvestmentMapper;
import com.esoft.archer.experience.service.ExperienceprojectMapper;
import com.esoft.core.jsf.el.SpringSecurityELLibrary;

/**
 * 
 *  手机端体验金功能保存投资信息
 *
 */
@Service("experienceinvestmentSave")
public class ExperienceinvestmentSaveImpl implements RequestHandler{
	
	@Resource
	private ExperiencegoldMapper experiencegoldMapper;
	@Resource
	private ExperienceprojectMapper experienceprojectMapper;
	@Resource
	private ExperienceinvestmentMapper experienceinvestmentMapper;
	
	
	@Override
	@Transactional(readOnly=false)
	public Out handle(In in, Out out) {
		try{
			JSONObject json=new JSONObject(in.getValue());
			String loanId=json.getString("userId");//用户id
				
			String hsql="from Experiencegold where userid='"+loanId+"' and utype='Y'";
			Experiencegold experiencegold=(Experiencegold) experiencegoldMapper.find(hsql).get(0);
			
			String hsql2="from Experienceproject";
			Experienceproject experienceproject=(Experienceproject) experienceprojectMapper.find(hsql2).get(0);
			//boolean flag=SpringSecurityELLibrary.ifAllGranted("INVESTOR");
			//if(flag){
				if(experiencegold.getMoney()>=experienceproject.getMoney()){
				Date now = new Date();
				Experienceinvestment experienceinvestment=new Experienceinvestment();
				experienceinvestment.setUserid(experiencegold.getUserid());
				experienceinvestment.setPday(experienceproject.getPday());
				experienceinvestment.setEname(experienceproject.getId());
				experienceinvestment.setStartdate(now);
		        experienceinvestment.setMoney(experiencegold.getMoney());
				experienceinvestment.setEnddate(DateUtils.addDays(now, experienceproject.getPday()));
				experienceinvestment.setProfit(experienceproject.getProfit());
				experienceinvestment.setPinterestrate(experienceproject.getPinterestrate());
				experienceinvestment.setEtype("Y");
				experienceinvestmentMapper.save(experienceinvestment);
				//修改体验金状态
				experiencegold.setUtype("N");
				experiencegoldMapper.update(experiencegold);
				
				out.encryptResult("成功投资体验金！");
				out.sign();
				out.setResultCode(ResponseMsgKey.SUCCESS);
				out.setResultMsg("处理成功");
			}else{
				
				System.out.println("*****************体验金小于项目金额！！！******************");
				out.encryptResult("用户id为空！");
				out.sign();
				out.setResultCode(ResponseMsgKey.ERROR);
				out.setResultMsg("发送失败，请确认用户信息正确！");
			}
			/*}else{
				out.encryptResult("验证码已经发送至邮箱！");
				out.sign();
				out.setResultCode(ResponseMsgKey.ERROR);
				out.setResultMsg("失败，还没有实名认证");
			}*/
			/*Gson gson=new Gson();
			String str=gson.toJson(subList);*/
			
		} catch (Exception e) {
			out.encryptResult("失败！");
			out.sign();
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg("失败，传参错误");
		}
		return out;
	}

}
