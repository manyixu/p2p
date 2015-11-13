package com.esoft.archer.language.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.language.LanguageConstants;
import com.esoft.archer.language.model.Language;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.loan.model.OperationLog;

@Component
@Scope(ScopeType.REQUEST)
public class LanguageHome extends EntityHome<Language> {
	@Resource
	HibernateTemplate ht;
	@Logger
	static Log log;
	private List<String> localeList = new ArrayList<String>();
	private String str;
	@Resource
	private LoginUserInfo loginUser;
	@Resource
	private OperationLogService operationLogService;
	@Autowired
	private HttpServletRequest request;
	private final static StringManager sm = StringManager
			.getManager(LanguageConstants.Package);

	
	public LanguageHome() {
		setUpdateView(FacesUtil.redirect(LanguageConstants.View.CONFIG_LIST));
		setDeleteView(FacesUtil.redirect(LanguageConstants.View.CONFIG_LIST));

	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public List<String> getLocaleList() {
		if (localeList == null || localeList.size() <= 0) {
			List<Language> listLanguage = ht.findByNamedQuery("Language.findAllLanguage");
			Locale[] list = Locale.getAvailableLocales();
			Locale locale = new Locale("Enlish");
			String first = sm.getString("chooseYourLanguage");
			localeList.add(first);
			for (int i = 0; i < list.length; i++) {
				String str = list[i].getDisplayCountry();
				if (StringUtils.isNotEmpty(str)) {
					str = list[i].getDisplayCountry(locale) + "  "
							+ list[i].getDisplayCountry() + "("
							+ list[i].getDisplayLanguage() + ")  "
							+ list[i].getLanguage() + "_"
							+ list[i].getCountry();
					if(localeList.contains(str)){
						continue;
					}else{
						if(listLanguage != null && listLanguage.size() > 0){
							boolean isNotExist = true;
							String countryStr = list[i].getLanguage() + "_"+ list[i].getCountry();
							for (Language l : listLanguage) {
								if(countryStr.equals(l.getId())){
									isNotExist = false;
								}
							}
							if(isNotExist){
								localeList.add(str);
							}
						}else{
							localeList.add(str);
						}
					}
				}
			}
		}
		Collections.sort(localeList);
		return localeList;
	}

	public void setLocaleList(List<String> localeList) {
		this.localeList = localeList;
	}
	
	public void initLanguage(){
		String language = this.getStr();
		if(StringUtils.isNotEmpty(language)){
			String[] arrStr = language.split("  ");
			if(arrStr.length > 1){
				getInstance().setId(arrStr[2]);
				getInstance().setCountry(arrStr[1]);
				getInstance().setName(arrStr[0]);
			}
		}
	}

	@Transactional(readOnly=false)
	public String save() {
		Language language = getInstance();
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(language.getId());
		operationLog.setOperationName(language.getName());
		Language l = getBaseService().get(Language.class,language.getId());
		operationLog.setOperationAction("语言配置-修改");
		if(l==null){
			operationLog.setOperationAction("语言配置-新建");
		}
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		return super.save();
	}

	@Override
	@Transactional(readOnly=false)
	public String delete() {
		if (log.isInfoEnabled()) {
			log.info(sm.getString("log.info.deleteLanguage",
					FacesUtil
					.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
		}
		//后台操作日志
		Language language = getBaseService().get(Language.class,getId());
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(language.getId());
		operationLog.setOperationName(language.getName());
		operationLog.setOperationAction("语言配置-删除");
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		return super.delete();
	}
}
