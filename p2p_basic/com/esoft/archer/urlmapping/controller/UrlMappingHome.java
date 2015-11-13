package com.esoft.archer.urlmapping.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.urlmapping.UrlMappingConstants;
import com.esoft.archer.urlmapping.model.UrlMapping;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.loan.model.OperationLog;
import com.ocpsoft.pretty.MyPrettyFilter;
import com.ocpsoft.pretty.PrettyContext;

@Component
@Scope(ScopeType.VIEW)
public class UrlMappingHome extends EntityHome<UrlMapping> {
	@Logger
	static Log log;
	
	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	@Autowired
	private HttpServletRequest request;
	
	private final static StringManager sm = StringManager
			.getManager(UrlMappingConstants.Package);

	public UrlMappingHome() {
		setUpdateView(FacesUtil
				.redirect(UrlMappingConstants.View.URL_MAPPING_LIST));
		setDeleteView(FacesUtil
				.redirect(UrlMappingConstants.View.URL_MAPPING_LIST));
	}
	@Override
	@Transactional(readOnly = false)
	public String save() {

		UrlMapping urlMapping = getInstance();
		// 判重 判断pattern是否重复
		List<UrlMapping> mappings = getBaseService().findByNamedQuery(
				"UrlMapping.findByPattern", urlMapping.getPattern());
		if (mappings.size() > 0) {
			// 编辑mapping的时候本身的的 pattern已经存在于系统中
			if (!(mappings.size() == 1 && StringUtils.equals(mappings.get(0)
					.getId(), urlMapping.getId()))) {
				FacesUtil.addErrorMessage(sm.getString("patternExist"));
				return null;
			}
		}
		
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(urlMapping.getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(urlMapping.getPattern());
		UrlMapping mapping = getBaseService().get(UrlMapping.class,urlMapping.getId());
		operationLog.setOperationAction("UrlMapping-修改");
		if(mapping==null){
			operationLog.setOperationAction("UrlMapping-新建");
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
	@Transactional(readOnly = false)
	public String delete() {
		if (log.isInfoEnabled()) {
			log.info(sm.getString("log.info.deleteUrlMapping", FacesUtil
					.getExpressionValue("#{loginUserInfo.loginUserId}"),
					new Date(), getId()));
		}
		UrlMapping mapping = getBaseService().get(UrlMapping.class,getId());
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(mapping.getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(mapping.getPattern());
		operationLog.setOperationAction("UrlMapping-删除");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("菜单管理");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		// 清除当前用户session里的pretty-config
		FacesUtil.getHttpSession().removeAttribute(PrettyContext.CONFIG_KEY);
		return super.delete();
	}
}
