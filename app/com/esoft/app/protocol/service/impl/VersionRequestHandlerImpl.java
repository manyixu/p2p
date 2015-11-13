package com.esoft.app.protocol.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.NumberUtil;
import com.esoft.app.protocol.util.exception.InvestException;
import com.esoft.app.protocol.util.exception.VersionException;
import com.esoft.app.protocol.util.exclusionStrategy.UserExclusionStrategy;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.archer.version.model.Version;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * 
 * 检查版本
 *
 */
@Service("versionRequestHandlerImpl")
public class VersionRequestHandlerImpl implements RequestHandler{

	@Resource
	HibernateTemplate ht;
	@Resource
	private UserBillBO userBillBO;
	@Override
	public Out handle(In in,Out out) {
		// TODO Auto-generated method stub
		try {
			JSONObject json=new JSONObject(in.getValue());
			String version=json.getString("version");
			if(version!=null&&version.length()>0){
				List<Version> list=ht.find("from Version where 1=1 order by time desc");
				StringBuilder str=new StringBuilder("{");
				if(list!=null&&list.size()>0){
					Version v=list.get(0);
					if(version.equals(v.getVersion())){
						str.append("\"version\":\"").append("").append("\",");
						str.append("\"url\":\"").append("").append("\",");
						str.append("\"rmk\":\"").append("1").append("\"");
					}else{
						str.append("\"version\":\"").append(v.getVersion()).append("\",");
						str.append("\"url\":\"").append("http://www.quanli360.com/apk/qlhf.apk").append("\",");
						str.append("\"rmk\":\"").append(v.getRmk()).append("\"");
					}
				}else{
					str.append("\"version\":\"").append("").append("\",");
					str.append("\"url\":\"").append("").append("\",");
					str.append("\"rmk\":\"").append("1").append("\"");
				}
				str.append("}");
				out.encryptResult(str.toString());
				out.sign();
				out.setResultCode(ResponseMsgKey.SUCCESS);
				out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
			}else{
				out.setResultCode(ResponseMsgKey.PARAMETER_INVALID);
				out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.PARAMETER_INVALID)));
			}
		}catch(JSONException e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.ERROR)));
			e.printStackTrace();
		}
		return out;
	}
	
	/**
	 * 
	 * @param userId
	 * @return 获取个人已经投资成功的总的收益
	 */
	public double getInvestsInterest(String userId){
		StringBuilder hql =new StringBuilder("select sum(interest+defaultInterest-fee)");
		hql.append(" from InvestRepay where time is not null and invest.id in(select id");
		hql.append(" from Invest where user.id=?)");
		double money=0;
		List<Object> result=ht.find(hql.toString(),userId);
		if(result!=null&&result.get(0)!=null){
			money=(Double)result.get(0);
		}
		return money;
	}

}
