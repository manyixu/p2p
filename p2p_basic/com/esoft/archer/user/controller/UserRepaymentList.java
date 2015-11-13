package com.esoft.archer.user.controller;

import java.util.Arrays;
import java.util.List;


import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.esoft.archer.common.controller.EntityQuery;
import com.esoft.archer.user.UserConstants;
import com.esoft.archer.user.model.User;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.bankcard.model.BankCardDaihuakou;

@Component
@Scope(ScopeType.VIEW)
public class UserRepaymentList extends EntityQuery<BankCardDaihuakou> {
	private static final String COUNT_HQL = "select count(distinct bank) from BankCardDaihuakou bank left join bank.user user ";
	private static final String HQL = "select distinct bank from BankCardDaihuakou bank left join bank.user user";

	private static StringManager sm = StringManager
			.getManager(UserConstants.Package);

	@Logger
	private static Log log ;
	
	private List<BankCardDaihuakou> selectedUsers;
	/**
	 * 体验金表列表查询
	 */
	public UserRepaymentList() {
		final String[] RESTRICTIONS = { "user.id like #{userRepaymentList.example.user.id}",
				"realname like #{userRepaymentList.example.realname}",
				"cardNo like #{userRepaymentList.example.cardNo}",
				"bankNo = #{userRepaymentList.example.bankNo}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}
	public List<BankCardDaihuakou> getSelectedUsers() {
		return selectedUsers;
	}
	public void setSelectedUsers(List<BankCardDaihuakou> selectedUsers) {
		this.selectedUsers = selectedUsers;
	}
	
	
	
	
	
	
	
	

}
