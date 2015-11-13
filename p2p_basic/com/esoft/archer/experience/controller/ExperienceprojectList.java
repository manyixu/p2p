package com.esoft.archer.experience.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.esoft.archer.common.controller.EntityQuery;
import com.esoft.archer.experience.model.Experienceproject;
import com.esoft.core.annotations.ScopeType;

@Component
@Scope(ScopeType.VIEW)
public class ExperienceprojectList extends EntityQuery<Experienceproject>  implements Serializable{
	private static final long serialVersionUID = -1350682013319140386L;

	public ExperienceprojectList() {
		final String[] RESTRICTIONS = { "id like #{experienceprojectList.example.id}",
				"pname like #{experienceprojectList.example.pname}" };
		ArrayList<String> a = new ArrayList(Arrays.asList(RESTRICTIONS));
		setRestrictionExpressionStrings(a);
	}

}
