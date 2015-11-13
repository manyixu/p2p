package com.esoft.archer.config.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
public class ConfigTypeHome extends EntityHome<ConfigType> implements
		java.io.Serializable {
	@Logger
	static Log log;
	
	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	@Autowired
	private HttpServletRequest request;
	private static final StringManager sm = StringManager
			.getManager(ConfigConstants.Package);

	public ConfigTypeHome() {
		setUpdateView(FacesUtil.redirect(ConfigConstants.View.CONFIG_TYPE_LIST));
		setDeleteView(FacesUtil.redirect(ConfigConstants.View.CONFIG_TYPE_LIST));
	}

	private List<Config> configs = new ArrayList<Config>();

	@PostConstruct
	public void init() {
		if (FacesUtil.getParameter("id") != null) {
			String id = FacesUtil.getParameter("id");
			super.setId(id);
			configs = new ArrayList<Config>();
			configs = getInstance().getConfigs();
		}
	}

	public List<Config> getConfigs() {
		return configs;
	}

	public void setConfigs(List<Config> configs) {
		this.configs = configs;
	}

	@Transactional(readOnly = false)
	public String configDetail() {
		String id = FacesUtil.getParameter("typeId");
		super.setId(id);
		ConfigType configType = getInstance();
		configType.setConfigs(configs);
		//后台操作日志
		super.save();
		return null ;
	}
	
	/**
	 * 20150728 唐相飞
	 * 安全管理中账号安全配置系统操作日志记录，与上边configDetail函数相同
	 * @return
	 */
	@Transactional(readOnly = false)
	public String configDetailUsersafe() {
		String id = FacesUtil.getParameter("typeId");
		super.setId(id);
		ConfigType configType = getInstance();
		configType.setConfigs(configs);
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(configType.getId());
		operationLog.setOperationName(configType.getName());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationAction("账号安全配置-修改");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("安全管理");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		super.save();
		return null ;
	}
	
	/**
	 * 20150728 唐相飞
	 * 系统设置中站点配置系统操作日志记录，与上边configDetail函数相同
	 * @return
	 */
	@Transactional(readOnly = false)
	public String configDetailBasic() {
		String id = FacesUtil.getParameter("typeId");
		super.setId(id);
		ConfigType configType = getInstance();
		configType.setConfigs(configs);
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(configType.getId());
		operationLog.setOperationName(configType.getName());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationAction("站点配置-修改");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		super.save();
		return null ;
	}
	
	/**
	 * 20150728 唐相飞
	 * 系统设置中邮件配置系统操作日志记录，与上边configDetail函数相同
	 * @return
	 */
	@Transactional(readOnly = false)
	public String configDetailMail() {
		String id = FacesUtil.getParameter("typeId");
		super.setId(id);
		ConfigType configType = getInstance();
		configType.setConfigs(configs);
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(configType.getId());
		operationLog.setOperationName(configType.getName());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationAction("邮件配置-修改");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		super.save();
		return null ;
	}
	
	/**
	 * 20150728 唐相飞
	 * 系统设置中债权转让配置系统操作日志记录，与上边configDetail函数相同
	 * @return
	 */
	@Transactional(readOnly = false)
	public String configDetailInvesttransfer() {
		String id = FacesUtil.getParameter("typeId");
		super.setId(id);
		ConfigType configType = getInstance();
		configType.setConfigs(configs);
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(configType.getId());
		operationLog.setOperationName(configType.getName());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationAction("债权转让配置-修改");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		super.save();
		return null ;
	}
	
	/**
	 * 20150728 唐相飞
	 * 系统设置中自动投标配置系统操作日志记录，与上边configDetail函数相同
	 * @return
	 */
	@Transactional(readOnly = false)
	public String configDetailAutoinvest() {
		String id = FacesUtil.getParameter("typeId");
		super.setId(id);
		ConfigType configType = getInstance();
		configType.setConfigs(configs);
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(configType.getId());
		operationLog.setOperationName(configType.getName());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationAction("自动投标配置-修改");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		super.save();
		return null ;
	}

	@Override
	@Transactional(readOnly = false)
	public String delete() {
		ConfigType userMessageTemplate = getInstance();
		//后台操作日志
		OperationLog operationLog = new OperationLog();//表
		operationLog.setOperationId(userMessageTemplate.getId());
		operationLog.setOperationName(userMessageTemplate.getName());
		operationLog.setOperationAction("配置分类-删除");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLogService.save(operationLog);
		
		
		if (log.isInfoEnabled()) {
			log.info(sm.getString("log.info.deleteConfigType", FacesUtil
					.getExpressionValue("#{loginUserInfo.loginUserId}"),
					new Date(), getId()));
		}
		return super.delete();
	}

	@Override
	@Transactional(readOnly = false)
	public String save() {
		ConfigType userMessageTemplate = getInstance();
		//后台操作日志
		OperationLog operationLog = new OperationLog();//表
		operationLog.setOperationId(userMessageTemplate.getId());
		ConfigType lt = getBaseService().get(ConfigType.class,userMessageTemplate.getId());
		operationLog.setOperationName(userMessageTemplate.getName());
		operationLog.setOperationAction("配置分类-修改");
		if(lt==null){
			operationLog.setOperationAction("配置分类-新建");
		}
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLogService.save(operationLog);


		return super.save();
	}
}
