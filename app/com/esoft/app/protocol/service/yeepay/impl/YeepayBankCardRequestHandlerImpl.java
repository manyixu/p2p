package com.esoft.app.protocol.service.yeepay.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.ListUtil;
import com.esoft.app.protocol.util.exception.BankCardException;
import com.esoft.app.protocol.util.exception.NoInvestorException;
import com.esoft.app.protocol.util.exclusionStrategy.BankCardExclusionStrategy;
import com.esoft.archer.user.model.Role;
import com.esoft.archer.user.model.User;
import com.esoft.core.annotations.Logger;
import com.esoft.core.util.Dom4jUtil;
import com.esoft.jdp2p.bankcard.BankCardConstants;
import com.esoft.jdp2p.bankcard.model.BankCard;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 银行卡查询
 *
 */
@Service("yeepayBankCardRequestHandler")
public class YeepayBankCardRequestHandlerImpl implements RequestHandler,Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1377392331915860488L;
	@Resource
	private HibernateTemplate ht;
	@Logger
	private static Log log;
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			String op=json.getString("op");//操作（query：查询）
			GsonBuilder builder=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").serializeNulls();
			User user=ht.get(User.class, userId);
			if(user!=null){
			}else{
				throw new BankCardException("该用户编号不存在");
			}
			List<Role> roleList=user.getRoles();
			if(roleList!=null&&roleList.size()>0){
				Map<String,Role> roleMap=ListUtil.getRoleMap(roleList);
				if(roleMap.get("INVESTOR")!=null){
					
				}else{
					throw new NoInvestorException();
				}
			}else{
				throw new NoInvestorException();
			}
			if("query".equals(op)){
				List<BankCard> list= ht.find("from BankCard where user.id = ? and status != ?", new String[]{userId, BankCardConstants.BankCardStatus.DELETED});
				List<BankCard> resultList=new ArrayList<BankCard>();
				if(list!=null&&list.size()>0){
					BankCard bc = null;
					for(BankCard b: list){
						if("VERIFYING".equals(b.getStatus())){
							bc = b;
							//resultList.add(b);
							break;
						}else if(("VERIFIED").equals(b.getStatus())){
							resultList.add(b);
						}
					}
//					BankCard bc = bankCardListbyLoginUser.get(0);
					if(bc != null && "VERIFYING".equals(bc.getStatus())){
						String status = queryCardStatus(userId);
						if("VERIFIED".equals(status)){
							bc.setStatus(status);
							ht.update(bc);
							resultList.add(bc);
						}else if(status == null || "".equals(status)){
							bc.setStatus("FAIL");
							ht.update(bc);
						}
					}
					if(resultList.size()>0){
						ExclusionStrategy exclusionStrategy=new BankCardExclusionStrategy();
						Gson gson=builder.setExclusionStrategies(exclusionStrategy).create();
						String str=gson.toJson(list);
						System.out.println(str);
						out.encryptResult(str);
						out.sign();
						out.setResultCode(ResponseMsgKey.SUCCESS);
						out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));						
					}else{
						throw new BankCardException("该用户不存在银行卡号");
					}
				}else{
					throw new BankCardException("该用户不存在银行卡号");
				}
			}else{
				throw new BankCardException("op的值不在范围内");
			}
		}catch(NoInvestorException e){
			out.setResultCode(ResponseMsgKey.NO_INVESTOR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.NO_INVESTOR)));
		}catch(BankCardException e){
			out.setResultCode(ResponseMsgKey.BANK_CARD_ERROR);
			out.setResultMsg(e.getMessage());
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
	private String queryCardStatus(String userId){
		String status = "";
		HttpClient client = new HttpClient();
		// 创建一个post方法
		PostMethod postMethod = new PostMethod(
				YeePayConstants.RequestUrl.RequestDirectUrl);

		StringBuffer content = new StringBuffer();
		content.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
		content.append("<request platformNo='"
				+ YeePayConstants.Config.MER_CODE + "'>");
		content.append("<platformUserNo>" + userId
				+ "</platformUserNo>");
		content.append("</request>");
		// 生成密文
		String sign = CFCASignUtil.sign(content.toString());

		postMethod.setParameter("req", content.toString());
		postMethod.setParameter("sign", sign);
		postMethod.setParameter("service", "ACCOUNT_INFO");

		// 执行post方法
		try {
			int statusCode = client.executeMethod(postMethod);
			System.out.println(statusCode);

			byte[] responseBody = postMethod.getResponseBody();
			log.debug(new String(responseBody, "UTF-8"));
			// 响应信息
			String respInfo = new String(new String(responseBody, "UTF-8"));
			Document respXML = DocumentHelper.parseText(respInfo);
			Map<String, String> resultMap = Dom4jUtil.xmltoMap(new String(
					responseBody, "UTF-8"));
			
			String code = resultMap.get("code");
			String description = resultMap.get("description");
			String balance = resultMap.get("balance");
			String availableAmount = resultMap.get("availableAmount");
			String freezeAmount = resultMap.get("freezeAmount");
			String cardNo = resultMap.get("cardNo");
			String cardStatus = resultMap.get("cardStatus");
			String bank = resultMap.get("bank");

			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();

			if (code.equals("1")) {
				// 查询成功 处理查询记录
				// request.setAttribute("platformNo", platformNo);
//				request.setAttribute("code", code);
//				request.setAttribute("description", description);
//				request.setAttribute("balance", balance);
//				request.setAttribute("availableAmount", availableAmount);
//				request.setAttribute("freezeAmount", freezeAmount);
//				request.setAttribute("cardNo", cardNo);
//				request.setAttribute("cardStatus", cardStatus);
//				request.setAttribute("bank", bank);
				status = cardStatus;
				if(cardNo == null || "".equals(cardNo)){
					status = "";
				}
			} else {
				request.setAttribute("description", "查询失败");
			}

			/*
			 * } else { log.error("Method failed: " +
			 * postMethod.getStatusLine()); }
			 */
		} catch (HttpException e) {
			log.error("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.error("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} catch (DocumentException e) {
			log.error("Fatal parseXML error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Release the connection.
			postMethod.releaseConnection();
		}
	
		return status;
	}
}
