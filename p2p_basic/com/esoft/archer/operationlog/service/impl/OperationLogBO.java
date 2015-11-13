package com.esoft.archer.operationlog.service.impl;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.esoft.jdp2p.loan.model.OperationLog;

/**
 * Company: jdp2p <br/>
 * Copyright: Copyright (c)2013 <br/>
 * Description:
 * 
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-1-13 下午3:08:49
 * 
 *           Modification History: <br/>
 *           Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-13 wangzhi 1.0
 */
@Service("operationLogBO")
public class OperationLogBO {

	@Resource
	HibernateTemplate ht;

	public void save(OperationLog operationLog) {
		ht.save(operationLog);
	}

}
