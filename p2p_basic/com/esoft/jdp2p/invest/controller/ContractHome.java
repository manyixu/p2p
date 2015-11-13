package com.esoft.jdp2p.invest.controller;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.esoft.archer.node.model.Node;
import com.esoft.archer.system.controller.DictUtil;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.DateStyle;
import com.esoft.core.util.DateUtil;
import com.esoft.jdp2p.invest.InvestConstants.InvestStatus;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.invest.model.TransferApply;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.repay.RepayConstants;
import com.esoft.jdp2p.repay.model.LoanRepay;
import com.esoft.jdp2p.risk.model.FeeConfig;
import com.google.common.base.Strings;
import com.lowagie.text.pdf.BaseFont;

@Component
@Scope(ScopeType.VIEW)
public class ContractHome {

	@Resource
	private HibernateTemplate ht;

	@Resource
	LoginUserInfo loginUserInfo;
	
	@Resource
	private DictUtil dictUtil;

	private String contractContent;
/**
 * 下载合同
 * @param fileName
 */
	public void contractPdfDownload(String fileName) {
		String body = contractContent.replace("&nbsp;", "&#160;");
		body = "<html><head></head><body style=\"font-family:'SimSun';\">"
				+ body + "</body></html>";
		StringReader contentReader = new StringReader(body);
		InputSource source = new InputSource(contentReader);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		ServletOutputStream sos = null;
		try {
			documentBuilder = factory.newDocumentBuilder();
			org.w3c.dom.Document xhtmlContent = documentBuilder.parse(source);
			ITextRenderer renderer = new ITextRenderer();
			ITextFontResolver fontResolver = renderer.getFontResolver();
			fontResolver.addFont(FacesUtil.getRealPath("SIMSUN.TTC"),
					BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
			renderer.setDocument(xhtmlContent, "");
			renderer.layout();
			FacesUtil.getHttpServletResponse().setHeader(
					"Content-Disposition",
					"attachment; filename=\""
							+ new String((fileName + ".pdf").getBytes("utf-8"),
									"iso8859-1") + "\"");
			FacesUtil.getHttpServletResponse().setContentType(
					"application/pdf;charset=UTF-8");
			sos = FacesUtil.getHttpServletResponse().getOutputStream();
			renderer.createPDF(sos);
			FacesUtil.getCurrentInstance().responseComplete();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (com.lowagie.text.DocumentException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(sos);
		}
	}
	/**
	 * 下载范本合同
	 * @param fileName
	 */
	public void contractPdfDownloadfan(String fileName) {
		//获取合同模板
		Node node = ht.get(Node.class, "quartet_contract_fan");
		contractContent = node.getNodeBody().getBody();
		String body = contractContent.replace("&nbsp;", "&#160;");
		body = "<html><head></head><body style=\"font-family:'SimSun';\">"
				+ body + "</body></html>";
		StringReader contentReader = new StringReader(body);
		InputSource source = new InputSource(contentReader);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		ServletOutputStream sos = null;
		try {
			documentBuilder = factory.newDocumentBuilder();
			org.w3c.dom.Document xhtmlContent = documentBuilder.parse(source);
			ITextRenderer renderer = new ITextRenderer();
			ITextFontResolver fontResolver = renderer.getFontResolver();
			fontResolver.addFont(FacesUtil.getRealPath("SIMSUN.TTC"),
					BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
			renderer.setDocument(xhtmlContent, "");
			renderer.layout();
			FacesUtil.getHttpServletResponse().setHeader(
					"Content-Disposition",
					"attachment; filename=\""
							+ new String((fileName + ".pdf").getBytes("utf-8"),
									"iso8859-1") + "\"");
			FacesUtil.getHttpServletResponse().setContentType(
					"application/pdf;charset=UTF-8");
			sos = FacesUtil.getHttpServletResponse().getOutputStream();
			renderer.createPDF(sos);
			FacesUtil.getCurrentInstance().responseComplete();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (com.lowagie.text.DocumentException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(sos);
		}
	}

	/**
	 * 替换 借款合同正文中的信息
	 * 
	 * @param loanId
	 * @return
	 */
	public String dealLoanContractContent(String loanId) {
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
		if (contractContent == null) {
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
		}
		return contractContent;
	}
	
	
	
	/**
	 * 替换债权借款合同正文中的信息
	 * 
	 * @param loanId
	 * @return
	 */
	@SuppressWarnings({ "static-access" })
	public String dealLoanContractTransferContent(String inevestId) {
		
		// 获取本次投资 也就是购买人投资信息
		Invest investmom = ht.get(Invest.class, inevestId);
		//获取债权信息
		TransferApply transferApply = investmom.getTransferApply();
		//获取转让前信息
		Invest invest = transferApply.getInvest();
		//获取项目列表
		Loan loan = ht.get(Loan.class, invest.getLoan().getId());
		//获取合同模板
		Node node = ht.get(Node.class, loan.getTransferType());
		FeeConfig feeConfig = ht.get(FeeConfig.class, "transfer");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
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
		
		//替换合同模板中的内容
		if (contractContent == null) {
			contractContent = node.getNodeBody().getBody();
			contractContent = contractContent
				//合同编号
				.replace("#{contractId}", Strings.nullToEmpty(loan.getId()))
				//时间
				.replace("#{time}", transferApply.getDeadline().toString())
				//转让人(甲方)
				.replace("#{realname_zr}", invest.getUser().getRealname())
				//转让人身份证
				.replace("#{idCard_zr}", invest.getUser().getIdCard())
				//转让人用户名
				.replace("#{username_zr}", invest.getUser().getUsername())
				//购买人
				.replace("#{realname_gm}", investmom.getUser().getRealname())
				//购买人身份证
				.replace("#{idCard_gm}", investmom.getUser().getIdCard())
				//购买人用户名
				.replace("#{username_gm}", investmom.getUser().getUsername())
				
				//项目编号
				.replace("#{loanId}", loan.getId())
				//借款人姓名/名称
				.replace("#{realname_loan}", loan.getUser().getRealname())
				//借款人证件类型及号码
				.replace("#{idCard_loan}", loan.getUser().getIdCard())
				//借款人用户名
				.replace("#{username_loan}", loan.getUser().getUsername())
				//借款本金总数额
				.replace("#{money_loan}", loan.getMoney().toString())
				
				//本次转让的债权本金数额
				.replace("#{corpus}",String.valueOf(transferApply.getCorpus()))
				//借款年利率
				.replace("#{rate_loan}", loan.getRatePercent().toString())
				//借款用途
				.replace("#{purpose_loan}", loan.getLoanPurpose())
				//原借款期限
				.replace("#{deadline_loan}", loan.getDeadline().toString())
					//替换日期(月/天)
				.replace("#{deadline_format}", dictUtil.getValue("repay_unit",loan.getType().getRepayTimeUnit()))
					//起始日期
				/*.replace("#{jk_begin}", loan.getCommitTime().toString())
					//结束日期
				.replace("#{jk_end}", loan.getExpectTime().toString())*/
				.replace(
							"#{jk_begin}",
							Strings.nullToEmpty(DateUtil.DateToString(
									loan.getInterestBeginTime(),
									DateStyle.YYYY_MM_DD_CN)))
					.replace(
							"#{jk_end}",
							// FIXME:需要根据借款类型，来计算借款到期日
							Strings.nullToEmpty(DateUtil.DateToString(endDate,
									DateStyle.YYYY_MM_DD_CN)))
				//计息方式
				.replace("#{repayType_loan}", loan.getType().getDescription())
				//付息方式
				.replace("#{nextRepayDay}",simpleDateFormat.format(loan.getCommitTime()))
				//还本方式
				//.replace("#{repayDay}",simpleDateFormat.format(loan.getCommitTime()))
				//转让价款
				.replace("#{money_gm}", String.valueOf(transferApply.getCorpus()-transferApply.getPremium()))
				//转让管理费
				.replace("#{fee_gm}", String.valueOf(feeConfig.getFee()*(transferApply.getCorpus()-transferApply.getPremium())))
				//月偿还本息数额
				.replace("#{repayAllMoney}",loan.getRepayRoadmap().getRepayMoney().toString())
				//还款日期
				.replace("#{repayDay}",
							String.valueOf(DateUtil.getDay(loan.getLoanRepays().get(0).getRepayDay())))
				//转让日期
				.replace("#{time_gm}", transferApply.getApplyTime().toString());
		}
		return contractContent;
	}
	
	
}
