package com.esoft.archer.experience.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.experience.model.ExperienceCoupons;
import com.esoft.archer.experience.model.Experiencegold;
import com.esoft.archer.experience.service.ExperiencegoldMapper;
import com.esoft.archer.user.model.InvestorPermissionCount;
import com.esoft.core.annotations.Logger;
import com.esoft.core.util.DateUtil;

@Service("experiencegoldMapper")
public class ExperiencegoldMapperImpl implements ExperiencegoldMapper {

	@Resource
	private ExperiencegoldBO experiencegoldBO;
	@Resource
	HibernateTemplate ht;
	@Logger
	static Log log;

	@Override
	public int save(Experiencegold record) {
		experiencegoldBO.save(record);
		return 0;
	}

	@Override
	public List find(String userid) {
		// TODO Auto-generated method stub
		return experiencegoldBO.find(userid);
	}

	@Override
	public int update(Experiencegold record) {
		experiencegoldBO.update(record);
		return 0;
	}

	/**
	 * 定时查询没用的体验金，变更为失效
	 */
	@Override
	@Transactional(readOnly=false)
	public void timeupdate() {
		try {
			System.out.println("=============================体验金==========================");
			//删除实名认证次数  开始
			List<InvestorPermissionCount> ilist = ht.find("from InvestorPermissionCount");
			ht.deleteAll(ilist);
			//删除实名认证次数  结束
			String dateStr=DateUtil.DateToString(DateUtil.addDay(new Date(),-1), "yyyy-MM-dd");
			String hql="from Experiencegold where utype='Y' and substring(rmk21,1,10)<='"+dateStr+"'";
			log.debug(hql);
			List<Experiencegold> goldList=experiencegoldBO.find(hql);
			String hql1="from ExperienceCoupons where etype='N' and substring(enddate,1,10)<='"+dateStr+"'";
			log.debug(hql1);
			List<ExperienceCoupons> gcouList=ht.find(hql1);
			for (ExperienceCoupons experienceCoupons : gcouList) {
				experienceCoupons.setEtype("S");
				ht.update(experienceCoupons);
			}
			for (Experiencegold experiencegold : goldList) {
				experiencegold.setUtype("S");
				experiencegoldBO.update(experiencegold);
				ExperienceCoupons experienceCoupons = ht.get(ExperienceCoupons.class, experiencegold.getRmk23());
				experienceCoupons.setEtype("S");
				ht.update(experienceCoupons);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
}