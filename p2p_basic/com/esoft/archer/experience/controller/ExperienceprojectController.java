package com.esoft.archer.experience.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.experience.model.Experienceproject;
import com.esoft.archer.experience.service.ExperienceprojectMapper;
import com.esoft.core.annotations.ScopeType;

@Component
@Scope(ScopeType.VIEW)
public class ExperienceprojectController extends EntityHome<Experienceproject>  {
	@Resource
	private ExperienceprojectMapper experienceprojectMapper;
	@Autowired
	private HttpServletRequest request;
	
	@Override
	@Transactional(readOnly=false)
	public String save() {
		System.out.println("*******************");
		String aaa="";
		aaa="1231231";
		return null;
	}
	
	
}
