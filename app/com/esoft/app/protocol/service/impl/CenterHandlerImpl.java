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
import com.esoft.archer.user.service.UserBillService;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.util.ArithUtil;
import com.esoft.jdp2p.statistics.controller.BillStatistics;
import com.esoft.jdp2p.statistics.controller.InvestStatistics;
/**
 * 
 * 获取个人中心一些信息
 *
 */
@Service("centerHandler")
public class CenterHandlerImpl implements RequestHandler{

	@Resource
	HibernateTemplate ht;
	@Resource
	private UserBillBO userBillBO;
	
	@Resource
	private BillStatistics billStatistics;
	
	@Resource
	private InvestStatistics investStatistics;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");
			//待收本金
			double dsbj = investStatistics.getReceivableCorpus(userId);
			//待收利息
			double dslx = investStatistics.getReceivableInterest(userId);
			//待收金额=待收本金+待收利息
			double dsje=ArithUtil.add(dsbj, dslx);
			//可用余额,账户余额
			double balcance=billStatistics.getBalanceByUserId(userId);
			//冻结金额
			double frozen=billStatistics.getFrozenMoneyByUserId(userId);
			//资产总额=账户可用金额+冻结金额+待收本息
			double mySum=ArithUtil.add(ArithUtil.add(balcance,frozen),dsje);
			//已收利息
			double myInvestsInterest=investStatistics.getReceivedInterest(userId);
			//已收本金
			double ysbj = investStatistics.getReceivedCorpus(userId);
			//累计投资额=回收本金+待收本金
			double ljtzje=ArithUtil.add(ysbj, dsbj);
			//预期总收益=回收利息+待收利息
			double yqzsy = ArithUtil.add(myInvestsInterest, dslx);
			
			StringBuilder str=new StringBuilder("{");
			str.append("\"dsbj\":\"").append(NumberUtil.getNumber(dsbj)).append("\",");
			str.append("\"dslx\":\"").append(NumberUtil.getNumber(dslx)).append("\",");
			str.append("\"dsje\":\"").append(NumberUtil.getNumber(dsje)).append("\",");
			str.append("\"balcance\":\"").append(NumberUtil.getNumber(balcance)).append("\",");
			str.append("\"frozen\":\"").append(NumberUtil.getNumber(frozen)).append("\",");
			str.append("\"mySum\":\"").append(NumberUtil.getNumber(mySum)).append("\",");
			str.append("\"myInvestsInterest\":\"").append(NumberUtil.getNumber(myInvestsInterest)).append("\",");
			str.append("\"ysbj\":\"").append(NumberUtil.getNumber(ysbj)).append("\",");
			str.append("\"ljtzje\":\"").append(NumberUtil.getNumber(ljtzje)).append("\",");
			str.append("\"yqzsy\":\"").append(NumberUtil.getNumber(yqzsy)).append("\"");
			str.append("}");
			//System.out.println(str.toString());
			out.encryptResult(str.toString());
			out.sign();
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
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
}
