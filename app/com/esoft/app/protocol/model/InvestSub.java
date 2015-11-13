package com.esoft.app.protocol.model;

import java.util.Date;

import com.esoft.jdp2p.invest.model.Invest;

public class InvestSub extends Invest{
	private String loanId;//项目编号
	private String loanName;//项目名称
	private double jd;//进度
	private double unPaidMoney;//待还金额
	private double paidFee;//已还费用
	private String userId;//投资编号
	private String userName;//投资人名称
	private Date completeTime;//完成时间
	public String getLoanId() {
		return loanId;
	}
	public void setLoanId(String loanId) {
		this.loanId = loanId;
	}
	public String getLoanName() {
		return loanName;
	}
	public void setLoanName(String loanName) {
		this.loanName = loanName;
	}
	public double getJd() {
		return jd;
	}
	public void setJd(double jd) {
		this.jd = jd;
	}
	public double getUnPaidMoney() {
		return unPaidMoney;
	}
	public void setUnPaidMoney(double unPaidMoney) {
		this.unPaidMoney = unPaidMoney;
	}
	public double getPaidFee() {
		return paidFee;
	}
	public void setPaidFee(double paidFee) {
		this.paidFee = paidFee;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Date getCompleteTime() {
		return completeTime;
	}
	public void setCompleteTime(Date completeTime) {
		this.completeTime = completeTime;
	}
	
	
	
	
}
