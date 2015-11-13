package com.esoft.archer.user.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.term.model.CategoryTerm;
import com.esoft.archer.user.UserConstants;
import com.esoft.archer.user.model.Role;
import com.esoft.archer.user.model.User;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.loan.model.OperationLog;

@Component
@Scope(ScopeType.VIEW)
public class RoleHome extends EntityHome<Role> {

	// private static final long serialVersionUID = -5194410042254832100L;

	@Logger
	Log log;
	
	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	
	@Autowired
	private HttpServletRequest request;

	private static StringManager sm = StringManager
			.getManager(UserConstants.Package);
	private final static String UPDATE_VIEW = FacesUtil
			.redirect("/admin/user/roleList");

	public RoleHome() {
		// FIXME：保存角色的时候会执行一条更新User的语句
		setUpdateView(UPDATE_VIEW);
	}

	@Override
	@Transactional(readOnly=false)
	public String delete() {
		if (log.isInfoEnabled()) {
			log.info(sm.getString("log.info.deleteRole",
					FacesUtil
					.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
		}
		List<User> userLists = getInstance().getUsers();
		if (userLists != null && userLists.size() > 0) {
			FacesUtil.addWarnMessage(sm
					.getString("canNotDeleteRole"));
			if (log.isInfoEnabled()) {
				log.info(sm.getString(
						"log.info.deleteRoleUnsuccessful",
						FacesUtil
						.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
			}
			return null;
		}else{
			//后台操作日志
			Role role = getBaseService().get(Role.class,getId());
			OperationLog operationLog = new OperationLog();
			operationLog.setOperationId(role.getId());
			operationLog.setOperationIp(FacesUtil.getRequestIp(request));
			operationLog.setOperationName(role.getName());
			operationLog.setOperationAction("角色管理-删除");
			operationLog.setOperationDate(new Date());
			operationLog.setOperationType("安全管理");
			operationLog.setOperationUser(loginUser.getLoginUserId());
			operationLogService.save(operationLog);
			return super.delete();
		}
	}
	
	@Override
	@Transactional(readOnly=false)
	public String save(){
		Role role = getInstance();
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(role.getId());
		operationLog.setOperationName(role.getName());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		Role r = getBaseService().get(Role.class,role.getId());
		operationLog.setOperationAction("角色管理-修改");
		if(r==null){
			operationLog.setOperationAction("角色管理-新建");
		}
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("安全管理");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		return super.save();
	}

}
