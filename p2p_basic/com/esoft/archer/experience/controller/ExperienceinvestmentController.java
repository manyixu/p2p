package com.esoft.archer.experience.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cfca.util.pki.cipher.Session;
import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.experience.model.Experiencegold;
import com.esoft.archer.experience.model.Experienceinvestment;
import com.esoft.archer.experience.model.Experienceproject;
import com.esoft.archer.experience.service.ExperiencegoldMapper;
import com.esoft.archer.experience.service.ExperienceinvestmentMapper;
import com.esoft.archer.experience.service.ExperienceprojectMapper;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.user.model.User;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.el.SpringSecurityELLibrary;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.jdp2p.loan.exception.ExistWaitAffirmInvests;
import com.esoft.yeepay.platformtransfer.model.YeePayPlatformTransfer;

@Component
@Scope(ScopeType.VIEW)
public class ExperienceinvestmentController extends EntityHome<Experienceinvestment>{

	@Resource
	private ExperienceinvestmentMapper experienceinvestmentMapper;
	@Autowired
	private HttpServletRequest request;
	@Resource
	private LoginUserInfo loginUser;
	@Resource
	private ExperiencegoldMapper experiencegoldMapper;
	@Resource
	private ExperienceprojectMapper experienceprojectMapper;
	
	
	public String loanMoney;
	
	@Override
	@Transactional(readOnly=false)
	public String save() {
		String hsql="from Experiencegold where rmk22='A1' and userid='"+loginUser.getLoginUserId()+"' and utype='Y'";
		Experiencegold experiencegold=(Experiencegold) experiencegoldMapper.find(hsql).get(0);
		
		String hsql2="from Experienceproject";
		Experienceproject experienceproject=(Experienceproject) experienceprojectMapper.find(hsql2).get(0);
		boolean flag=SpringSecurityELLibrary.ifAllGranted("INVESTOR");
		if(flag){
		if(experiencegold.getMoney()>=experienceproject.getMoney()){
			Date now = DateUtil.StringToDate(DateUtil.getDate(new Date()));
			Experienceinvestment experienceinvestment=new Experienceinvestment();
			experienceinvestment.setId(IdGenerator.randomUUID());
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
			experiencegold.setUtype("Z");
			experiencegoldMapper.update(experiencegold);
			return "Y";
		}else{
			
			System.out.println("*****************体验金小于项目金额！！！******************");
			return "X";
		}
		}
		try {
			FacesUtil.getHttpServletResponse().sendRedirect(
					FacesUtil.getCurrentAppUrl() + "/user/get_investor_permission");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "/user/get_investor_permission";
	}
	/**
	 * 使用特色卷
	 * @param pid  体验券  type  类型
	 * @return
	 */
	@Transactional(readOnly=false)
	public String save2(String pid,String type) {
		String hsql = "";
		if("A1".equals(type)){
			//查询自己的卷
			hsql="from Experiencegold where rmk22 = '"+type+"' and userid='"+loginUser.getLoginUserId()+"'";
		}else{
			//查询自己的卷
			hsql="from Experiencegold where rmk23 = '"+pid+"' and userid='"+loginUser.getLoginUserId()+"'";
		}
		List experiencegoldList=experiencegoldMapper.find(hsql);
		Experiencegold experiencegold = (Experiencegold)experiencegoldList.get(0);
		
		/*//查询项目信息
		String hsql2="from Experienceproject where id='"+experiencegold.getProjectid()+"'";
		Experienceproject experienceproject=(Experienceproject) experienceprojectMapper.find(hsql2).get(0);
		
		//查询一下是否投资过该项目
		String hsql3="from Experienceinvestment where ename='"+experiencegold.getProjectid()+"' and userid='"+loginUser.getLoginUserId()+"' " ;
		List list=experienceinvestmentMapper.find(hsql3);*/
		
		/*if(list !=null && list.size()>0){
				FacesUtil.addInfoMessage("您已经参加过此投的优惠活动了！！！同一个活动只能参加一次哦！");
				if (FacesUtil.isMobileRequest()) {
					return "pretty:weixinapp_user_experience";
				}
				return "pretty:experience";
			}*/
			//if(experiencegold.getMoney()>=experienceproject.getMoney()){
				//Date now = new Date();
				Date now = DateUtil.StringToDate(DateUtil.getDate(new Date()));
				Experienceinvestment experienceinvestment=new Experienceinvestment();
				experienceinvestment.setId(IdGenerator.randomUUID());
				experienceinvestment.setUserid(experiencegold.getUserid());
				experienceinvestment.setPday(experiencegold.getPday());
				experienceinvestment.setEname(experiencegold.getProjectname());
				experienceinvestment.setEid(experiencegold.getProjectid());
				experienceinvestment.setStartdate(now);
				
				/*Calendar c = Calendar.getInstance();
				c.setTime(new Date());   //设置当前日期
				int sum=Integer.parseInt(experiencegold.getPday());
				c.add(Calendar.DATE,sum); //日期加 ，当天日期开始算
				Date date = c.getTime(); //结果
*/				
				experienceinvestment.setMoney(experiencegold.getMoney());
				experienceinvestment.setEnddate(DateUtil.addDay(now, experiencegold.getPday()));
				experienceinvestment.setProfit(experiencegold.getProfit());
				experienceinvestment.setPinterestrate(experiencegold.getPinterestrate());
				experienceinvestment.setEtype("Y");
				experienceinvestmentMapper.save(experienceinvestment);
				//修改体验金卷状态
				experiencegold.setUtype("Z");
				experiencegoldMapper.update(experiencegold);
				FacesUtil.addInfoMessage("投资成功");
				if (FacesUtil.isMobileRequest()) {
					return "pretty:weixinapp_user_experience";
				}
				return "pretty:experience";
			/*}else{
				FacesUtil.addInfoMessage("体验金小于项目金额");
				System.out.println("*****************体验金小于项目金额！！！******************");
				if (FacesUtil.isMobileRequest()) {
					return "pretty:weixinapp_user_experience";
				}
				return "pretty:experience";
			}*/
	}
	
	
	
	//没用
	@Transactional(readOnly=false)
	public String getLoanMoney() {
		String dateStr=DateUtil.DateToString(new Date(), "yyyy-MM-dd");//当前日期
		String hsql="from Experienceinvestment where DATE_FORMAT(enddate,'%Y-%m-%d')='"+dateStr+"' and etype='Y'";
		List<Experienceinvestment> eList=experienceinvestmentMapper.find(hsql);
		String flage="";
		try {
			flage=experienceinvestmentMapper.sendPlatformTransfer(eList,FacesContext.getCurrentInstance());
		} catch (ExistWaitAffirmInvests e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(flage.equals("Y")){
		for (int i = 0; i < eList.size(); i++) {
			Experienceinvestment experienceinvestment=eList.get(i);
			//修改状态
			experienceinvestment.setEtype("N");
			experienceinvestmentMapper.update(experienceinvestment);
			
		}
		}else{
			System.out.println("直接打款出错。");
		}
		return "1";
	}
	
	//没用
	@Transactional(readOnly=false)
	public void loanMoneys() {
		String dateStr=DateUtil.DateToString(new Date(), "yyyy-MM-dd");//当前日期
		String hsql="from Experienceinvestment where DATE_FORMAT(enddate,'%Y-%m-%d')='"+dateStr+"' and etype='Y'";
		List<Experienceinvestment> eList=experienceinvestmentMapper.find(hsql);
		String flage="";
		try {
			flage=experienceinvestmentMapper.sendPlatformTransfer(eList,FacesContext.getCurrentInstance());
		} catch (ExistWaitAffirmInvests e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(flage.equals("Y")){
		for (int i = 0; i < eList.size(); i++) {
			Experienceinvestment experienceinvestment=eList.get(i);
			//修改状态
			experienceinvestment.setEtype("N");
			experienceinvestmentMapper.update(experienceinvestment);
			//等待2秒
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}else{
			System.out.println("直接打款出错。");
		}
		//return "1";
	}

	public void setLoanMoney(String loanMoney) {
		this.loanMoney = loanMoney;
	}

}
