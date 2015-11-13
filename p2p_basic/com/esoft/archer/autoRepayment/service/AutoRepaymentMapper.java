package com.esoft.archer.autoRepayment.service;

import java.util.List;

import com.esoft.archer.autoRepayment.model.AutoRepayments;
import com.esoft.archer.experience.model.Experiencegold;
import com.esoft.jdp2p.bankcard.model.BankCard;

public interface AutoRepaymentMapper {
    
    List find(String hsql);

    int update(Experiencegold record);

	int saveOrUpdate(AutoRepayments autoRepayments);
	public void autorepayments();
}