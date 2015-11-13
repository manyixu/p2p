package com.esoft.archer.operationlog.service;

import com.esoft.archer.common.exception.AuthInfoAlreadyActivedException;
import com.esoft.archer.common.exception.AuthInfoOutOfDateException;
import com.esoft.archer.common.exception.NoMatchingObjectsException;
import com.esoft.archer.user.UserConstants;
import com.esoft.archer.user.exception.ConfigNotFoundException;
import com.esoft.archer.user.exception.NotConformRuleException;
import com.esoft.archer.user.exception.RoleNotFoundException;
import com.esoft.archer.user.exception.UserNotFoundException;
import com.esoft.archer.user.model.User;
import com.esoft.jdp2p.loan.model.OperationLog;

/**
 * Description: 用户service<br/>
 * Copyright: Copyright (c)2013<br/>
 * Company:jdp2p<br/>
 * 
 * @author wanghm
 * @version: 1.0 Create at: 2014-1-4 下午3:36:30
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-4 wanghm 1.0
 * 
 */
public interface OperationLogService {

	/**
	 *保存操作日志
	 * 
	 * @param operationLog
	 *            操作日志对象
	 */
	public void save(OperationLog operationLog);

}
