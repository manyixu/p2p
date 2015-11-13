package com.esoft.archer.experience.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cfca.util.pki.cipher.Session;
import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.common.controller.EntityQuery;
import com.esoft.archer.experience.model.Experiencegold;
import com.esoft.archer.experience.model.Experienceinvestment;
import com.esoft.archer.experience.model.Experienceproject;
import com.esoft.archer.experience.service.ExperiencegoldMapper;
import com.esoft.archer.experience.service.ExperienceinvestmentMapper;
import com.esoft.archer.experience.service.ExperienceprojectMapper;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.user.model.User;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.el.SpringSecurityELLibrary;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.jdp2p.loan.exception.ExistWaitAffirmInvests;
import com.esoft.yeepay.platformtransfer.model.YeePayPlatformTransfer;

@Component
@Scope(ScopeType.VIEW)
public class ExperienceinvestmentList extends EntityQuery<Experienceinvestment>{
	private static final long serialVersionUID = -1350682013319140386L;

	public ExperienceinvestmentList() {
		final String[] RESTRICTIONS = { "id like #{experienceinvestmentList.example.id}",
				"ename like #{experienceinvestmentList.example.ename}" };
		ArrayList<String> a = new ArrayList(Arrays.asList(RESTRICTIONS));
		setRestrictionExpressionStrings(a);
	}
}
