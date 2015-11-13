package com.esoft.archer.experience.job;

import javax.annotation.Resource;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.esoft.archer.experience.service.ExperienceinvestmentMapper;
import com.esoft.jdp2p.loan.exception.ExistWaitAffirmInvests;

/**
 * 还款提醒
 * @author Administrator
 *
 */
@Component
public class ExperienceinvestmentJob implements Job{
	
	@Resource
	private ExperienceinvestmentMapper experienceinvestmentMapper;
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		try {
			experienceinvestmentMapper.sendPlatformTransfer2();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
