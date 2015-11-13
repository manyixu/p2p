package com.esoft.archer.user.controller;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.esoft.archer.common.controller.EntityQuery;
import com.esoft.archer.experience.model.Experienceinvestment;
import com.esoft.archer.user.UserConstants;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.util.StringManager;

@Component
@Scope(ScopeType.VIEW)
public class UserExperienceLogList extends EntityQuery<Experienceinvestment> {

	private static StringManager sm = StringManager
			.getManager(UserConstants.Package);

	@Logger
	private static Log log ;
	/**
	 * 体验金表列表查询
	 */
	public UserExperienceLogList() {
		final String[] RESTRICTIONS = { 
		"userid like #{userExperienceLogList.example.userid}",
		"etype = #{userExperienceLogList.example.etype}",
		"startdate >= #{userExperienceLogList.loginTimeStart}",
		"startdate <= #{userExperienceLogList.loginTimeEnd}"};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	private Date loginTimeStart;
	private Date loginTimeEnd;

	public Date getLoginTimeStart() {
		return loginTimeStart;
	}
	public void setLoginTimeStart(Date loginTimeStart) {
		this.loginTimeStart = loginTimeStart;
	}
	public Date getLoginTimeEnd() {
		return loginTimeEnd;
	}
	public void setLoginTimeEnd(Date loginTimeEnd) {
		this.loginTimeEnd = loginTimeEnd;
	}

	
	


}
