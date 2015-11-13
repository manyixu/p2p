package com.esoft.app.protocol.util.exclusionStrategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class InvestExclusionStrategy implements ExclusionStrategy {

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		// TODO Auto-generated method stub
		return "user".equals(f.getName())||"loan".equals(f.getName())||"userCoupon".equals(f.getName())||"transferApply".equals(f.getName())||"transferApplies".equals(f.getName())||"investRepays".equals(f.getName())||"handler".equals(f.getName());
	}
}
