package com.esoft.archer.link.controller;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.link.LinkConstants;
import com.esoft.archer.link.model.Link;
import com.esoft.archer.link.model.LinkType;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.loan.model.OperationLog;

@Component
@Scope(ScopeType.VIEW)
public class LinkHome extends EntityHome<Link> implements Serializable {
	private static final long serialVersionUID = -5661050223988953680L;
	@Logger
	static Log log;
	@Resource
	LoginUserInfo loginUserInfo;
	
	@Resource
	private OperationLogService operationLogService;
	@Autowired
	private HttpServletRequest request;
	private final static StringManager sm = StringManager
			.getManager(LinkConstants.Package);

	public LinkHome() {
		setUpdateView(FacesUtil.redirect(LinkConstants.View.LINK_LIST));
		setDeleteView(FacesUtil.redirect(LinkConstants.View.LINK_LIST));
	}
	
	@Override
	@Transactional(readOnly=false)
	public String delete() {
		if (log.isInfoEnabled()) {
			log.info(sm.getString("log.info.deleteLink",
					loginUserInfo.getLoginUserId(), new Date(), getId()));
		}
		return super.delete();
	}

	@Override
	protected Link createInstance() {
		Link link = new Link();
		link.setType(new LinkType());
		link.setUrl("http://");
		link.setSeqNum(0);
		return link;
	}
	
	@Override
	@Transactional(readOnly=false)
	public String save() {
		Link link = getInstance();
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(link.getId());
		operationLog.setOperationName(link.getName());
		Link l = getBaseService().get(Link.class,link.getId());
		operationLog.setOperationAction("友情链接管理-修改");
		if(l==null){
			operationLog.setOperationAction("友情链接管理-新建");
		}
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUserInfo.getLoginUserId());
		operationLogService.save(operationLog);
		
		return super.save();
	}
	
	@Override
	@Transactional(readOnly=false)
	public String delete(String id) {
		//后台操作日志
		Link link = getBaseService().get(Link.class,id);
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(link.getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(link.getName());
		operationLog.setOperationAction("友情链接管理-删除");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUserInfo.getLoginUserId());
		operationLogService.save(operationLog);
		return super.delete(id);
	}

}
