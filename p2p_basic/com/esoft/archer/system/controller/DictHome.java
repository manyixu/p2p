package com.esoft.archer.system.controller;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.model.Dict;
import com.esoft.archer.user.model.Role;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.jdp2p.loan.model.OperationLog;

@Component
@Scope(ScopeType.VIEW)
public class DictHome extends EntityHome<Dict> implements java.io.Serializable{
	
	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	
	@Autowired
	private HttpServletRequest request;
	
	public DictHome() {
		setUpdateView(FacesUtil.redirect("/admin/system/dictList"));
	}
	
	@Override
	@Transactional(readOnly = false)
	public String save() {
		Dict dict = getInstance();
		if(StringUtils.isEmpty(dict.getId())){//新增
			dict.setId(IdGenerator.randomUUID());
		}else{//编辑
			//判断父编号不能为自己本身
			Dict parent = dict.getParent();
			String parentId = parent == null ? "" : parent.getId();
			if(StringUtils.equals(dict.getId(), parentId)){
				FacesUtil.addErrorMessage("父节点编码不能为自己本身！");
				return null ;
			}
			
		}
		
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(dict.getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(dict.getValue());
		Dict r = getBaseService().get(Dict.class,dict.getId());
		operationLog.setOperationAction("数据字典管理-修改");
		if(r==null){
			operationLog.setOperationAction("数据字典管理-新建");
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
		Dict dict = getBaseService().get(Dict.class,id);
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(dict.getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(dict.getValue());
		operationLog.setOperationAction("数据字典管理-删除");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		return super.delete(id);
	}
}
