package com.esoft.archer.user.controller;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.user.UserConstants;
import com.esoft.archer.user.service.UserService;
import com.esoft.archer.user.service.impl.UserBO;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.bankcard.model.BankCardDaihuakou;
import com.esoft.jdp2p.loan.model.OperationLog;

/**
 * Filename: UserHome.java Description: Copyright: Copyright (c)2013
 * Company:jdp2p
 * 
 * @author: yinjunlu
 * @version: 1.0 Create at: 2014-1-9 上午10:16:53
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-9 yinjunlu 1.0 1.0 Version
 */
@Component
@Scope(ScopeType.VIEW)
public class UserRepaymentHome extends EntityHome<BankCardDaihuakou> implements java.io.Serializable {

	@Resource
	private UserService userService;

	@Resource
	private LoginUserInfo loginUser;

	@Resource
	private OperationLogService operationLogService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Logger
	static Log log;
	private static StringManager sm = StringManager
			.getManager(UserConstants.Package);

	@Resource
	HibernateTemplate ht;


	/**
	 * 后台保存用户
	 */
	@Override
	@Transactional(readOnly = false)
	public String save() {
		// FIXME:放在service中
		if (StringUtils.isEmpty(getInstance().getId())) {
			getInstance().setId(IdGenerator.randomUUID());
			getInstance().setTime(new Date());
			getInstance().setStatus("Y");
			//真实姓名与传过来的不相同则运行
			if(! getInstance().getRealname().equals(getInstance().getUser().getRealname())){
				FacesUtil.addInfoMessage("真实姓名与填写用户真实姓名不相符");
				return null;
			}
			//System.out.println(getInstance().getBankNo());
			//银行编号
			if(getInstance().getBankNo().equals("ICBC")){
				getInstance().setBank("中国工商银行");
			}else if(getInstance().getBankNo().equals("ABC")){
				getInstance().setBank("中国农业银行");
			}else if(getInstance().getBankNo().equals("CCB")){
				getInstance().setBank("中国建设银行");
			}else if(getInstance().getBankNo().equals("BOC")){
				getInstance().setBank("中国银行");
			}else if(getInstance().getBankNo().equals("CMBC")){
				getInstance().setBank("中国民生银行");
			}else if(getInstance().getBankNo().equals("CMB")){
				getInstance().setBank("招商银行");
			}else if(getInstance().getBankNo().equals("CIB")){
				getInstance().setBank("兴业银行");
			}else if(getInstance().getBankNo().equals("BCM")){
				getInstance().setBank("交通银行");
			}else if(getInstance().getBankNo().equals("CEB")){
				getInstance().setBank("中国光大银行");
			}else if(getInstance().getBankNo().equals("GDB")){
				getInstance().setBank("广东发展银行");
			}else{
				
			}
			/*jdk 1.6不能这样用
			 * switch (getInstance().getBankNo()) {
			case "ICBC":
				getInstance().setBank("中国工商银行");
				break;
			case "ABC":
				getInstance().setBank("中国农业银行");
				break;
			case "CCB":
				getInstance().setBank("中国建设银行");
				break;
			case "BOC":
				getInstance().setBank("中国银行");
				break;
			case "CMBC":
				getInstance().setBank("中国民生银行");
				break;
			case "CMB":
				getInstance().setBank("招商银行");
				break;
			case "CIB":
				getInstance().setBank("兴业银行");
				break;
			case "BCM":
				getInstance().setBank("交通银行");
				break;
			case "CEB":
				getInstance().setBank("中国光大银行");
				break;
			case "GDB":
				getInstance().setBank("广东发展银行");
				break;

			default:
				break;
			}*/
		}
		
		//System.out.println(getInstance().getBank());
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(getInstance().getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(getInstance().getUser().getId());
		operationLog.setOperationAction("新建自动代扣款用户");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("自动代扣款管理");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);

		setUpdateView(FacesUtil.redirect("/admin/user/userRepaymentList"));
		return super.save();
	}

	/**
	 * 管理员修改用户，修改普通信息和邮箱、手机，不可修改密码等
	 * 
	 * @return
	 */
	@Transactional(readOnly=false,rollbackFor = Exception.class)
	public String modifyByAdmin() {
		//真实姓名与传过来的不相同则运行
		if(! getInstance().getRealname().equals(getInstance().getUser().getRealname())){
			FacesUtil.addInfoMessage("真实姓名与填写用户真实姓名不相符");
			return null;
		}
		getBaseService().merge(getInstance());
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(getInstance().getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(getInstance().getUser().getId());
		operationLog.setOperationAction("修改自动代扣款功能");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("自动代扣款管理");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		FacesUtil.addInfoMessage("修改自动代扣款信息修改成功！");
		return FacesUtil.redirect("/admin/user/userRepaymentList");
	}

	

}
