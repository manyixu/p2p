package com.esoft.archer.experience.service;

import java.util.List;

import javax.faces.context.FacesContext;

import com.esoft.archer.experience.model.Experienceinvestment;
import com.esoft.jdp2p.loan.exception.ExistWaitAffirmInvests;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.yeepay.platformtransfer.model.YeePayPlatformTransfer;

public interface ExperienceinvestmentMapper {

    //int deleteByPrimaryKey(Integer id);

    int save(Experienceinvestment record);

    List find(String esql);

    int update(Experienceinvestment record);
    
    /**
	 * 放款
	 * 
	 * @throws ExistWaitAffirmInvests 
	 */
    //public void createOperation(Loan instance) throws ExistWaitAffirmInvests;
    public String sendPlatformTransfer(List<Experienceinvestment> eList,FacesContext fc) throws ExistWaitAffirmInvests;
    public void sendPlatformTransfer2() throws ExistWaitAffirmInvests;
    
}