package com.esoft.app.protocol.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.hibernate3.HibernateTemplate;

import com.esoft.archer.node.model.Node;

public class NodeUtil {
	public static Map<String,String> getMap(HibernateTemplate ht,String termId){
		Map<String,String> map=new HashMap<String,String>();
		String hql="select distinct node from Node node left join node.categoryTerms term where term.id=?";
		List<Node> nodeList=ht.find(hql,termId);
		for(Node node:nodeList){
			map.put(node.getId(), node.getTitle());
		}
		return map;
	}
}
