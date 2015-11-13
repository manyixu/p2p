package com.esoft.app.protocol.util.exclusionStrategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class LoanExclusionStrategy implements ExclusionStrategy {

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		// TODO Auto-generated method stub
		/*if(clazz.getName().lastIndexOf("com.esoft")!=-1){
			if(clazz.getName().lastIndexOf("com.esoft.app")!=-1){
				return false;
			}
			return true;
		}else{
			return false;
		}*/
		//System.out.println("_________________"+clazz.getName());
		return false;
	}

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		// TODO Auto-generated method stub
		return "invests".equals(f.getName())||"loanRepays".equals(f.getName())||"loanAttrs".equals(f.getName())||"verifyUser".equals(f.getName())||"loanInfoPics".equals(f.getName())||"guaranteeInfoPics".equals(f.getName())||"user".equals(f.getName())||"type".equals(f.getName())||"handler".equals(f.getName());
	}
}
