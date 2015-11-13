package com.esoft.archer.version.controller;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.language.LanguageConstants;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.version.model.Version;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.jdp2p.loan.model.OperationLog;
@Component
@Scope(ScopeType.VIEW)
public class VersionHome  extends EntityHome<Version> implements Serializable {
	
	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	
	@Autowired
	private HttpServletRequest request;
	
	public VersionHome() {
		setUpdateView(FacesUtil.redirect("/admin/version/versionList"));
		setDeleteView(FacesUtil.redirect("/admin/version/versionList"));

	}
	
	@Override
	@Transactional(readOnly = false)
	public String save() {
		Version version = getInstance();
		if(StringUtils.isEmpty(version.getId())){//新增
			version.setId(IdGenerator.randomUUID());
		}
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(version.getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(version.getVersion());
		Version v = getBaseService().get(Version.class,version.getId());
		operationLog.setOperationAction("app版本-修改");
		if(v==null){
			operationLog.setOperationAction("app版本-新建");
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
		Version version = getBaseService().get(Version.class,id);
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(version.getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(version.getVersion());
		operationLog.setOperationAction("app版本-删除");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		return super.delete(id);
	}

}
