package com.esoft.archer.user.controller;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.config.service.ConfigService;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.user.UserBillConstants.OperatorInfo;
import com.esoft.archer.user.UserConstants;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.model.UserBill;
import com.esoft.archer.user.service.UserBillService;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.loan.model.OperationLog;
import com.esoft.jdp2p.user.service.RechargeService;
import com.esoft.jdp2p.user.service.WithdrawCashService;

@Component
@Scope(ScopeType.VIEW)
public class UserBillHome extends EntityHome<UserBill> implements Serializable {

	@Logger
	private static Log log;

	@Resource
	private UserBillService ubs;

	@Resource
	private RechargeService rechargeService;

	@Resource
	private WithdrawCashService wcService;

	@Resource
	private ConfigService cs;
	
	@Resource
	private LoginUserInfo loginUser;
	@Autowired
	private HttpServletRequest request;
	
	@Resource
	private OperationLogService operationLogService;

	private static StringManager sm = StringManager
			.getManager(UserConstants.Package);

	@Override
	protected UserBill createInstance() {
		UserBill bill = new UserBill();
		bill.setUser(new User());
		return bill;
	}

	/**
	 * 获取用户账户余额
	 * 
	 * @return
	 */
	public double getBalanceByUserId(String userId) {
		return ubs.getBalance(userId);
	}

	/**
	 * 获取用户账户冻结金额
	 * 
	 * @param userId
	 *            用户id
	 * @return 余额
	 */
	public double getFrozenMoneyByUserId(String userId) {
		return ubs.getFrozenMoney(userId);
	}

	/**
	 * 管理员操作借款账户
	 */
	@Transactional(readOnly = false)
	public String managerUserBill() {
		if (log.isInfoEnabled())
			log.info("管理员后台手工干预账户余额，干预类型：" + this.getInstance().getType()
					+ "，干预金额：" + this.getInstance().getMoney());
		try {
			if (this.getInstance().getType().equals("管理员-充值")) {
				rechargeService.rechargeByAdmin(getInstance());
				//后台操作日志
				OperationLog operationLog = new OperationLog();
				operationLog.setOperationId(getInstance().getId());
				operationLog.setOperationIp(FacesUtil.getRequestIp(request));
				operationLog.setOperationName("财务管理");
				operationLog.setOperationAction("管理员-充值");
				operationLog.setOperationDate(new Date());
				operationLog.setOperationType("财务管理");
				operationLog.setOperationUser(loginUser.getLoginUserId());
				operationLogService.save(operationLog);
			} else if (this.getInstance().getType().equals("管理员-提现")) {
				wcService.withdrawByAdmin(getInstance());
				//后台操作日志
				OperationLog operationLog = new OperationLog();
				operationLog.setOperationId(getInstance().getId());
				operationLog.setOperationIp(FacesUtil.getRequestIp(request));
				operationLog.setOperationName("财务管理");
				operationLog.setOperationAction("管理员-提现");
				operationLog.setOperationDate(new Date());
				operationLog.setOperationType("财务管理");
				operationLog.setOperationUser(loginUser.getLoginUserId());
				operationLogService.save(operationLog);
			} else if (this.getInstance().getType().equals("转入到余额")) {
				ubs.transferIntoBalance(getInstance().getUser().getId(),
						getInstance().getMoney(), OperatorInfo.ADMIN_OPERATION,
						getInstance().getDetail());
				//后台操作日志
				OperationLog operationLog = new OperationLog();
				operationLog.setOperationId(getInstance().getId());
				operationLog.setOperationIp(FacesUtil.getRequestIp(request));
				operationLog.setOperationName("财务管理");
				operationLog.setOperationAction("转入到余额");
				operationLog.setOperationDate(new Date());
				operationLog.setOperationType("财务管理");
				operationLog.setOperationUser(loginUser.getLoginUserId());
				operationLogService.save(operationLog);
			} else if (this.getInstance().getType().equals("从余额转出")) {
				ubs.transferOutFromBalance(getInstance().getUser().getId(),
						getInstance().getMoney(), OperatorInfo.ADMIN_OPERATION,
						getInstance().getDetail());
				//后台操作日志
				OperationLog operationLog = new OperationLog();
				operationLog.setOperationId(getInstance().getId());
				operationLog.setOperationIp(FacesUtil.getRequestIp(request));
				operationLog.setOperationName("财务管理");
				operationLog.setOperationAction("从余额转出");
				operationLog.setOperationDate(new Date());
				operationLog.setOperationType("财务管理");
				operationLog.setOperationUser(loginUser.getLoginUserId());
				operationLogService.save(operationLog);
			} else if (this.getInstance().getType().equals("管理员-冻结金额")) {
				ubs.freezeMoney(getInstance().getUser().getId(), getInstance()
						.getMoney(), OperatorInfo.ADMIN_OPERATION,
						getInstance().getDetail());
				//后台操作日志
				OperationLog operationLog = new OperationLog();
				operationLog.setOperationId(getInstance().getId());
				operationLog.setOperationIp(FacesUtil.getRequestIp(request));
				operationLog.setOperationName("财务管理");
				operationLog.setOperationAction("冻结金额");
				operationLog.setOperationDate(new Date());
				operationLog.setOperationType("财务管理");
				operationLog.setOperationUser(loginUser.getLoginUserId());
				operationLogService.save(operationLog);
			} else if (this.getInstance().getType().equals("管理员-解冻金额")) {
				ubs.unfreezeMoney(getInstance().getUser().getId(),
						getInstance().getMoney(), OperatorInfo.ADMIN_OPERATION,
						getInstance().getDetail());
				//后台操作日志
				OperationLog operationLog = new OperationLog();
				operationLog.setOperationId(getInstance().getId());
				operationLog.setOperationIp(FacesUtil.getRequestIp(request));
				operationLog.setOperationName("财务管理");
				operationLog.setOperationAction("解冻金额");
				operationLog.setOperationDate(new Date());
				operationLog.setOperationType("财务管理");
				operationLog.setOperationUser(loginUser.getLoginUserId());
				operationLogService.save(operationLog);
			} else {
				log.warn("未知的转账类型：" + this.getInstance().getType());
				FacesUtil.addErrorMessage("未知的转账类型："
						+ this.getInstance().getType());
				return null;
			}
		} catch (InsufficientBalance e) {
			FacesUtil.addErrorMessage("余额不足");
			return null;
		}
		FacesUtil.addInfoMessage("操作成功！");
		return FacesUtil.redirect("/admin/fund/userBillList");
	}

}
