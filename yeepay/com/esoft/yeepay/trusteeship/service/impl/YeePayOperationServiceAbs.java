package com.esoft.yeepay.trusteeship.service.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.core.annotations.Logger;
import com.esoft.core.util.HtmlElementUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationServiceAbs;

public abstract class YeePayOperationServiceAbs<T> extends
		TrusteeshipOperationServiceAbs<T> {

	@Logger
	Log log;

	@Resource
	HibernateTemplate ht;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sendOperation(String content, String charset, FacesContext fc)
			throws IOException {
		TrusteeshipOperation to = ht.get(TrusteeshipOperation.class, content);
		// FIXME:验证，判空
		// FacesContext fc = FacesContext.getCurrentInstance();
		Map<String, String> params = MapUtil.stringToHashMap(to
				.getRequestData());
		String reqContent = HtmlElementUtil.createAutoSubmitForm(params,
				to.getRequestUrl(), charset);
		ExternalContext ec = fc.getExternalContext();
		ec.responseReset();
		ec.setResponseCharacterEncoding(charset);
		ec.setResponseContentType("text/html");
		ec.getResponseOutputWriter().write(reqContent);
		fc.responseComplete();

		to.setRequestTime(new Date());
		to.setStatus(TrusteeshipConstants.Status.SENDED);
		ht.update(to);
	}

	@Transactional(rollbackFor = Exception.class)
	public TrusteeshipOperation createTrusteeshipOperation(String markId,
			String operator, String type, String content) {
		TrusteeshipOperation to = new TrusteeshipOperation();
		to.setId(IdGenerator.randomUUID());
		to.setMarkId(markId);
		to.setOperator(operator);
		//to.setRequestUrl(RequestUrl.PAYFRONT_GATEWAY_URL);
		to.setCharset("utf-8");
		to.setRequestData(content);
		to.setType(type);
		to.setTrusteeship("sinapay");
		to.setRequestTime(new Date());
		to.setStatus(TrusteeshipConstants.Status.SENDED);
		ht.save(to);
		return to;
	}

}
