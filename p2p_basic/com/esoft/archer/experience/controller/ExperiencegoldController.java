package com.esoft.archer.experience.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.CommonConstants;
import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.experience.model.ExperienceCoupons;
import com.esoft.archer.experience.model.Experiencegold;
import com.esoft.archer.experience.service.ExperiencegoldMapper;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.IdGenerator;

@Component
@Scope(ScopeType.VIEW)
public class ExperiencegoldController extends EntityHome<Experiencegold> {
	@Resource
	private LoginUserInfo loginUser;
	@Resource
	private ExperiencegoldMapper experiencegoldMapper;
	@Resource
	private HibernateTemplate ht;
	private String captcha;//验证码
	@Autowired
	private HttpServletRequest request;
	@Override
	@Transactional(readOnly=false)
	public String save() {
		
		Object ccp = request.getSession().getAttribute(CommonConstants.CaptchaFlag.CAPTCHA_SESSION);
		System.out.println(ccp);
		System.out.println(captcha);
		if(ccp==null||"".equals(ccp)){
			//兑换码不存在
			FacesUtil.addInfoMessage("验证码已失效.");
			if (FacesUtil.isMobileRequest()) {
				return "pretty:weixinapp_user_experience";
			}
			return "pretty:experience";
		}
		if(captcha==null||"".equals(captcha)){
			//兑换码不存在
			FacesUtil.addInfoMessage("请填写验证码.");
			if (FacesUtil.isMobileRequest()) {
				return "pretty:weixinapp_user_experience";
			}
			return "pretty:experience";
		}else if(!ccp.toString().toLowerCase().equals(captcha.toLowerCase())){
			//兑换码不存在
			FacesUtil.addInfoMessage("验证码已输入错误.");
			if (FacesUtil.isMobileRequest()) {
				return "pretty:weixinapp_user_experience";
			}
			return "pretty:experience";
		}
		/**
		 * 
		 * 立即兑换的时候，去验证体验券是否存在，是否过期，是否使用
		 * 
		 * 
		 * 
		 */
		//1.输入码  查询是否有这个码
		
		//2.去用户体验金表experiencegold表查询是否有值，有值 代表用过了
		//3.将这个对话吗 保存到用户体验金表experiencegold表,修改状态为已使用
		System.out.println(this.getInstance().getRmk23());
		ExperienceCoupons ec = ht.get(ExperienceCoupons.class, this.getInstance().getRmk23());
		if(ec==null){
			//兑换码不存在
			FacesUtil.addInfoMessage("兑换码不存在.");
			if (FacesUtil.isMobileRequest()) {
				return "pretty:weixinapp_user_experience";
			}
			return "pretty:experience";
		}
		if("Y".equals(ec.getEtype())){
			//兑换码已使用
			FacesUtil.addInfoMessage("兑换码已使用.");
			if (FacesUtil.isMobileRequest()) {
				return "pretty:weixinapp_user_experience";
			}
			return "pretty:experience";
		}
		if("S".equals(ec.getEtype())){
			//兑换码已过期
			FacesUtil.addInfoMessage("兑换码已过期.");
			if (FacesUtil.isMobileRequest()) {
				return "pretty:weixinapp_user_experience";
			}
			return "pretty:experience";
		}
		//查询自己的卷
		String hsql="from Experiencegold where userid='"+loginUser.getLoginUserId()+"' and rmk23 like '%"+ec.getId().substring(0, 4)+"%'";
		List experiencegold=experiencegoldMapper.find(hsql);
		if(experiencegold!=null&&experiencegold.size()>0){
			//兑换码已过期
			FacesUtil.addInfoMessage("一个项目只能兑换一次体验券.");
			if (FacesUtil.isMobileRequest()) {
				return "pretty:weixinapp_user_experience";
			}
			return "pretty:experience";
		}
		/*1.	id	序号		int(23)	pk	
		2.		userId	用户id				
		3.		projectid	项目id				
		4.		projectname	项目名（除新手体验金外）				
		5.		money	体验金				
		6.		pday	体验天数				
		7.		pinterestrate	年利率				
		8.		profit	到期收益				
		9.		etime	发送日期				
		10.		utype	状态	y:有效
							Z:进行中
							N:无效
							S:过期			
		11.		rmk21	结束日期				
		12.		rmk22	类型	A1：新手体验金
		B1：活动体验卷			
		13.		rmk23	兑换码				*/
	
		Date now = new Date();
		//兑换后的体验券在15天以后失效
		Date enddate = DateUtil.addDay(now, 7);
		Experiencegold eg = new Experiencegold();
		eg.setId(IdGenerator.randomUUID());
		eg.setUserid(loginUser.getLoginUserId());
		eg.setProjectid(ec.getExperienceproject().getId());
		eg.setProjectname(ec.getExperienceproject().getPname());
		eg.setMoney(ec.getMoney());
		eg.setPday(ec.getPday());
		eg.setPinterestrate(ec.getPinterestrate());
		eg.setProfit(ec.getExperienceproject().getProfit());
		eg.setEtime(ec.getStartdate());
		eg.setUtype("Y");
		eg.setRmk21(enddate);
		eg.setRmk22("B1");
		eg.setRmk23(ec.getId());
		ht.save(eg);
		ec.setEtype("Y");
		ht.update(ec);
		FacesUtil.addInfoMessage("兑换成功.");
		if (FacesUtil.isMobileRequest()) {
			return "pretty:weixinapp_user_experience";
		}
		return "pretty:experience";
	}


	public String getCaptcha() {
		return captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}
	
	
	
}
