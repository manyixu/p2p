package com.esoft.jdp2p.risk.controller;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.jdp2p.loan.model.OperationLog;
import com.esoft.jdp2p.risk.model.FeeConfig;

@Component
@Scope(ScopeType.VIEW)
public class FeeConfigHome extends EntityHome<FeeConfig> {

	private final static String UPDATE_VIEW = FacesUtil
			.redirect("/admin/risk/feeConfigList");

	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	
	@Autowired
	private HttpServletRequest request;
	
	public FeeConfigHome() {
		setUpdateView(UPDATE_VIEW);
	}
	
	@Override
	@Transactional(readOnly=false)
	public String save() {
		FeeConfig feeConfig = getInstance();
		//FeeConfig f = getBaseService().get(FeeConfig.class,feeConfig.getId());
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(feeConfig.getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationAction("费率配置-修改");
		operationLog.setOperationName(feeConfig.getDescription());
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		
		return super.save();
	}
	
}
