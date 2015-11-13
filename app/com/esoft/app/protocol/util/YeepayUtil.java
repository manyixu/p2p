package com.esoft.app.protocol.util;

import java.util.Date;
import java.util.Map;

import org.springframework.orm.hibernate3.HibernateTemplate;

import com.esoft.core.util.HtmlElementUtil;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;

public class YeepayUtil {
	public static String getFormStr(String content, String charset,HibernateTemplate ht){
		TrusteeshipOperation to = ht.get(TrusteeshipOperation.class, content);
		// FIXME:验证，判空
		// FacesContext fc = FacesContext.getCurrentInstance();
		Map<String, String> params = MapUtil.stringToHashMap(to
				.getRequestData());
		String reqContent = HtmlElementUtil.createAutoSubmitForm(params,
				to.getRequestUrl(), charset);
		to.setRequestTime(new Date());
		to.setStatus(TrusteeshipConstants.Status.SENDED);
		return reqContent;
	}
}
