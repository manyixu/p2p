package com.esoft.jdp2p.repay.controller;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.esoft.archer.common.controller.EntityQuery;
import com.esoft.archer.user.model.User;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.repay.model.InvestRepay;

@Component
@Scope(ScopeType.VIEW)
public class InvestRepayList extends EntityQuery<InvestRepay> {
	@Logger
	static Log log;
	
	public InvestRepayList() {
		final String[] RESTRICTIONS = { "id like #{investRepayList.example.id}",
				"invest.id like #{investRepayList.example.invest.id}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}
	
	@Override
	protected void initExample() {
		InvestRepay example = new InvestRepay();
		Invest invest = new Invest();
		//loan.setUser(new User());
		example.setInvest(invest);
		setExample(example);
	}

}
