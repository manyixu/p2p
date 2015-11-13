package com.esoft.jdp2p.schedule.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.hibernate.ObjectNotFoundException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.esoft.archer.config.ConfigConstants;
import com.esoft.archer.config.service.ConfigService;
import com.esoft.archer.experience.model.ExperienceCoupons;
import com.esoft.archer.experience.model.Experiencegold;
import com.esoft.archer.experience.service.impl.ExperiencegoldBO;
import com.esoft.archer.user.model.User;
import com.esoft.core.annotations.Logger;
import com.esoft.core.util.DateUtil;
import com.esoft.jdp2p.loan.service.LoanService;
import com.esoft.jdp2p.message.model.UserMessageTemplate;
import com.esoft.jdp2p.message.service.impl.MessageBO;

/**
 * 还款提醒
 * @author Administrator
 *
 */
@Component
public class StartInterestAlert implements Job{
	
	@Resource
	private LoanService loanService;
	@Resource
	ConfigService configService;
	@Resource
	private MessageBO messageBO;
	@Logger
	static Log log;
	@Resource
	HibernateTemplate ht;
	@Resource
	private ExperiencegoldBO experiencegoldBO;
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		loanService.startInterestAlert();
		
		//删除实名认证次数  结束
		String dateStr=DateUtil.DateToString(DateUtil.addDay(new Date(),0), "yyyy-MM-dd");
		String hql="from Experiencegold where utype='Y' and substring(rmk21,1,10)='"+dateStr+"'";
		log.debug(hql);
		List<Experiencegold> goldList=experiencegoldBO.find(hql);		
		//发送短信放款成功
		String enableRepayAlert = "0";
		try {
			enableRepayAlert = configService.getConfigValue(ConfigConstants.Schedule.EXPERIENCE_END);
		} catch (ObjectNotFoundException onfe) {
			onfe.printStackTrace();
		}
		
		if (enableRepayAlert.equals("1")) {
			for (Experiencegold experiencegold : goldList) {
				User u = ht.get(User.class, experiencegold.getUserid());
				Map<String, String> params = new HashMap<String, String>();
				params.put("username", experiencegold.getUserid());
				System.out.println(ConfigConstants.Schedule.EXPERIENCE_END_ALERT + "_sms");
				UserMessageTemplate umt=ht.get(UserMessageTemplate.class,ConfigConstants.Schedule.EXPERIENCE_END_ALERT + "_sms");
				//发送短信
				messageBO.sendSMS(umt, params, u.getMobileNumber());
			}

		}
	}
}
