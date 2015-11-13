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

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.jdp2p.loan.model.OperationLog;
import com.esoft.jdp2p.message.model.UserMessageTemplate;

@Component
@Scope(ScopeType.VIEW)
public class UserMessageTemplateHome extends EntityHome<UserMessageTemplate>
		implements java.io.Serializable {

	@Resource
	private HibernateTemplate ht;

	@Logger
	private static Log log;

	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	@Autowired
	private HttpServletRequest request;

	public UserMessageTemplateHome() {
		setUpdateView(FacesUtil
				.redirect("/admin/userMessage/userMessageTemplateList"));
	}

	/**
	 * 生成编号
	 */
	public void generateId(){
		if (this.getInstance().getUserMessageNode() != null && this.getInstance().getUserMessageWay() != null) {
			this.getInstance().setId(
					this.getInstance().getUserMessageNode().getId() + "_"
							+ this.getInstance().getUserMessageWay().getId());
		} else {
			this.getInstance().setId(null);
		}
	}

	@Transactional(readOnly = false)
	public String save() {
		UserMessageTemplate userMessageTemplate = getInstance();
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(userMessageTemplate.getId());
		UserMessageTemplate lt = getBaseService().get(UserMessageTemplate.class,userMessageTemplate.getId());
		operationLog.setOperationName(userMessageTemplate.getName());
		operationLog.setOperationAction("用户消息发送模板-修改");
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		if(lt==null){
			operationLog.setOperationAction("用户消息发送模板-新建");
		}
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		return super.save();
	}
	
	@Override
	@Transactional(readOnly=false)
	public String delete(String id) {
		//后台操作日志
		UserMessageTemplate userMessageTemplate = getBaseService().get(UserMessageTemplate.class,id);
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(userMessageTemplate.getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(userMessageTemplate.getName());
		operationLog.setOperationAction("用户消息发送模板-删除");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		return super.delete(id);
	}

}
