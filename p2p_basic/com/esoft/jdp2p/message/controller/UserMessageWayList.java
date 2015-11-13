package com.esoft.jdp2p.message.controller;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityQuery;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.jdp2p.loan.model.OperationLog;
import com.esoft.jdp2p.message.MessageConstants;
import com.esoft.jdp2p.message.model.UserMessageWay;

@Component
@Scope(ScopeType.VIEW)
public class UserMessageWayList extends EntityQuery<UserMessageWay>{
	
	@Resource
	private HibernateTemplate ht ;
	
	@Logger 
	private static Log log ;
	
	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	@Autowired
	private HttpServletRequest request;
	
	@Transactional(readOnly=false)
	public void changeStatus(UserMessageWay umw){
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		if (umw.getStatus().equals(MessageConstants.UserMessageNodeStatus.OPEN)) {
			umw.setStatus(MessageConstants.UserMessageNodeStatus.CLOSED);
			operationLog.setOperationAction("用户消息发送方式-"+MessageConstants.UserMessageNodeStatus.CLOSED);
		} else {
			umw.setStatus(MessageConstants.UserMessageNodeStatus.OPEN);
			operationLog.setOperationAction("用户消息发送方式-"+MessageConstants.UserMessageNodeStatus.OPEN);
		} 
		getHt().update(umw);
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationId(umw.getId());
		operationLog.setOperationName(umw.getName());
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
	}
}
