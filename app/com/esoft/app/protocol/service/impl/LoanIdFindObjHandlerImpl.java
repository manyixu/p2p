package com.esoft.app.protocol.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.LoanSub;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.LoanUtil;
import com.esoft.app.protocol.util.NodeUtil;
import com.esoft.app.protocol.util.exclusionStrategy.BannerPictureExclusionStrategy;
import com.esoft.app.protocol.util.exclusionStrategy.InvestExclusionStrategy;
import com.esoft.app.protocol.util.exclusionStrategy.LoanExclusionStrategy;
import com.esoft.app.protocol.util.exclusionStrategy.LoanRepayExclusionStrategy;
import com.esoft.app.protocol.util.exclusionStrategy.LoanTypeExclusionStrategy;
import com.esoft.app.protocol.util.exclusionStrategy.NodeAttrExclusionStrategy;
import com.esoft.app.protocol.util.exclusionStrategy.UserExclusionStrategy;
import com.esoft.archer.system.controller.DictUtil;
import com.esoft.archer.user.model.User;
import com.esoft.core.util.HibernateProxyTypeAdapter;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.loan.service.LoanCalculator;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * 通过项目Id找到其他对象
 *
 */
@Service("loanIdFindRequestHandler")
public class LoanIdFindObjHandlerImpl implements RequestHandler{

	@Resource
	private HibernateTemplate ht;
	@Resource
	private LoanCalculator loanCalculator;
	@Resource
	private DictUtil dictUtil;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			GsonBuilder builder=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY).serializeNulls();
			JSONObject json=new JSONObject(in.getValue());
			String loanId=json.getString("loanId");//项目编号
			String objName=json.getString("objName");//要获取对象的名称;loan:项目;invests:投资(集合),loanRepays:借款的还款信息(集合),loanAttrs:节点属性(集合),verifyUser:审核人,loanInfoPics:项目材料(集合),guaranteeInfoPics:担保材料(集合),user:借款人,type:借款类型
			Map<String,String> htMap=NodeUtil.getMap(ht, "contract");//获取合同
			Loan loan=ht.load(Loan.class, loanId);
			if(objName!=null&&objName.length()>0){
				StringBuilder sb=new StringBuilder();
				if(objName.lastIndexOf(",")!=-1){
					String[] objNames=objName.split(",");
					sb.append("{");
					for(int i=0;i<objNames.length;i++){
						String oName=objNames[i];
						sb.append("\"").append(oName).append("\":").append(getJson(oName,builder,out,loan,htMap));
						if(i<objNames.length-1){
							sb.append(",");
						}
					}
					sb.append("}");
				}else{
					sb.append(getJson(objName,builder,out,loan,htMap));
				}
				out.encryptResult(sb.toString());
				out.sign();
				out.setResultCode(ResponseMsgKey.SUCCESS);
				out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
			}else{
				String str=getJson("loan",builder,out,loan,htMap);
				System.out.println(str);
				out.encryptResult(str);
				out.sign();
				out.setResultCode(ResponseMsgKey.SUCCESS);
				out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
			}	
		}catch(JSONException e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}
		return out;
	}
	public String getJson(String objName,GsonBuilder builder,Out out,Loan loan,Map<String,String> htMap) throws Exception{
		if("loan".equals(objName)){
			LoanSub sub=LoanUtil.getLoanSub(builder, loan, loanCalculator,dictUtil,htMap);
			ExclusionStrategy exclusionStrategy=new LoanExclusionStrategy();
			Gson gson=builder.setExclusionStrategies(exclusionStrategy).create();
			String str=gson.toJson(sub);
			return str;
		}else if("invests".equals(objName)){
			ExclusionStrategy exclusionStrategy=new InvestExclusionStrategy();
			Gson gson=builder.setExclusionStrategies(exclusionStrategy).create();
			StringBuilder hql=new StringBuilder("from Invest invest where");
			hql.append(" invest.loan.id=? and invest.status!=?");
			List<Invest> list=ht.find(hql.toString(),new Object[]{loan.getId(),"cancel"});
			//List<Invest> list=loan.getInvests();
			String str=gson.toJson(list);
			JSONArray array=new JSONArray(str);
			for(int i=0;i<list.size();i++){
				JSONObject json=array.getJSONObject(i);
				User user=list.get(i).getUser();
				json.put("userId",user.getId());
				json.put("userName",user.getUsername());
				array.put(i, json);
			}
			return array.toString();
		}else if("loanRepays".equals(objName)){
			ExclusionStrategy exclusionStrategy=new LoanRepayExclusionStrategy();
			Gson gson=builder.setExclusionStrategies(exclusionStrategy).create();
			String str=gson.toJson(loan.getLoanRepays());
			return str;
		}else if("loanAttrs".equals(objName)){
			ExclusionStrategy exclusionStrategy=new NodeAttrExclusionStrategy();
			Gson gson=builder.setExclusionStrategies(exclusionStrategy).create();
			String str=gson.toJson(loan.getLoanAttrs());
			return str;
		}else if("verifyUser".equals(objName)){
			ExclusionStrategy exclusionStrategy=new UserExclusionStrategy();
			Gson gson=builder.setExclusionStrategies(exclusionStrategy).create();
			String str=gson.toJson(loan.getVerifyUser());
			return str;
		}else if("loanInfoPics".equals(objName)){
			ExclusionStrategy exclusionStrategy=new BannerPictureExclusionStrategy();
			Gson gson=builder.setExclusionStrategies(exclusionStrategy).create();
			String str=gson.toJson(loan.getLoanInfoPics());
			return str;
		}else if("guaranteeInfoPics".equals(objName)){
			ExclusionStrategy exclusionStrategy=new BannerPictureExclusionStrategy();
			Gson gson=builder.setExclusionStrategies(exclusionStrategy).create();
			String str=gson.toJson(loan.getGuaranteeInfoPics());
			out.encryptResult(str);
			out.sign();
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));	
		}else if("user".equals(objName)){
			ExclusionStrategy exclusionStrategy=new UserExclusionStrategy();
			Gson gson=builder.setExclusionStrategies(exclusionStrategy).create();
			String str=gson.toJson(loan.getUser());
			return str;
		}else if("type".equals(objName)){
			ExclusionStrategy exclusionStrategy=new LoanTypeExclusionStrategy();
			Gson gson=builder.setExclusionStrategies(exclusionStrategy).create();
			String str=gson.toJson(loan.getType());
			return str;
		}else{
			throw new Exception("op的值不在范围内");
		}
		return null;
	}
}
