package com.esoft.archer.experience.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.esoft.archer.experience.model.Experienceproject;
import com.esoft.archer.experience.service.ExperienceprojectMapper;

@Service("experienceprojectMapper")
public class ExperienceprojectMapperImpl implements ExperienceprojectMapper {

	@Resource
	private ExperienceprojectMapperBO experienceprojectMapperBO;
	
	@Override
	public int save(Experienceproject record) {
		experienceprojectMapperBO.save(record);
		return 0;
	}

	@Override
	public List find(String esql) {
		return experienceprojectMapperBO.find(esql);
	}

	@Override
	public int update(Experienceproject record) {
		experienceprojectMapperBO.update(record);
		return 0;
	}

}