package com.esoft.archer.version.controller;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.esoft.archer.common.controller.EntityQuery;
import com.esoft.archer.version.model.Version;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
@Component
@Scope(ScopeType.REQUEST)
public class VersionList  extends EntityQuery<Version> implements java.io.Serializable{
	
	@Logger
	static Log log;
	public VersionList() {
		final String[] RESTRICTIONS = {
				"version like #{versionList.example.version}"};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));

	}
}
