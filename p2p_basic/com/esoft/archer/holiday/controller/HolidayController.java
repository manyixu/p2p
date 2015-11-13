package com.esoft.archer.holiday.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.holiday.model.HolidayTable;
import com.esoft.archer.holiday.service.HolidayServiceMapper;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.user.model.User;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.IdGenerator;

@Component
@Scope(ScopeType.REQUEST)
public class HolidayController extends EntityHome<HolidayTable> {
	@Resource
	public HolidayServiceMapper holidayServiceMapper;
	@Resource
	private LoginUserInfo loginUserInfo;
	@Resource
	HibernateTemplate ht;
	
	@Transactional(readOnly=false)
public String saveHoliday(){
		
		
		if(loginUserInfo.getLoginUserId() == null){
			FacesUtil.addInfoMessage("请登录");
			return null;//返回
		}
	//得到当前用户
	User user=ht.get(User.class, loginUserInfo.getLoginUserId());
	String a=DateUtil.DateToString(user.getRegisterTime(),"yyyy-MM-dd");
	//判断
	if(a.compareTo("2015-09-24")<=0){
		HolidayTable holiday2=new HolidayTable();
		holiday2.setUserid(user.getId());
		//是否投资过
		List investList=ht.find("from Invest where user.id='"+user.getId()+"' and status !='cancel'");
		if(investList.size()>0){
			holiday2.setMoney(20.00);
		}else{
			holiday2.setMoney(10.00);
		}
		holiday2.setId(IdGenerator.randomUUID());
		holiday2.setHstate("00");
		holiday2.setHtype("201509ZQ");//ZQ就是代表中秋的意思，以后有别的活动可以改
		holiday2.setStarttime(new Date());
		holiday2.setRmk1("中秋节日老客户回馈");
		ht.save(holiday2);
		//判断是否得到过该活动的福利
		List hoList=holidayServiceMapper.find("from HolidayTable where userid='"+holiday2.getUserid()+"' and htype='201509ZQ' and hstate = '01'");
		if(hoList.size()>0){
			FacesUtil.addInfoMessage("您已经领取过了！！！");
			//如果是手机访问
				return "pretty:myCashFlow";//返回交易记录
		}else{
			String flag = holidayServiceMapper.HolidaySendPlatformTransfer(holiday2);
			if(flag.equals("Y")){
				holiday2.setHstate("01");
				ht.saveOrUpdate(holiday2);
				FacesUtil.addInfoMessage("领取成功");
				//如果是手机访问
					return "pretty:myCashFlow";//返回交易记录
			}else{
				holiday2.setHstate("02");
				FacesUtil.addInfoMessage("领取失败");
				ht.saveOrUpdate(holiday2);
				return null;
			}
		}
	}
	FacesUtil.addInfoMessage("领取失败");
	return null;
	}
	
public String getTouZi(String uid){
	
	List investList=ht.find("from Invest where user.id='"+uid+"' and status !='cancel'");
	if(investList.size()>0){
		return "Y";
	}else{
		return "N";
	}
}
}
