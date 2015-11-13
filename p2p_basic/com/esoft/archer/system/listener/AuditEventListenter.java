package com.esoft.archer.system.listener;

import javax.annotation.Resource;

import org.springframework.context.ApplicationListener;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.system.event.AuditEvent;

/**
 * 审计事件监听器
 * @author Administrator
 *
 */

@Component  
public class AuditEventListenter implements ApplicationListener<AuditEvent>{

	@Resource
	HibernateTemplate ht;
	
	@Override
	@Transactional
	public void onApplicationEvent(AuditEvent event) {
		ht.save(event.getSource());
	}
	
}
