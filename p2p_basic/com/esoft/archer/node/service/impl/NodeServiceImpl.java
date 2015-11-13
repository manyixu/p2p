package com.esoft.archer.node.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.service.impl.BaseServiceImpl;
import com.esoft.archer.node.model.Node;
import com.esoft.archer.node.service.NodeService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.Logger;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.jdp2p.loan.model.OperationLog;

@Service
public class NodeServiceImpl extends BaseServiceImpl<Node> implements
		NodeService {

	@Logger
	static Log log;
	
	@Resource
	HibernateTemplate ht;
	
	@Resource
	private LoginUserInfo loginUserInfo;
	@Autowired
	private HttpServletRequest request;

	@Transactional(readOnly=false)
	public void save(Node node) {

		
		if (StringUtils.isEmpty(node.getNodeBody().getId())) {
			// 第一次创建
			node.getNodeBody().setId(IdGenerator.randomUUID());
		}
		if (node.getCreateTime() == null) {
			node.setCreateTime(new Date());
		}
		node.setUpdateTime(new Date());

		
		Double version = 0.1d ;
		Iterator<Double> it = ht.iterate("select max(version) from NodeBodyHistory where node.id = ?",node.getId());
		if(it.hasNext()){
			Double result = it.next();
			if(result != null){
				version = new BigDecimal(version).add(new BigDecimal(result)).doubleValue();
			}
		}
//		NodeBodyHistory history = new NodeBodyHistory();
//		history.setId(IdGenerator.randomUUID());
//		history.setNode(node);
//		history.setCreateTime(node.getUpdateTime());
//		history.setVersion(version);
//		history.setBody(node.getNodeBody().getBody());
		// node.getNodeBodyHistories().add(history);
		ht.saveOrUpdate(node);
		
//		ht.save(history);
		// ht.save(history);
	}
	
	@Transactional(readOnly=false)
	public Node get(String id) {

		return ht.get(Node.class, id);
//		ht.save(history);
		// ht.save(history);
	}
	
}