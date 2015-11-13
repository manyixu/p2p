package com.esoft.archer.operationlog.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.codec.binary.Base64;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.CommonConstants;
import com.esoft.archer.common.exception.AuthInfoAlreadyActivedException;
import com.esoft.archer.common.exception.AuthInfoOutOfDateException;
import com.esoft.archer.common.exception.NoMatchingObjectsException;
import com.esoft.archer.common.service.AuthService;
import com.esoft.archer.common.service.impl.AuthInfoBO;
import com.esoft.archer.openauth.OpenAuthConstants;
import com.esoft.archer.openauth.service.OpenAuthService;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.service.SpringSecurityService;
import com.esoft.archer.user.UserConstants;
import com.esoft.archer.user.exception.ConfigNotFoundException;
import com.esoft.archer.user.exception.NotConformRuleException;
import com.esoft.archer.user.exception.RoleNotFoundException;
import com.esoft.archer.user.exception.UserNotFoundException;
import com.esoft.archer.user.model.Role;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.UserInfoService;
import com.esoft.archer.user.service.impl.UserBO;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.DateStyle;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.HashCrypt;
import com.esoft.jdp2p.loan.model.OperationLog;
import com.esoft.jdp2p.message.MessageConstants;
import com.esoft.jdp2p.message.model.UserMessageTemplate;
import com.esoft.jdp2p.message.service.MessageService;
import com.esoft.jdp2p.message.service.impl.MessageBO;

/**
 * 
 */
@Service("operationLogService")
public class OperationLogServiceImpl implements OperationLogService {
	
	@Resource
	private OperationLogBO operationLogBO;

	@Override
	@Transactional(readOnly = false)
	public void save(OperationLog operationLog) {
		// TODO Auto-generated method stub
		operationLogBO.save(operationLog);
		
	}



}
