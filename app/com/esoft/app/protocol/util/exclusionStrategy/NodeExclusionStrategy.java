package com.esoft.app.protocol.util.exclusionStrategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class NodeExclusionStrategy implements ExclusionStrategy {

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		// TODO Auto-generated method stub
		return "userByLastModifyUser".equals(f.getName())||"nodeType".equals(f.getName())||"nodeBody".equals(f.getName())||"userByCreator".equals(f.getName())||"nodeBodyHistories".equals(f.getName())||"nodeAttrs".equals(f.getName())||"comments".equals(f.getName())||"categoryTerms".equals(f.getName())||"handler".equals(f.getName());
	}
}
