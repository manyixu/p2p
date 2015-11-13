package com.esoft.app.protocol.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
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
import com.esoft.app.protocol.util.exclusionStrategy.LoanExclusionStrategy;
import com.esoft.archer.node.model.Node;
import com.esoft.archer.system.controller.DictUtil;
import com.esoft.jdp2p.loan.LoanConstants;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.loan.service.LoanCalculator;
import com.esoft.jdp2p.repay.RepayConstants.RepayUnit;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * 项目
 */
@Service("loanRequestHandler")
public class LoanRequestHandlerImpl implements RequestHandler{

	@Resource
	HibernateTemplate ht;
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
			String isRecommend=json.getString("isRecommend");//推荐状态,0:不推荐;1:推荐;默认不推荐
			String type=json.getString("type");//推荐,新手
			String status=json.getString("status");//项目状态,all:全部;默认loanList页面的使用状态
			Integer curPage=json.getInt("curPage");//当前要显示的页数,默认值0
			Integer size=json.getInt("size");//每页要显示的条数,默认20条,默认值0
			
			//新增筛选条件
			String businessType = json.getString("businessType");//项目类型
			
			Integer minDeadline = json.getInt("minDeadline");//项目期限范围起始时间
			Integer maxDeadline = json.getInt("maxDeadline");//项目期限范围截止时间
			
			Double minRate = json.getDouble("minRate");//年化利率范围起始
			Double maxRate = json.getDouble("maxRate");//年化利率范围截止
			
			//风险等级
			String riskLevel = json.getString("riskLevel");//风险等级
			
			String restriction = "";
			
			if(businessType !=null && !"".equals(businessType)){
				restriction += " and loan.businessType='"+businessType+"'"; 
			}
			
			if(riskLevel !=null && !"".equals(riskLevel)){
				restriction += " and loan.riskLevel='"+riskLevel+"'"; 
			}
			
			if(minDeadline != null && minDeadline > 0){ 
				restriction += " and (loan.deadline >= ("+ minDeadline+" * loan.type.repayTimePeriod) and loan.type.repayTimeUnit = '"+RepayUnit.MONTH+"' or loan.deadline >= ("+(minDeadline*30)+" * loan.type.repayTimePeriod) and loan.type.repayTimeUnit = '"+RepayUnit.DAY+"')";
			}
			
			if(maxDeadline != null && maxDeadline > 0){
				restriction += " and (loan.deadline <= ("+maxDeadline+" * loan.type.repayTimePeriod) and loan.type.repayTimeUnit = '"+RepayUnit.MONTH+"' or loan.deadline <= ("+(maxDeadline*30)+" * loan.type.repayTimePeriod) and loan.type.repayTimeUnit = '"+RepayUnit.DAY+"')";
			}
			
			if(minRate != null && minRate > 0.0){
				restriction += " and loan.rate >= "+minRate;
			}
			
			if(maxRate != null && maxRate > 0.0){
				restriction += " and loan.rate <= "+maxRate;
			}
			
			StringBuilder hql=new StringBuilder("from Loan loan left join loan.loanAttrs loanAttr where 1=1");
			if(!"".equals(restriction)){
				hql.append(restriction);
			}
			Map<String,String> htMap=NodeUtil.getMap(ht, "contract");//获取合同
			if(isRecommend!=null&&isRecommend.length()>0){
				if("1".equals(isRecommend)){
					hql.append(" and loanAttr.id='recommend'");
				}else{
					//hql.append(" and loanAttr.id!='recommend'");
				}
			}
			if(type!=null&&type.length()>0){
					hql.append(" and loanAttr.id='"+type+"'");
			}
			if(status!=null){
				if("".equals(status)){
					hql.append(" and loan.status in(");
					hql.append("'").append(LoanConstants.LoanStatus.RAISING).append("',");
					hql.append("'").append(LoanConstants.LoanStatus.COMPLETE).append("',");
					hql.append("'").append(LoanConstants.LoanStatus.RECHECK).append("',");
					hql.append("'").append(LoanConstants.LoanStatus.REPAYING).append("'");
					hql.append(")");
				}else if("可投资".equals(status)){
						hql.append(" and loan.status ='"+LoanConstants.LoanStatus.RAISING+"'");
				}else if("已完成".equals(status)){
					hql.append(" and loan.status ='"+LoanConstants.LoanStatus.COMPLETE+"'");
				}else if("还款中".equals(status)){
					hql.append(" and loan.status ='"+LoanConstants.LoanStatus.REPAYING+"'");
				}
			}
			hql.append(" order by ");
			hql.append("(case loan.status when 'raising' then 1 when 'recheck' then 2 when 'repaying' then 3 else 4 end) asc,");
			hql.append(" loan.seqNum desc ,");
			hql.append(" loan.commitTime desc");
			
			List<Long> countList=ht.find("select distinct count(loan) "+hql.toString());
			Integer count=countList.get(0).intValue();
			if(count==null){
				count=0;
			}
			Page page=new Page();
			if(size!=null&&size>0){
				page.setSize(size);
			}
			if(curPage!=null&&curPage>0){
				page.setCurPage(curPage);
			}
			page.setMaxSize(count);
			page.init();
			Query query=session.createQuery("select distinct loan "+hql.toString());
			query.setFirstResult(page.getStart());
			query.setMaxResults(page.getSize());
			List<Loan> list=query.list();
			if(list!=null&&list.size()>0){
				GsonBuilder builder=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").serializeNulls();
				List<LoanSub> list1=new ArrayList<LoanSub>();
				for(Loan loan:list){
					if(StringUtils.isNotEmpty(loan.getGuaranteeCompanyName())){
						Node guaranteeCompanyName = ht.get(Node.class, loan.getGuaranteeCompanyName());
						loan.setGuaranteeCompanyName(guaranteeCompanyName==null?"":guaranteeCompanyName.getTitle());
					}
					LoanSub sub=LoanUtil.getLoanSub(builder, loan, loanCalculator,dictUtil,htMap);
					list1.add(sub);
					session.evict(loan);
				}
				//System.out.println(list.size()+"__________________"+list1.size());
				ExclusionStrategy exclusionStrategy=new LoanExclusionStrategy();
				Gson gson=builder.setExclusionStrategies(exclusionStrategy).create();
				//page.setData(gson.toJson(list));
				page.setData(list1);
				String str=gson.toJson(page);
//				System.out.println(str);
				out.encryptResult(str);
				out.sign();
				out.setResultCode(ResponseMsgKey.SUCCESS);
				out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
			}else{
				out.setResultCode(ResponseMsgKey.LOAN_NOT_FIND);
				out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.LOAN_NOT_FIND)));
			}
		}catch(JSONException e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		} catch(Exception e){
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
}
