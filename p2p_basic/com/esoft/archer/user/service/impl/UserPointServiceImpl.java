package com.esoft.archer.user.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.LockMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.user.UserConstants;
import com.esoft.archer.user.UserConstants.UserPointOperateType;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.model.UserPoint;
import com.esoft.archer.user.model.UserPointHistory;
import com.esoft.archer.user.service.UserPointService;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.loan.model.OperationLog;

@Service(value = "userPointService")
public class UserPointServiceImpl implements UserPointService {

	@Resource
	private HibernateTemplate ht;
	
	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	@Autowired
	private HttpServletRequest request;
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void minus(String userId, int point, String type,
			String operatorInfo, String operatorDetail)
			throws InsufficientBalance {
		if (point < 0) {
			throw new RuntimeException("point cannot be less than zero!");
		}
		UserPoint up = getPointByUserId(userId, type);

		if (up == null) {
			up = new UserPoint();
			up.setId(IdGenerator.randomUUID());
			up.setUser(new User(userId));
			up.setType(type);
			up.setPoint(0);
		}
		if (up.getPoint() < point) {
			throw new InsufficientBalance("minus point:" + point
					+ ",remain point:" + up.getPoint());
		}
		up.setPoint(up.getPoint() - point);
		up.setLastUpdateTime(new Date());

		//历史 start
		UserPointHistory history=new UserPointHistory();
		history.setId(IdGenerator.randomUUID());
		history.setTimeDate(new Date());
		history.setOperateType(UserPointOperateType.MINUS);
		history.setType(type);
		history.setUser(new User(userId));
		history.setTypeInfo(operatorInfo);
		history.setRemark(operatorDetail);
		history.setPoint(point);
		ht.save(history);
		//历史end
		ht.saveOrUpdate(up);
		
		ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		HttpSession session=attr.getRequest().getSession(true);
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(up.getUser().getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName("积分管理");
		operationLog.setOperationAction("减少");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("安全管理");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLog.setOperationUser(((SecurityContextImpl)session.getAttribute("SPRING_SECURITY_CONTEXT")).getAuthentication().getName());
		operationLogService.save(operationLog);
		
		if (type.equals(UserConstants.UserPointType.LEVEL)) {
			//TODO:处理用户级别信息
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void add(String userId, int point, String type, String operatorInfo,
			String operatorDetail) {
		//FIXME:如果是添加的升级积分，则根据情况，需要调用userLevelService，进行升级
		if (point < 0) {
			throw new RuntimeException("point cannot be less than zero!");
		}
		UserPoint up = getPointByUserId(userId, type);
		if (up == null) {
			up = new UserPoint();
			up.setId(IdGenerator.randomUUID());
			up.setUser(new User(userId));
			up.setType(type);
			up.setPoint(0);
		} else {
			ht.lock(up, LockMode.UPGRADE);
		}
		up.setLastUpdateTime(new Date());
		up.setPoint(up.getPoint() + point);
		ht.saveOrUpdate(up);
		//历史 start
		UserPointHistory history=new UserPointHistory();
		history.setId(IdGenerator.randomUUID());
		history.setTimeDate(new Date());
		history.setOperateType(UserPointOperateType.ADD);
		history.setType(type);
		history.setUser(new User(userId));
		history.setTypeInfo(operatorInfo);
		history.setRemark(operatorDetail);
		history.setPoint(point);
		ht.save(history);
		
		ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		HttpSession session=attr.getRequest().getSession(true);
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(up.getUser().getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName("积分管理");
		operationLog.setOperationAction("增加");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("安全管理");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLog.setOperationUser(((SecurityContextImpl)session.getAttribute("SPRING_SECURITY_CONTEXT")).getAuthentication().getName());
		operationLogService.save(operationLog);
		//历史 end
		if (type.equals(UserConstants.UserPointType.LEVEL)) {
			//TODO:处理用户级别信息
		}
	}

	@Override
	public UserPoint getPointByUserId(String userId, String type) {
		String hql = "from UserPoint up where up.user.id =? and up.type=?";
		List<UserPoint> ups = ht.find(hql, new String[] { userId, type });
		if (ups.size() == 0) {
			return null;
		} else if (ups.size() == 1) {
			ht.lock(ups.get(0), LockMode.UPGRADE);
			return ups.get(0);
		} else {
			throw new DuplicateKeyException("user.id:'" + userId + "', type:'"
					+ type + "'");
		}
	}

	@Override
	public int getPointsByUserId(String userId, String type) {
		UserPoint up = getPointByUserId(userId, type);
		if (up == null) {
			return 0;
		}
		return up.getPoint();
	}

}
