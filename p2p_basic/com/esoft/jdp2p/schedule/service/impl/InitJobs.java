package com.esoft.jdp2p.schedule.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.hibernate.ObjectNotFoundException;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdScheduler;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.esoft.archer.config.ConfigConstants;
import com.esoft.archer.config.service.ConfigService;
import com.esoft.archer.experience.job.ExperiencegoldJob;
import com.esoft.archer.experience.job.ExperienceinvestmentJob;
import com.esoft.core.annotations.Logger;
import com.esoft.jdp2p.repay.model.LoanRepay;
import com.esoft.jdp2p.schedule.ScheduleConstants;
import com.esoft.jdp2p.schedule.job.AutoRepayment;
import com.esoft.jdp2p.schedule.job.LoanOverdueCheck;
import com.esoft.jdp2p.schedule.job.RefreshTrusteeshipOperation;
import com.esoft.jdp2p.schedule.job.RepayAlert;
import com.esoft.jdp2p.schedule.job.StartInterestAlert;
import com.esoft.jdp2p.schedule.model.QrtzTriggersTime;

/**
 * Company: jdp2p <br/>
 * Copyright: Copyright (c)2013 <br/>
 * Description: 项目启动以后，初始化调度
 * 
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-4-10 下午12:52:57
 * 
 *           Modification History: <br/>
 *           Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-4-10 wangzhi 1.0
 */
@Component
public class InitJobs implements ApplicationListener<ContextRefreshedEvent> {

	@Resource
	StdScheduler scheduler;

	@Logger
	static Log log;

	@Resource
	ConfigService configService;
	
	@Resource
	HibernateTemplate ht;

	// 开启哪些调度，能手动控制
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			// root application context 没有parent，他就是老大
			// 需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。 初始化所有的调度。
			
			
			// 起息提醒
			String startInterestAlert = "0";
			try {
				startInterestAlert = configService
						.getConfigValue(ConfigConstants.Schedule.START_INTEREST_ALERT);
			} catch (ObjectNotFoundException onfe) {
				onfe.printStackTrace();
			}
			
			// 自动划扣
			/*String autoRepayment = "0";
			try {
				autoRepayment = configService
						.getConfigValue(ConfigConstants.Schedule.AUTOREPAYMENT);
			} catch (ObjectNotFoundException onfe) {
				onfe.printStackTrace();
			}*/
			

			// 第三方资金托管，主动查询，默认不开启
			String enableRefreshTrusteeship = "0";
			try {
				enableRefreshTrusteeship = configService
						.getConfigValue(ConfigConstants.Schedule.ENABLE_REFRESH_TRUSTEESHIP);
			} catch (ObjectNotFoundException onfe) {
				onfe.printStackTrace();
			}
			
			
			// 体验金到账
			String experienceinvestment = "0";
			try {
				experienceinvestment = configService
						.getConfigValue(ConfigConstants.Schedule.EXPERIENCEINVESTMENT);
			} catch (ObjectNotFoundException onfe) {
				onfe.printStackTrace();
			}
			
			
			// 体验金
			String experiencegold = "0";
			try {
				experiencegold = configService
						.getConfigValue(ConfigConstants.Schedule.EXPERIENCEGOLD);
			} catch (ObjectNotFoundException onfe) {
				onfe.printStackTrace();
			}

			// 自动还款+检查项目逾期，默认开启
			//自动还款应该默认是关闭的
			String enableAutoRepayment = "1";
			try {
				enableAutoRepayment = configService
						.getConfigValue(ConfigConstants.Schedule.ENABLE_AUTO_REPAYMENT);
			} catch (ObjectNotFoundException onfe) {
				onfe.printStackTrace();
			}
			String enableRepayAlert = "0";
			try {
				enableRepayAlert = configService
						.getConfigValue(ConfigConstants.Schedule.ENABLE_REPAY_ALERT);
			} catch (ObjectNotFoundException onfe) {
				onfe.printStackTrace();
			}
			try {
				
				if (experienceinvestment.equals("1")) {
					if (log.isDebugEnabled()) {
						log.debug("enable EXPERIENCEINVESTMENT job");
					}
					// 体验金到账
					CronTrigger trigger2 = (CronTrigger) scheduler
							.getTrigger(TriggerKey
									.triggerKey(
											ScheduleConstants.TriggerName.EXPERIENCEINVESTMENT,
											ScheduleConstants.TriggerGroup.EXPERIENCEINVESTMENT));
					if (trigger2 == null) {
						initExperienceinvestmentJob();
					} else {
						scheduler.resumeTrigger(trigger2.getKey());
					}
				}
				
				if (experiencegold.equals("1")) {
					if (log.isDebugEnabled()) {
						log.debug("enable EXPERIENCEGOLD job");
					}
					// 体验金
					CronTrigger trigger2 = (CronTrigger) scheduler
							.getTrigger(TriggerKey
									.triggerKey(
											ScheduleConstants.TriggerName.EXPERIENCEGOLD,
											ScheduleConstants.TriggerGroup.EXPERIENCEGOLD));
					if (trigger2 == null) {
						initExperiencegoldJob();
					} else {
						scheduler.resumeTrigger(trigger2.getKey());
					}
				}
				
				if (enableRefreshTrusteeship.equals("1")) {
					if (log.isDebugEnabled()) {
						log.debug("enable refresh trusteeship schdule job");
					}
					// 第三方资金托管，主动查询
					CronTrigger trigger2 = (CronTrigger) scheduler
							.getTrigger(TriggerKey
									.triggerKey(
											ScheduleConstants.TriggerName.REFRESH_TRUSTEESHIP_OPERATION,
											ScheduleConstants.TriggerGroup.REFRESH_TRUSTEESHIP_OPERATION));
					if (trigger2 == null) {
						initRefreshTrusteeshipJob();
					} else {
						scheduler.resumeTrigger(trigger2.getKey());
					}
				}

				if (enableAutoRepayment.equals("1")) {
					if (log.isDebugEnabled()) {
						log.debug("enable auto repayment schdule job");
					}
					// 到期自动还款，改状态（还款完成、逾期之类）
					CronTrigger trigger = (CronTrigger) scheduler
							.getTrigger(TriggerKey
									.triggerKey(
											ScheduleConstants.TriggerName.AUTO_REPAYMENT,
											ScheduleConstants.TriggerGroup.AUTO_REPAYMENT));
					if (trigger == null) {
						initAutoRepaymengJob();
					} else {
						scheduler.resumeTrigger(trigger.getKey());
					}
				}

				if (enableRepayAlert.equals("1")) {
					// 还款提醒
					CronTrigger trigger3 = (CronTrigger) scheduler
							.getTrigger(TriggerKey.triggerKey(
									ScheduleConstants.TriggerName.REPAY_ALERT,
									ScheduleConstants.TriggerGroup.REPAY_ALERT));
					if (trigger3 == null) {
						initRepayAlertJob();
					} else {
						scheduler.rescheduleJob(trigger3.getKey(), trigger3);
					}
				}
				
				if (startInterestAlert.equals("1")) {
					if (log.isDebugEnabled()) {
						log.debug("enable refresh trusteeship schdule job");
					}
					// 起息提醒
					CronTrigger trigger5 = (CronTrigger) scheduler
							.getTrigger(TriggerKey
									.triggerKey(
											ScheduleConstants.TriggerName.REFRESH_TRUSTEESHIP_OPERATION,
											ScheduleConstants.TriggerGroup.REFRESH_TRUSTEESHIP_OPERATION));
					if (trigger5 == null) {
						initStartInterestAlertJob();
					} else {
						scheduler.resumeTrigger(trigger5.getKey());
					}
				}
				
				/*if (autoRepayment.equals("1")) {
					if (log.isDebugEnabled()) {
						log.debug("enable AUTOREPAYMENT schdule job");
					}
					//自动划扣
					CronTrigger trigger5 = (CronTrigger) scheduler
							.getTrigger(TriggerKey
									.triggerKey(
											ScheduleConstants.TriggerName.AUTOREPAYMENT,
											ScheduleConstants.TriggerGroup.AUTOREPAYMENT));
					if (trigger5 == null) {
						initAutoRepaymentJob();
					} else {
						scheduler.resumeTrigger(trigger5.getKey());
					}
				}*/
				
				
				if (log.isDebugEnabled()) {
					log.debug("start loan overdue check schdule job");
				}
				// 借款逾期调度
				CronTrigger trigger4 = (CronTrigger) scheduler
						.getTrigger(TriggerKey
								.triggerKey(
										ScheduleConstants.TriggerName.LOAN_OVERDUE_CHECK,
										ScheduleConstants.TriggerGroup.LOAN_OVERDUE_CHECK));
				if (trigger4 == null) {
					initLoanOverdueCheckJob();
				} else {
					scheduler.resumeTrigger(trigger4.getKey());
				}
			} catch (SchedulerException e1) {
				throw new RuntimeException(e1);
			}

		}
	}
	
	/**
	 * 检查项目逾期
	 * @throws SchedulerException
	 */
	private void initLoanOverdueCheckJob() throws SchedulerException {
		QrtzTriggersTime q= ht.get(QrtzTriggersTime.class, ScheduleConstants.JobName.LOAN_OVERDUE_CHECK);
		JobDetail jobDetail = JobBuilder
				.newJob(LoanOverdueCheck.class)
				.withIdentity(ScheduleConstants.JobName.LOAN_OVERDUE_CHECK,
						ScheduleConstants.JobGroup.LOAN_OVERDUE_CHECK).build();

		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.LOAN_OVERDUE_CHECK,
						ScheduleConstants.TriggerGroup.LOAN_OVERDUE_CHECK)
				.forJob(jobDetail).withSchedule(
				// 每天1点
						CronScheduleBuilder.cronSchedule(q.getTime()))
				.build();

		scheduler.scheduleJob(jobDetail, trigger);
	}


	private void initAutoRepaymengJob() throws SchedulerException {
		QrtzTriggersTime q= ht.get(QrtzTriggersTime.class, ScheduleConstants.JobName.AUTO_REPAYMENT);
		// 到期自动还款
		JobDetail jobDetail = JobBuilder
				.newJob(AutoRepayment.class)
				.withIdentity(ScheduleConstants.JobName.AUTO_REPAYMENT,
						ScheduleConstants.JobGroup.AUTO_REPAYMENT).build();

		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.AUTO_REPAYMENT,
						ScheduleConstants.TriggerGroup.AUTO_REPAYMENT)
				.forJob(jobDetail).withSchedule(
				// 每天0点1分
						CronScheduleBuilder.cronSchedule(q.getTime()))
				.build();

		scheduler.scheduleJob(jobDetail, trigger);
	}
	
	private void initRepayAlertJob() throws SchedulerException {
		log.debug("===========================321行===================================");
		System.out.println("===========================321行===================================");
		QrtzTriggersTime q= ht.get(QrtzTriggersTime.class, ScheduleConstants.JobName.REPAY_ALERT);
		// 还款提醒
		JobDetail jobDetail = JobBuilder
				.newJob(RepayAlert.class)
				.withIdentity(ScheduleConstants.JobName.REPAY_ALERT,
						ScheduleConstants.JobGroup.REPAY_ALERT).build();

		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.REPAY_ALERT,
						ScheduleConstants.TriggerGroup.REPAY_ALERT)
				.forJob(jobDetail).withSchedule(
				// 每天0点1分
						CronScheduleBuilder.cronSchedule(q.getTime()))
				.build();

		scheduler.scheduleJob(jobDetail, trigger);
		log.debug("===========================339行===================================");
	}
	
	private void initRefreshTrusteeshipJob() throws SchedulerException {
		QrtzTriggersTime q= ht.get(QrtzTriggersTime.class, ScheduleConstants.JobName.REFRESH_TRUSTEESHIP_OPERATION);
		// 资金托管主动查询
		JobDetail jobDetail2 = JobBuilder
				.newJob(RefreshTrusteeshipOperation.class)
				.withIdentity(
						ScheduleConstants.JobName.REFRESH_TRUSTEESHIP_OPERATION,
						ScheduleConstants.JobGroup.REFRESH_TRUSTEESHIP_OPERATION)
				.build();

		CronTrigger trigger2 = TriggerBuilder
				.newTrigger()
				.withIdentity(
						ScheduleConstants.TriggerName.REFRESH_TRUSTEESHIP_OPERATION,
						ScheduleConstants.TriggerGroup.REFRESH_TRUSTEESHIP_OPERATION)
				.forJob(jobDetail2).withSchedule(
				// 每两分钟执行一次
						CronScheduleBuilder.cronSchedule(q.getTime()))
				.build();

		scheduler.scheduleJob(jobDetail2, trigger2);
	}
	
	private void initStartInterestAlertJob() throws SchedulerException {
		log.debug("===========================367行===================================");
		System.out.println("===========================367行===================================");
		QrtzTriggersTime q= ht.get(QrtzTriggersTime.class, ScheduleConstants.JobName.START_INTEREST_ALERT);
		// 起息提醒
		JobDetail jobDetail2 = JobBuilder
				.newJob(StartInterestAlert.class)
				.withIdentity(
						ScheduleConstants.JobName.START_INTEREST_ALERT,
						ScheduleConstants.JobGroup.START_INTEREST_ALERT)
				.build();

		CronTrigger trigger2 = TriggerBuilder
				.newTrigger()
				.withIdentity(
						ScheduleConstants.TriggerName.START_INTEREST_ALERT,
						ScheduleConstants.TriggerGroup.START_INTEREST_ALERT)
				.forJob(jobDetail2).withSchedule(
				// 十分钟执行一次
						CronScheduleBuilder.cronSchedule(q.getTime()))
				.build();

		scheduler.scheduleJob(jobDetail2, trigger2);
	}
	
	
	
	private void initExperienceinvestmentJob() throws SchedulerException {
		QrtzTriggersTime q= ht.get(QrtzTriggersTime.class, ScheduleConstants.JobName.EXPERIENCEINVESTMENT);
		// 体验金到账
		JobDetail jobDetail2 = JobBuilder
				.newJob(ExperienceinvestmentJob.class)
				.withIdentity(
						ScheduleConstants.JobName.EXPERIENCEINVESTMENT,
						ScheduleConstants.JobGroup.EXPERIENCEINVESTMENT)
				.build();

		CronTrigger trigger2 = TriggerBuilder
				.newTrigger()
				.withIdentity(
						ScheduleConstants.TriggerName.EXPERIENCEINVESTMENT,
						ScheduleConstants.TriggerGroup.EXPERIENCEINVESTMENT)
				.forJob(jobDetail2).withSchedule(
				// 每天凌晨1点提醒
						CronScheduleBuilder.cronSchedule(q.getTime()))
				.build();

		scheduler.scheduleJob(jobDetail2, trigger2);
	}
	
	
	private void initExperiencegoldJob() throws SchedulerException {
		QrtzTriggersTime q= ht.get(QrtzTriggersTime.class, ScheduleConstants.JobName.EXPERIENCEGOLD);
		// 体验金
		JobDetail jobDetail2 = JobBuilder
				.newJob(ExperiencegoldJob.class)
				.withIdentity(
						ScheduleConstants.JobName.EXPERIENCEGOLD,
						ScheduleConstants.JobGroup.EXPERIENCEGOLD)
				.build();

		CronTrigger trigger2 = TriggerBuilder
				.newTrigger()
				.withIdentity(
						ScheduleConstants.TriggerName.EXPERIENCEGOLD,
						ScheduleConstants.TriggerGroup.EXPERIENCEGOLD)
				.forJob(jobDetail2).withSchedule(
				// 每天陵城0点提醒
						CronScheduleBuilder.cronSchedule(q.getTime()))
				.build();

		scheduler.scheduleJob(jobDetail2, trigger2);
	}
	/*private void initAutoRepaymentJob() throws SchedulerException {
		QrtzTriggersTime q= ht.get(QrtzTriggersTime.class, ScheduleConstants.JobName.AUTOREPAYMENT);
		// 自动划扣
		JobDetail jobDetail2 = JobBuilder
				.newJob(AutoRepaymentJob.class)
				.withIdentity(
						ScheduleConstants.JobName.AUTOREPAYMENT,
						ScheduleConstants.JobGroup.AUTOREPAYMENT)
				.build();

		CronTrigger trigger2 = TriggerBuilder
				.newTrigger()
				.withIdentity(
						ScheduleConstants.TriggerName.AUTOREPAYMENT,
						ScheduleConstants.TriggerGroup.AUTOREPAYMENT)
				.forJob(jobDetail2).withSchedule(
				// 每天陵城0点提醒
						CronScheduleBuilder.cronSchedule(q.getTime()))
				.build();

		scheduler.scheduleJob(jobDetail2, trigger2);
	}*/
}
