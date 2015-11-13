package com.esoft.archer.experience.controller;

import java.io.Serializable;
import java.util.Arrays;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.esoft.archer.common.controller.EntityQuery;
import com.esoft.archer.experience.model.Experiencegold;
import com.esoft.archer.experience.service.ExperiencegoldMapper;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.ScopeType;


@Component
@Scope(ScopeType.VIEW)
public class ExperiencegoldList3 extends EntityQuery<Experiencegold> implements Serializable{
	/*@Resource
	private LoginUserInfo loginUser;
	@Resource
	private ExperiencegoldMapper experiencegoldMapper;


	private static final String lazyModelCountHql = "select count(distinct exper) from Experiencegold exper";
	private static final String lazyModelHql = "select distinct exper from Experiencegold exper";
	
public List<Experiencegold> experiencegoldList(String utype){
	String hsql="from Experiencegold where rmk22 !='A1' and utype='"+utype+"' and userid='"+loginUser.getLoginUserId()+"'";
	return experiencegoldMapper.find(hsql);
}

	public ExperiencegoldList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		final String[] RESTRICTIONS = {
				"exper.rmk22 != 'A1'",
				"exper.userid = #{experiencegoldList.example.userid}"};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}*/
	
}
