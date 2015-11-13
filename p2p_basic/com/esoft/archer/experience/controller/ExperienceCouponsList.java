package com.esoft.archer.experience.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Hibernate;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.esoft.archer.common.controller.EntityQuery;
import com.esoft.archer.experience.model.ExperienceCoupons;
import com.esoft.core.annotations.ScopeType;

@Component
@Scope(ScopeType.VIEW)
public class ExperienceCouponsList extends EntityQuery<ExperienceCoupons> implements java.io.Serializable {
	private static final long serialVersionUID = -1350682013319140386L;
	@Resource
	HibernateTemplate ht;
	public ExperienceCouponsList() {
		final String[] RESTRICTIONS = { "id like #{experienceCouponsList.example.id}",
				"pname like #{experienceCouponsList.example.pname}" };
		ArrayList<String> a = new ArrayList(Arrays.asList(RESTRICTIONS));
		setRestrictionExpressionStrings(a);
	}
	
	public Long getExperienceCouponsCountByProject(String projectId){
		List<Long> is = ht.find("select count(*) from ExperienceCoupons e where e.experienceproject.id =?",projectId);
		return is.get(0);
	}
	
}
