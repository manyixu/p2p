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
import com.esoft.app.protocol.util.exclusionStrategy.NodeExclusionStrategy;
import com.esoft.archer.node.NodeConstants;
import com.esoft.archer.node.model.Node;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * 
 *
 * 根据分类找到对应的文章
 */
@Service("nodeRequestHandler")
public class NodeRequestHandlerImpl implements RequestHandler{

	@Resource
	HibernateTemplate ht;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String termId=json.getString("termId");//上级分类编号
			StringBuilder hql=new StringBuilder("select node from Node node join node.categoryTerms term where node.status=1 and node.nodeType.id=?");
			hql.append(" and node in elements(term.nodes)");
			if(termId!=null&&termId.length()>0){
				hql.append(" and term.id='").append(termId).append("'");
			}
			hql.append(" order by node.seqNum desc, node.updateTime desc");
			List<Node> list=ht.find(hql.toString(), new Object[]{NodeConstants.DEFAULT_TYPE});
			if(list!=null&&list.size()>0){
				ExclusionStrategy exclusionStrategy=new NodeExclusionStrategy();
				Gson gson=new GsonBuilder().setExclusionStrategies(exclusionStrategy).setDateFormat("yyyy-MM-dd HH:mm:ss").serializeNulls().create();
				String str=gson.toJson(list);
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.ERROR)));
			e.printStackTrace();
		}
		return out;
	}

}
