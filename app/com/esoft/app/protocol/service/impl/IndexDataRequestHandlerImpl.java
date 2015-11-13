package com.esoft.app.protocol.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.LoanSub;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.model.Page;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.LoanUtil;
import com.esoft.app.protocol.util.NodeUtil;
import com.esoft.app.protocol.util.NumberUtil;
import com.esoft.app.protocol.util.exception.RegistException;
import com.esoft.app.protocol.util.exclusionStrategy.BannerPictureExclusionStrategy;
import com.esoft.app.protocol.util.exclusionStrategy.LoanExclusionStrategy;
import com.esoft.archer.banner.model.Banner;
import com.esoft.archer.node.model.Node;
import com.esoft.archer.system.controller.DictUtil;
import com.esoft.core.annotations.Logger;
import com.esoft.jdp2p.invest.InvestConstants;
import com.esoft.jdp2p.loan.LoanConstants;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.loan.service.LoanCalculator;
import com.esoft.jdp2p.repay.RepayConstants.RepayUnit;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * 首页加载所有用户投资总额，所有用户总收益
 */
@Service("indexDataRequestHandler")
public class IndexDataRequestHandlerImpl implements RequestHandler{

	@Resource
	HibernateTemplate ht;
	
	@Logger
	static Log log;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			StringBuilder str=new StringBuilder("{");
			//所有用户总收益
			String hql = "Select sum(interest+defaultInterest-fee) from InvestRepay where time is not null";
			List<Object> oos = ht.find(hql);
			Object o = oos.get(0);
			String result="0";
			if (o == null) {
				o=0;
			}
			double money = (Double) o;
			result = String.format("%.2f",money+11288.22+7767.12);
			log.debug("--->>用户总收益："+o);
			log.debug("--->>用户总收益str："+result);
			str.append("\"tzzsy\":\"").append(result).append("\",");
			//所有用户投资总额
			hql = "select sum(invest.investMoney) from Invest invest "
					+ "where invest.status not in (?,?)";
			oos = ht.find(hql, new String[] {InvestConstants.InvestStatus.WAIT_AFFIRM,InvestConstants.InvestStatus.CANCEL });
			o = oos.get(0);
			if (o == null) {
				o=0;
			}
			double tzze = (Double) o;
			result = String.format("%.2f",tzze+1526000+1050000);
			
			str.append("\"tzze\":\"").append(result).append("\"");
			str.append("}");
			out.encryptResult(str.toString());
			out.sign();
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
