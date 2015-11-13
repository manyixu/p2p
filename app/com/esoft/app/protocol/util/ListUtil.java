package com.esoft.app.protocol.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esoft.archer.user.model.Role;

/**
 * 
 * @author Hch
 * 集合工具类
 *
 */
public class ListUtil {
	public static Map<String,Role> getRoleMap(List<Role> list){
		Map<String,Role> map=new HashMap<String,Role>();
		for(Role role:list){
			map.put(role.getId(), role);
		}
		return map;
	}
}
