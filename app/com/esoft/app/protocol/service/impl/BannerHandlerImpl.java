package com.esoft.app.protocol.service.impl;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.exclusionStrategy.BannerPictureExclusionStrategy;
import com.esoft.archer.banner.model.Banner;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 *
 * 发出banner
 *
 */
@Service("bannerRequestHandler")
public class BannerHandlerImpl implements RequestHandler{
	
	@Resource
	HibernateTemplate ht;
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			Banner banner=ht.get(Banner.class,"index_mobile");
			if(banner!=null){
				ExclusionStrategy exclusionStrategy=new BannerPictureExclusionStrategy();
				Gson gson=new GsonBuilder().setExclusionStrategies(exclusionStrategy).serializeNulls().create();
				String str=gson.toJson(banner.getPictures());
				out.encryptResult(str);
				out.sign();
			}
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));	
		}catch (Exception e) {
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.ERROR)));
			e.printStackTrace();
		}
		return out;
	}
}
