package com.esoft.archer.autoRepayment.service.impl;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

import com.esoft.archer.autoRepayment.model.AutoRepayments;
import com.esoft.archer.autoRepayment.service.AutoRepaymentMapper;
import com.esoft.archer.experience.model.Experiencegold;
import com.esoft.jdp2p.bankcard.model.BankCard;

@Service("autoRepaymentMapper")
public class AutoRepaymentMapperImpl implements AutoRepaymentMapper {

	@Resource
	private AutoRepaymentBO autoRepaymentBO;

	@Override
	public int saveOrUpdate(AutoRepayments autoRepayments) {
		return autoRepaymentBO.saveOrUpdate(autoRepayments);
	}

	@Override
	public List find(String hsql) {
		return autoRepaymentBO.find(hsql);
	}

	@Override
	public int update(Experiencegold record) {
		// TODO Auto-generated method stub
		return 0;
	}
	/**
	 * 划扣功能
	 */
	public void autorepayments(){
		String hsql="from AutoRepayments where utype='Y'";
		//查询出自动划扣数据人员
		List<AutoRepayments> autoList=autoRepaymentBO.find(hsql);
		autoRepaymentBO.huakou(autoList);
		
	}

}