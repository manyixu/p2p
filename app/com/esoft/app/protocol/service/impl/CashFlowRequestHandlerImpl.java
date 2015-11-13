package com.esoft.app.protocol.service.impl;

import java.util.ArrayList;
import java.util.List;

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
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.model.Page;
import com.esoft.app.protocol.model.UserBillSub;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.UserBillUtil;
import com.esoft.app.protocol.util.exclusionStrategy.UserBillExclusionStrategy;
import com.esoft.archer.user.model.UserBill;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * 交易记录
 *
 */
@Service("cashFlowRequestHandler")
public class CashFlowRequestHandlerImpl implements RequestHandler{

	@Resource
	HibernateTemplate ht;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		Session session=ht.getSessionFactory().openSession();
		try{
			GsonBuilder builder=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").serializeNulls();
			JSONObject json=new JSONObject(in.getValue());
			Integer curPage=json.getInt("curPage");//当前要显示的页数,可以设定默认值为0
			Integer size=json.getInt("size");//每页要显示的条数,默认20条,可以设定默认值为0
			String type=json.getString("type");//类型
			String typeInfo=json.getString("typeInfo");//类型描述
			
			//用户编号
			String userId=json.getString("userId");
			
			StringBuilder hql=new StringBuilder("from UserBill userBill where 1=1");
			if(userId!=null&&userId.length()>0){
				hql.append(" and user.id='").append(userId).append("'");
			}
			if(type!=null&&type.length()>0){
				if(type.lastIndexOf(",")!=-1){
					String[] values=type.split(",");
					hql.append(" and type in(");
					for(int i=0;i<values.length;i++){
						String value=values[i];
						hql.append("'").append(value).append("'");
						if(i<values.length-1){
							hql.append(",");
						}
					}
					hql.append(")");
				}else{
					hql.append(" and type='").append(type).append("'");
				}
			}
			if(typeInfo!=null&&typeInfo.length()>0){
				if(typeInfo.lastIndexOf(",")!=-1){
					String[] values=typeInfo.split(",");
					hql.append(" and typeInfo in(");
					for(int i=0;i<values.length;i++){
						String value=values[i];
						hql.append("'").append(value).append("'");
						if(i<values.length-1){
							hql.append(",");
						}
					}
					hql.append(")");
				}else{
					hql.append(" and typeInfo='").append(typeInfo).append("'");
				}
			}
			hql.append(" order by seqNum desc");
			List<Long> countList=ht.find("select distinct count(id) "+hql.toString());
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
			Query query=session.createQuery("select distinct userBill "+hql.toString());
			query.setFirstResult(page.getStart());
			query.setMaxResults(page.getSize());
			List<UserBill> list=query.list();
			//System.out.println(hql.toString()+"_______"+list.size());
			if(list!=null&&list.size()>0){
				List<UserBillSub> list1=new ArrayList<UserBillSub>();
				for(UserBill user:list){
					UserBillSub sub=UserBillUtil.getUserBillSub(builder, user);
					list1.add(sub);
				}
				ExclusionStrategy exclusionStrategy=new UserBillExclusionStrategy();
				Gson gson=builder.setExclusionStrategies(exclusionStrategy).create();
				page.setData(list1);
				String str=gson.toJson(page);
				System.out.println(str);
				out.encryptResult(str);
			}
			out.sign();
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
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
