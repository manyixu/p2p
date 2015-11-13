package com.esoft.app.protocol.controller;

import java.io.IOException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.esoft.app.AppConstants;
import com.esoft.app.AppConstants.Config;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.SpringBeanUtil;
import com.esoft.core.util.ThreeDES;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 处理app发来的请求
 * 
 * @author Administrator
 * 
 */
@Component
@Scope(ScopeType.REQUEST)
public class RequestHome {
	private static final Gson gson = new GsonBuilder().serializeNulls().create();

	/** 处理请求 */
	public void handleRequest() {
		In in = new In(FacesUtil.getHttpServletRequest());
		//TODO:验签
		//in.verify();
		Out out = new Out(null,null, in.getDeviceId(),
				in.getRequestId(), in.getMethod(), null,
				null);
		//TODO:根据操作，取到对应service，如果找不到，报错
		if(in.verify()){
			//解密
			if(in.getValue()!=null&&in.getValue().length()>0){
				in.setValue(ThreeDES.decrypt(in.getValue(), Config.THREE_DES_BASE64_KEY,Config.THREE_DES_IV, Config.THREE_DES_ALGORITHM));
			}
			//System.out.println("______"+in.getValue());
			RequestHandler handler = null;
			try{
				handler=(RequestHandler) SpringBeanUtil.getBeanByName(in.getMethod());
				if(handler!=null){
					out= handler.handle(in,out);
				}else{
					out.setResultCode(ResponseMsgKey.METHOD_NOT_FOUND);
					out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.METHOD_NOT_FOUND)));
				}
			}catch(org.springframework.beans.factory.NoSuchBeanDefinitionException e){
				out.setResultCode(ResponseMsgKey.METHOD_NOT_FOUND);
				out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.METHOD_NOT_FOUND)));
			}
	
		}else{
			out.setResultCode(ResponseMsgKey.ILLEGAL_SIGN);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.ILLEGAL_SIGN)));
		}
		try {
			FacesUtil.getHttpServletResponse().getWriter()
					.print(gson.toJson(out));
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				FacesUtil.getHttpServletResponse().getWriter().flush();
				FacesUtil.getHttpServletResponse().getWriter().close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FacesUtil.getCurrentInstance().responseComplete();
	}
	public static void main(String[] args){
		Out out=new Out();
		out.setDeviceId("1111");
		out.setMethod("111");
		System.out.println(gson.toJson(out));
	}
}
