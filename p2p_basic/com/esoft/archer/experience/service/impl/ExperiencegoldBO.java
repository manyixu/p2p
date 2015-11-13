package com.esoft.archer.experience.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.esoft.archer.experience.model.Experiencegold;
import com.esoft.archer.experience.service.ExperiencegoldMapper;

@Service("experiencegoldBO")
public class ExperiencegoldBO {

	@Resource
	HibernateTemplate ht;
	
	public int save(Experiencegold experiencegold){
		ht.save(experiencegold);
		return 0;
	}
	public List<Experiencegold> find(String userid) {
		
		return ht.find(userid);
	}

	
	public int update(Experiencegold record) {
		ht.update(record);
		return 0;
	}
}