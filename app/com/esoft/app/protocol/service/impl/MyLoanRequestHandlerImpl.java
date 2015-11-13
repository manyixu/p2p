package com.esoft.app.protocol.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
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
import com.esoft.app.protocol.util.PageUtil;
import com.esoft.app.protocol.util.exception.LoanException;
import com.esoft.app.protocol.util.exclusionStrategy.LoanExclusionStrategy;
import com.esoft.archer.common.exception.NoMatchingObjectsException;
import com.esoft.archer.system.controller.DictUtil;
import com.esoft.core.util.HibernateProxyTypeAdapter;
import com.esoft.jdp2p.loan.LoanConstants;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.loan.service.LoanCalculator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * 个人借款列表
 *
 */
@Service("myLoanRequestHandler")
public class MyLoanRequestHandlerImpl implements RequestHandler{
	
	@Resource
	private HibernateTemplate ht;
	@Resource
	private LoanCalculator loanCalculator;
	@Resource
	private DictUtil dictUtil;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		Session session=ht.getSessionFactory().openSession();
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			String op=json.getString("op");//操作(hk:还款中;jq:已结清;mj:募集中;lb;流标)
			Integer curPage=json.getInt("curPage");//当前要显示的页数,默认值0
			Integer size=json.getInt("size");//每页要显示的条数,默认20条,默认值0
			if(op!=null&&op.length()>0){
				Map<String,String> htMap=NodeUtil.getMap(ht, "contract");//获取合同
				GsonBuilder builder=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setExclusionStrategies(new LoanExclusionStrategy()).registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY).serializeNulls();
				List<String> statusList=new ArrayList<String>();
				if("hk".equals(op)){
					statusList.add(LoanConstants.LoanStatus.REPAYING);
					statusList.add(LoanConstants.LoanStatus.OVERDUE);
					statusList.add(LoanConstants.LoanStatus.BAD_DEBT);
				}else if("jq".equals(op)){
					statusList.add(LoanConstants.LoanStatus.COMPLETE);
				}else if("mj".equals(op)){
					statusList.add(LoanConstants.LoanStatus.RAISING);
					statusList.add(LoanConstants.LoanStatus.RECHECK);
				}else if("lb".equals(op)){
					statusList.add(LoanConstants.LoanStatus.CANCEL);
				}else{
					throw new LoanException("参数op的值不在范围内");
				}
				out=getLoans(out,session,builder,userId,curPage,size,statusList,htMap);
				out.setResultCode(ResponseMsgKey.SUCCESS);
				out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
			}else{
				throw new LoanException("参数op的值不能为空");
			}
		}catch(LoanException e){
			out.setResultCode(ResponseMsgKey.INVEST_ERROR);
			out.setResultMsg(e.getMessage());
		}catch(JSONException e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.ERROR)));
			e.printStackTrace();
		}finally{
			if(session!=null){
				session.flush();
				session.clear();
				session.close();
			}
		}
		return out;
	}
	private Out getLoans(Out out,Session session,GsonBuilder builder,String userId,int curPage,int size,List<String> statusList,Map<String,String> htMap) throws IllegalAccessException, InvocationTargetException, NoMatchingObjectsException{
		//String hql="from Invest where user.id=? and status=? order by time desc";
		StringBuilder hql=new StringBuilder("from Loan where user.id=? and status");
		if(statusList.size()>1){
			hql.append(" in(");
			for(int i=0;i<statusList.size();i++){
				hql.append("'").append(statusList.get(i)).append("'");
				if(i<statusList.size()-1){
					hql.append(",");
				}
			}
			hql.append(")");
		}else{
			hql.append("='").append(statusList.get(0)).append("'");
		}
		hql.append(" order by commitTime desc");
		List<Long> countList=ht.find("select count(id) "+hql,userId);
		Integer count=countList.get(0).intValue();
		if(count==null){
			count=0;
		}
		Page page=PageUtil.getPage(curPage, size, count);
		Query query=session.createQuery(hql.toString());
		query.setString(0, userId);
		query.setFirstResult(page.getStart());
		query.setMaxResults(page.getSize());
		List<Loan> list=query.list();
		if(list!=null&&list.size()>0){
			Gson gson=builder.create();
			List<LoanSub> subList=new ArrayList<LoanSub>();
			for(int i=0;i<list.size();i++){
				LoanSub sub=LoanUtil.getLoanSub(builder, list.get(i), loanCalculator,dictUtil,htMap);
				subList.add(sub);
			}
			page.setData(subList);
			String str=gson.toJson(page);
			System.out.println(str);
			out.encryptResult(str);
			out.sign();
		}else{
			out.encryptResult("");
			out.sign();
		}
		return out;
	}	
}
