package com.esoft.archer.menu.controller;


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
import com.esoft.archer.menu.MenuConstants;
import com.esoft.archer.menu.model.Menu;
import com.esoft.archer.menu.model.MenuType;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.loan.model.OperationLog;
import com.ocpsoft.pretty.MyPrettyFilter;

@Component
@Scope(ScopeType.VIEW)
public class MenuTypeHome extends EntityHome<MenuType>{
	
	@Logger static Log log ;

	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	@Autowired
	private HttpServletRequest request;
	private static final StringManager sm = StringManager.getManager(MenuConstants.Package);
	public MenuTypeHome(){
		setUpdateView( FacesUtil.redirect(MenuConstants.View.MENU_TYPE_LIST) );
	}

	@Override
	@Transactional(readOnly = false)
	public String save() {

		MenuType menuType = getInstance();
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(menuType.getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(menuType.getName());
		MenuType type = getBaseService().get(MenuType.class,menuType.getId());
		operationLog.setOperationAction("菜单类型-修改");
		if(type==null){
			operationLog.setOperationAction("菜单类型-新建");
		}
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("菜单管理");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);

		// 清除当前用户session里的pretty-config
		// FacesUtil.getHttpSession().removeAttribute(PrettyContext.CONFIG_KEY);
		// 清除application中的pretty-config
		FacesUtil.getHttpSession().getServletContext()
				.removeAttribute(MyPrettyFilter.CONFIG_KEY);
		return super.save();
	}
	@Override
	@Transactional(readOnly=false)
	public String delete() {
		if(log.isInfoEnabled()){
			log.info(sm.getString("log.info.deleteMenuType",FacesUtil
					.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
		}
		Set<Menu> menuSets = getInstance().getMenus();
		if(menuSets != null && menuSets.size() > 0){
			FacesUtil.addWarnMessage(sm.getString("canNotDeleteMenuType"));
			if(log.isInfoEnabled()){
				log.info(sm.getString("log.info.deleteMenuUnsuccessful",
						FacesUtil
						.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
			}
			return null;
		}else{
			MenuType type = getBaseService().get(MenuType.class,getId());
			//后台操作日志
			OperationLog operationLog = new OperationLog();
			operationLog.setOperationId(type.getId());
			operationLog.setOperationIp(FacesUtil.getRequestIp(request));
			operationLog.setOperationName(type.getName());
			operationLog.setOperationAction("菜单类型-删除");
			operationLog.setOperationDate(new Date());
			operationLog.setOperationType("菜单管理");
			operationLog.setOperationUser(loginUser.getLoginUserId());
			operationLogService.save(operationLog);
			return super.delete();
		}
	}
}
