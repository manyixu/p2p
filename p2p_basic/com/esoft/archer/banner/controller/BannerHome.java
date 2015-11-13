package com.esoft.archer.banner.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.banner.BannerConstants;
import com.esoft.archer.banner.model.Banner;
import com.esoft.archer.banner.model.BannerPicture;
import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.system.model.Dict;
import com.esoft.archer.user.model.Role;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.loan.model.OperationLog;

@Component
@Scope(ScopeType.VIEW)
public class BannerHome extends EntityHome<Banner> {
	private static final long serialVersionUID = 2373410201504708160L;
	@Logger
	static Log log;
	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	@Autowired
	private HttpServletRequest request;
	private static final StringManager sm = StringManager
			.getManager(BannerConstants.Package);

	public BannerHome() {
		this.setUpdateView(FacesUtil.redirect(BannerConstants.View.BANNER_LIST));
	}

	@Override
	public Banner createInstance() {
		Banner banner = new Banner();
		banner.setPictures(new ArrayList<BannerPicture>());
		return banner;
	}

	@Override
	@Transactional(readOnly=false)
	public String save() {
		//后台操作日志
		Banner banner = this.getInstance();
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationAction("广告管理-修改");
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		Banner b = getBaseService().get(Banner.class,banner.getId());
		if(b==null){
			operationLog.setOperationAction("广告管理-新建");
		}
		if (banner.getId() == null) {
			banner.setId(IdGenerator.randomUUID());
		}
		
		List<BannerPicture> bps = banner.getPictures();
		if (bps == null || bps.size() == 0) {
			FacesUtil.addErrorMessage(sm.getString("pictureNullError"));
			return null;
		}
		for (int i = 0; i < bps.size(); i++) {
			bps.get(i).setBanner(banner);
			bps.get(i).setSeqNum(i + 1);
		}
		getBaseService().merge(banner);
		FacesUtil.addInfoMessage("Banner保存成功！");
		operationLog.setOperationId(banner.getId());
		operationLog.setOperationName(banner.getDescription());
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		
		return getSaveView();
	}
	
	@Override
	@Transactional(readOnly=false)
	public String delete(String id) {
		//后台操作日志
		Banner banner = getBaseService().get(Banner.class,id);
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(banner.getId());
		operationLog.setOperationName(banner.getDescription());
		operationLog.setOperationAction("广告管理-删除");
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		return super.delete(id);
	}
}
