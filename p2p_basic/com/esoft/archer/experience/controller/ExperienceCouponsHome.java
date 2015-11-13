package com.esoft.archer.experience.controller;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.experience.model.ExperienceCoupons;
import com.esoft.archer.experience.model.Experienceproject;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.ArithUtil;
import com.esoft.core.util.IdGenerator;

@Component
@Scope(ScopeType.VIEW)
public class ExperienceCouponsHome extends EntityHome<ExperienceCoupons> implements
		java.io.Serializable {
	@Resource
	HibernateTemplate ht;
	/**
	 * 
	 */
	public final static String experienceCouponsListUrl = "/admin/experiencegold/experienceCouponsList";
	@Override
	@Transactional(readOnly = false)
	public String save() {
		ExperienceCoupons experienceCoupons = this.getInstance();
		Experienceproject experienceproject = new Experienceproject();
		experienceproject.setId(IdGenerator.randomUUID());
		experienceproject.setPname(experienceCoupons.getPname());//项目名
		experienceproject.setPday(experienceCoupons.getPday());//天数
		experienceproject.setPinterestrate(experienceCoupons.getPinterestrate());//利率
		double profit = ArithUtil.round(experienceCoupons.getPinterestrate()/100 * experienceCoupons.getMoney() / 365 * experienceCoupons.getPday(), 2);
		experienceproject.setProfit(profit);
		experienceproject.setMoney(experienceCoupons.getMoney());//金额
		experienceproject.setRmk1("B1");
		ht.save(experienceproject);
		int pronumber = experienceCoupons.getPronumber();
		for (int i = 0; i < pronumber; i++) {
			ExperienceCoupons experienceCoupons1 = new ExperienceCoupons();
			//定义一个循环，判断生成的券是否存在，不存在 跳出while循环
			while (true) {
				String random = getStringRandom(6);
				experienceCoupons1.setId((experienceCoupons.getCouponsNum()+random).toUpperCase());
				ExperienceCoupons l = ht.get(ExperienceCoupons.class, experienceCoupons1.getId());
				if(l==null){
					break;
				}
			}
			experienceCoupons1.setEnddate(experienceCoupons.getEnddate());
			experienceCoupons1.setEtype("N");
			experienceCoupons1.setExperienceproject(experienceproject);
			experienceCoupons1.setMoney(experienceCoupons.getMoney());
			experienceCoupons1.setPday(experienceCoupons.getPday());
			experienceCoupons1.setPinterestrate(experienceCoupons.getPinterestrate());
			experienceCoupons1.setPname(experienceCoupons.getPname());
			experienceCoupons1.setPronumber(pronumber);
			experienceCoupons1.setStartdate(experienceCoupons.getStartdate());
			experienceCoupons1.setCouponsNum(experienceCoupons.getId());
			ht.save(experienceCoupons1);
		}
		FacesUtil.addInfoMessage("优惠券保存成功！");
		return FacesUtil.redirect(experienceCouponsListUrl);
	}
	

		//生成随机数字和字母,
		public String getStringRandom(int length) {
			
			String val = "";
			Random random = new Random();
			
			//参数length，表示生成几位随机数
			for(int i = 0; i < length; i++) {
				
				String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
				//输出字母还是数字
				if( "char".equalsIgnoreCase(charOrNum) ) {
					//输出是大写字母还是小写字母
					int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
					val += (char)(random.nextInt(26) + temp);
				} else if( "num".equalsIgnoreCase(charOrNum) ) {
					val += String.valueOf(random.nextInt(10));
				}
			}
			return val;
		}
		public static void  main(String[] args) {  
			ExperienceCouponsHome test = new ExperienceCouponsHome();  
	        //测试  
	        System.out.println(test.getStringRandom(8).toUpperCase());  
	    }  
		
	
}
