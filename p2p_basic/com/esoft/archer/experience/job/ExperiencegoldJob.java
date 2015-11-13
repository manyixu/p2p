package com.esoft.archer.experience.job;

import javax.annotation.Resource;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.esoft.archer.experience.service.ExperiencegoldMapper;

/**
 * 还款提醒
 * @author Administrator
 *
 */
@Component
public class ExperiencegoldJob implements Job{
	
	@Resource
	private ExperiencegoldMapper experiencegoldMapper;
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		experiencegoldMapper.timeupdate();
	}
}
