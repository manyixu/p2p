package com.esoft.archer.experience.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.esoft.archer.experience.model.Experienceproject;

@Service("experienceprojectMapperBO")
public class ExperienceprojectMapperBO {
	@Resource
	HibernateTemplate ht;
	
    int save(Experienceproject record){
    	ht.save(record);
    	return 0;
    }

    List find(String esql){
    	return ht.find(esql);
    }

    int update(Experienceproject record){
    	ht.update(record);
    	return 0;
    }
}