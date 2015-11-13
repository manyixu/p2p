package com.esoft.archer.user.controller;

import java.util.Date;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.menu.model.Menu;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.user.UserConstants;
import com.esoft.archer.user.model.Permission;
import com.esoft.archer.user.model.Role;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.loan.model.OperationLog;

@Component
@Scope(ScopeType.REQUEST)
public class PermissionHome extends EntityHome<Permission> {

	@Logger
	static Log log;
	
	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	@Autowired
	private HttpServletRequest request;
	private static StringManager userSM = StringManager
			.getManager(UserConstants.Package);
	private final static String UPDATE_VIEW = FacesUtil
			.redirect("/admin/user/permissionList");

	public PermissionHome() {
		setUpdateView(UPDATE_VIEW);
	}
	
	@Transactional(readOnly=false)
	public void ajaxDelete() {
		super.delete();
		FacesUtil.addInfoMessage(commonSM.getString("deleteLabel")
				+ commonSM.getString("success"));
	}

	@Override
	@Transactional(readOnly=false)
	public String delete() {
		if (log.isInfoEnabled()) {
			log.info(userSM.getString("log.info.deletePermission",
					FacesUtil
					.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
		}
		Set<Role> roleSets = getInstance().getRoles();
		Set<Menu> menuSets = getInstance().getMenus();
		if (roleSets != null && roleSets.size() > 0) {
			FacesUtil.addWarnMessage(userSM
					.getString("canNotDeletePermissionByRole"));
			if (log.isInfoEnabled()) {
				log.info(userSM.getString(
						"log.info.deletePermissionByRoleUnsuccessful",
						FacesUtil
						.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
			}
			return null;
		} else if (menuSets != null && menuSets.size() > 0) {
			FacesUtil.addWarnMessage(userSM
					.getString("canNotDeletePermissionByMenu"));
			if (log.isInfoEnabled()) {
				log.info(userSM.getString(
						"log.info.deletePermissionByMenuUnsuccessful",
						FacesUtil
						.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
			}
			return null;
		} else {
			//后台操作日志
			Permission permission = getBaseService().get(Permission.class,getId());
			OperationLog operationLog = new OperationLog();
			operationLog.setOperationId(permission.getId());
			operationLog.setOperationIp(FacesUtil.getRequestIp(request));
			operationLog.setOperationName(permission.getName());
			operationLog.setOperationAction("权限管理-删除");
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
		Permission permission = getInstance();
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(permission.getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(permission.getName());
		Permission p = getBaseService().get(Permission.class,permission.getId());
		operationLog.setOperationAction("权限管理-修改");
		if(p==null){
			operationLog.setOperationAction("权限管理-新建");
		}
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("安全管理");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		return super.save();
	}
}
