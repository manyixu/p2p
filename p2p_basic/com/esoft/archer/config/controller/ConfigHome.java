package com.esoft.archer.config.controller;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.config.ConfigConstants;
import com.esoft.archer.config.model.Config;
import com.esoft.archer.config.model.ConfigType;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.loan.model.OperationLog;

@Component
@Scope(ScopeType.VIEW)
public class ConfigHome extends EntityHome<Config> implements
		java.io.Serializable {
	@Resource
	private LoginUserInfo loginUser;
	@Resource
	private OperationLogService operationLogService;
	@Autowired
	private HttpServletRequest request;
	
	@Logger
	static Log log;
	private final static StringManager sm = StringManager
			.getManager(ConfigConstants.Package);

	@Resource
	private LoginUserInfo loginUserInfo;

	public ConfigHome() {
		setUpdateView(FacesUtil.redirect(ConfigConstants.View.CONFIG_LIST));
		setDeleteView(FacesUtil.redirect(ConfigConstants.View.CONFIG_LIST));
	}

	public Config createInstance() {
		Config config = new Config();
		config.setConfigType(new ConfigType());
		return config;
	}

	@Transactional(readOnly = false)
	public String save() {
		Config userMessageTemplate = getInstance();
		//后台操作日志
		OperationLog operationLog = new OperationLog();//表
		operationLog.setOperationId(userMessageTemplate.getId());
		Config lt = getBaseService().get(Config.class,userMessageTemplate.getId());
		operationLog.setOperationName(userMessageTemplate.getName());
		operationLog.setOperationAction("添加配置属性-修改");
		if(lt==null){
			operationLog.setOperationAction("添加配置属性-新建");
		}
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLogService.save(operationLog);
		
		
		if (getInstance().getConfigType() == null
				|| StringUtils.isEmpty(getInstance().getConfigType().getId())) {
			getInstance().setConfigType(null);
		}
		return super.save();
	}

	/**
	 * 通过配置编号得到配置的值
	 * 
	 * @param configId
	 * @return
	 */
	public String getConfigValue(String configId) {
		Config config = getBaseService().get(Config.class, configId);
		if (config != null) {
			return config.getValue();
		}
		return "";
	}

	@Override
	@Transactional(readOnly = false)
	public String delete() {
		Config userMessageTemplate = getInstance();
		//后台操作日志
		OperationLog operationLog = new OperationLog();//表
		operationLog.setOperationId(userMessageTemplate.getId());
		operationLog.setOperationName(userMessageTemplate.getName());
		operationLog.setOperationAction("添加配置属性-删除");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLogService.save(operationLog);
		
		
		if (log.isInfoEnabled()) {
			log.info(sm.getString("log.info.deleteConfig",
					loginUserInfo.getLoginUserId(), new Date(), getId()));
		}
		return super.delete();
	}
}
