package com.esoft.archer.user.controller;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.esoft.archer.common.controller.EntityQuery;
import com.esoft.archer.user.UserConstants;
import com.esoft.archer.user.model.UserLoginLog;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.loan.model.OperationLog;

@Component
@Scope(ScopeType.VIEW)
public class UserOperationLogList extends EntityQuery<OperationLog> {

	private static StringManager sm = StringManager
			.getManager(UserConstants.Package);

	@Logger
	private static Log log ;
	
	public UserOperationLogList() {
		final String[] RESTRICTIONS = { 
				"operationName like #{userOperationLogList.example.operationName}",
				"operationUser like #{userOperationLogList.example.operationUser}",
				"operationDate >= #{userOperationLogList.operationTimeStart}",
				"operationDate <= #{userOperationLogList.operationTimeEnd}"};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	private Date operationTimeStart;
	private Date operationTimeEnd;

	public Date getOperationTimeStart() {
		return operationTimeStart;
	}
	public void setOperationTimeStart(Date operationTimeStart) {
		this.operationTimeStart = operationTimeStart;
	}
	public Date getOperationTimeEnd() {
		return operationTimeEnd;
	}
	public void setOperationTimeEnd(Date operationTimeEnd) {
		this.operationTimeEnd = operationTimeEnd;
	}

	
	
	


}
