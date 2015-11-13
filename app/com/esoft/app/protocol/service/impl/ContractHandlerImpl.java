package com.esoft.app.protocol.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.NumberUtil;
import com.esoft.archer.node.model.Node;
import com.esoft.archer.system.controller.DictUtil;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.archer.user.service.UserBillService;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.util.ArithUtil;
import com.esoft.core.util.DateStyle;
import com.esoft.core.util.DateUtil;
import com.esoft.jdp2p.invest.InvestConstants.InvestStatus;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.repay.RepayConstants;
import com.esoft.jdp2p.repay.model.LoanRepay;
import com.esoft.jdp2p.statistics.controller.BillStatistics;
import com.esoft.jdp2p.statistics.controller.InvestStatistics;
import com.google.common.base.Strings;
/**
 * 
 * 获取个人中心一些信息
 *
 */
@Service("contractHandler")
public class ContractHandlerImpl implements RequestHandler{

	@Resource
	HibernateTemplate ht;
	
	@Resource
	private BillStatistics billStatistics;
	
	@Resource
	private InvestStatistics investStatistics;
	@Resource
	LoginUserInfo loginUserInfo;
	
	
	@Resource
	private DictUtil dictUtil;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			String contractContent;
			JSONObject json=new JSONObject(in.getValue());
			String loanId=json.getString("loanId");
			Loan loan = ht.get(Loan.class, loanId);
			// #{loanRepayList} 还款计划列表
			  Element table0 = Jsoup
			       .parseBodyFragment("" +
			         "<table border='1' style='margin: 0px auto; border-collapse: collapse; border: 1px solid rgb(0, 0, 0); width: 70%; '>" +
			         "<tbody>" +
			         "<tr class='firstRow'>" +
			         "<td style='text-align:center;'>期数</td>" +
			         "<td style='text-align:center;'>本金</td>" +
			         "<td style='text-align:center;'>利息</td>" +
			         "<td style='text-align:center;'>罚息</td>" +
			         "<td style='text-align:center;'>手续费</td>" +
			         "<td style='text-align:center;'>总额</td>" +
			         "<td style='text-align:center;'>还款日</td>" +
			         "<td style='text-align:center;'>还款时间</td>" +
			         "<td style='text-align:center;'>状态</td>" +
			         "</tr></tbody></table>");
			     Element tbody0 = table0.getElementsByTag("tbody").first();
			  
			     
			  List<LoanRepay> loanRepays=ht.find("from LoanRepay lr where lr.loan.id=?",new String[] { loan.getId() });
			  for (LoanRepay loanRepay : loanRepays) {
			      tbody0.append("<tr>" +"<td style='text-align:center;'>"
			        + loanRepay.getPeriod()+ "</td><td style='text-align:center;'>"
			        + loanRepay.getCorpus().toString()
			        + "</td><td style='text-align:center;'>"
			        + loanRepay.getInterest().toString()
			        + "</td><td style='text-align:center;'>"
			        + loanRepay.getDefaultInterest().toString()
			        + "</td><td style='text-align:center;'>"
			        + loanRepay.getFee().toString()
			        + "</td><td style='text-align:center;'>"
			        + (loanRepay.getCorpus()+loanRepay.getInterest()+loanRepay.getDefaultInterest()+loanRepay.getFee())
			        
			        + "</td><td style='text-align:center;'>"
			       + Strings.nullToEmpty(DateUtil.DateToString(
			    		   loanRepay.getRepayDay(),
										DateStyle.YYYY_MM_DD_CN))
			        + "</td><td style='text-align:center;'>"
			        +Strings.nullToEmpty(DateUtil.DateToString(
			        		loanRepay.getTime(),
											DateStyle.YYYY_MM_DD_CN)) 
			        + "</td><td style='text-align:center;'>"
			        + dictUtil.getValue("repay_status", loanRepay.getStatus())
			        + "</td></tr>");
			     }

			// #{investList} 投资列表
			Element table = Jsoup
					.parseBodyFragment("<table border='1' style='margin: 0px auto; border-collapse: collapse; border: 1px solid rgb(0, 0, 0); width: 70%; '><tbody><tr class='firstRow'><td style='text-align:center;'>用户名</td><td style='text-align:center;'>投标来源</td><td style='text-align:center;'>借出金额</td><td style='text-align:center;'>借款期限</td><td style='text-align:center;'>应收利息</td></tr></tbody></table>");
			Element tbody = table.getElementsByTag("tbody").first();

			List<Invest> is = ht.find(
					"from Invest i where i.loan.id=? and i.status!=?",
					new String[] { loan.getId(), InvestStatus.CANCEL });
			for (Invest invest : is) {
				String usname2=invest.getUser().getUsername();
				if(!usname2.equals(loginUserInfo.getLoginUserId())){
					usname2="*****";
				}
				tbody.append("<tr><td style='text-align:center;'>"
						+ usname2
						+ "</td><td style='text-align:center;'>"
						+ (invest.getIsAutoInvest() ? "自动投标" : "手动投标")
						+ "</td><td style='text-align:center;'>"
						+ invest.getRepayRoadmap().getRepayCorpus()
						+ "</td><td style='text-align:center;'>"
						+ loan.getDeadline()
						* loan.getType().getRepayTimePeriod()
						+ "("
						+ dictUtil.getValue("repay_unit", loan.getType()
								.getRepayTimeUnit()) + ")"
						+ "</td><td style='text-align:center;'>"
						+ invest.getRepayRoadmap().getRepayInterest()
						+ "</td></tr>");
			}
			tbody.append("<tr><td style='text-align:center;'>总计</td><td></td><td style='text-align:center;'>"
					+ loan.getRepayRoadmap().getRepayCorpus()
					+ "</td><td></td><td style='text-align:center;'>"
					+ loan.getRepayRoadmap().getRepayInterest() + "</td></tr>");

			Node node = ht.get(Node.class, loan.getContractType());
				/**caijinmin 201412231623 判断还款单元是天还是月，计算借款到期日  add begin**/
				Date endDate = null;
				if (RepayConstants.RepayUnit.DAY.equals(loan.getType()
						.getRepayTimeUnit())) {
					endDate = DateUtil.addDay(loan.getInterestBeginTime(),
							loan.getDeadline());
				} else if (RepayConstants.RepayUnit.MONTH.equals(loan.getType()
						.getRepayTimeUnit())) {
					endDate = DateUtil.addMonth(loan.getInterestBeginTime(),
							loan.getDeadline());
				}
				/**caijinmin 201412231623 add 判断还款单元是天还是月，计算借款到期日 end**/
				contractContent = node.getNodeBody().getBody();
				contractContent = contractContent
						.replace("#{loanId}", Strings.nullToEmpty(loan.getId()))
						.replace("#{loanName}", Strings.nullToEmpty(loan.getName()))
						.replace("#{actualMoney}", loan.getMoney().toString())
						.replace("#{investList}", table.outerHtml())
						.replace("#{loanRepayList}", table0.outerHtml())
						.replace(
								"#{interest}",
								loan.getRepayRoadmap().getRepayInterest()
										.toString())
						.replace("#{loanerRealname}",
								Strings.nullToEmpty(loan.getUser().getRealname()))
						.replace("#{loanerIdCard}",
								Strings.nullToEmpty(loan.getUser().getIdCard()))
						.replace("#{loanerUsername}", loan.getUser().getUsername())
						.replace("#{guaranteeCompanyName}",
								Strings.nullToEmpty(loan.getGuaranteeCompanyName()))
						.replace(
								"#{giveMoneyDate}",
								Strings.nullToEmpty(DateUtil.DateToString(
										loan.getGiveMoneyTime(),
										DateStyle.YYYY_MM_DD_CN)))
						.replace("#{rate}", loan.getRatePercent().toString())
						.replace("#{deadline}",String.valueOf(loan.getRepayRoadmap().getRepayPeriod()))
						.replace(
								"#{interestBeginTime}",
								Strings.nullToEmpty(DateUtil.DateToString(
										loan.getInterestBeginTime(),
										DateStyle.YYYY_MM_DD_CN)))
						.replace(
								"#{interestEndTime}",
								// FIXME:需要根据借款类型，来计算借款到期日
								Strings.nullToEmpty(DateUtil.DateToString(endDate,
										DateStyle.YYYY_MM_DD_CN)))
						.replace("#{loanPurpose}", loan.getLoanPurpose())
						.replace("#{repayAllMoney}",
								loan.getRepayRoadmap().getRepayMoney().toString())
						// FIXME:需要根据借款类型，来显示还款日
						.replace(
								"#{repayDay}",
								String.valueOf(DateUtil.getDay(loan.getLoanRepays().get(0).getRepayDay())))
						.replace("#{investTransferList}", "债权转让列表");
			/*StringBuilder str=new StringBuilder("{");
			str.append("\"contractContent\":\"").append(contractContent).append("\"");
			str.append("}");*/
			out.encryptResult(contractContent);
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
