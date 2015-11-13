package com.esoft.archer.term.controller;


import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.node.model.Node;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.term.TermConstants;
import com.esoft.archer.term.model.CategoryTerm;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.loan.model.OperationLog;

@Component
@Scope(ScopeType.VIEW)
public class CategoryTermHome extends EntityHome<CategoryTerm>  implements Serializable{
	
	
	@Logger static Log log ;
	
	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	@Autowired
	private HttpServletRequest request;
	@Resource
	HibernateTemplate ht ;
	private static final StringManager sm = StringManager.getManager(TermConstants.Package);
	public CategoryTermHome(){
		setUpdateView( FacesUtil.redirect(TermConstants.View.TERM_LIST) );
	}
	
	
	public CategoryTerm getTermById(String id){
		CategoryTerm instance = getBaseService().get(getEntityClass(), id);
		return instance ;
	}
	
	@Override
	@Transactional(readOnly=false)
	public String delete() {
		
//		if (log.isInfoEnabled()) {
//			log.info(sm.getString("log.info.deleteTerm",
//					(FacesUtil
//							.getExpressionValue("#{loginUserInfo.loginUserId}").toString()), new Date(), getId()));
//		}
		Set<Node> nodeSets =  getInstance().getNodes();
		List<CategoryTerm> ct =getInstance().getChildren();
		if( (nodeSets != null && nodeSets.size() > 0) || ct.size()>0){
//			log.info(sm.getString("log.info.deleteTermUnccessful",
//					(FacesUtil
//							.getExpressionValue("#{loginUserInfo.loginUserId}").toString()), new Date(), getId()));
			FacesUtil.addErrorMessage("删除失败，请先删除该分类下的所有分类或文章。");
			return null;
		}else{
			CategoryTerm categoryTerm = getBaseService().get(CategoryTerm.class,getId());
			//后台操作日志
			OperationLog operationLog = new OperationLog();
			operationLog.setOperationId(categoryTerm.getId());
			operationLog.setOperationIp(FacesUtil.getRequestIp(request));
			operationLog.setOperationName(categoryTerm.getName());
			operationLog.setOperationAction("文章分类-删除");
			operationLog.setOperationDate(new Date());
			operationLog.setOperationType("文章管理");
			operationLog.setOperationUser(loginUser.getLoginUserId());
			operationLogService.save(operationLog);
			return super.delete();
		}
	}
	@Override
	@Transactional(readOnly=false)
	public String save(){
		CategoryTerm categoryTerm = getInstance();
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(categoryTerm.getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(categoryTerm.getName());
		CategoryTerm category = getBaseService().get(CategoryTerm.class,categoryTerm.getId());
		operationLog.setOperationAction("文章分类-修改");
		if(category==null){
			operationLog.setOperationAction("文章分类-新建");
		}
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("文章管理");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		return super.save();
	}
	
}
