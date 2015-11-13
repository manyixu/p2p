package com.esoft.app.protocol.model;

import com.esoft.jdp2p.loan.model.Loan;
/**
 * 
 * 自定义Loan
 *
 */
public class LoanSub extends Loan{
	private String jd;//进度
	private String lastStrTime;//最后时间
	private double myRatePercent;//年化利率
	private double sybj;//剩余本金
	/**
	 * 还款账单（按天还、按月还）
	 */
	private String repayTimeUnit;
	
	public String getJd() {
		return jd;
	}

	public void setJd(String jd) {
		this.jd = jd;
	}

	public String getLastStrTime() {
		return lastStrTime;
	}

	public void setLastStrTime(String lastStrTime) {
		this.lastStrTime = lastStrTime;
	}

	public double getMyRatePercent() {
		return myRatePercent;
	}

	public void setMyRatePercent(double myRatePercent) {
		this.myRatePercent = myRatePercent;
	}

	public double getSybj() {
		return sybj;
	}

	public void setSybj(double sybj) {
		this.sybj = sybj;
	}

	public String getRepayTimeUnit() {
		return repayTimeUnit;
	}

	public void setRepayTimeUnit(String repayTimeUnit) {
		this.repayTimeUnit = repayTimeUnit;
	}
	
}
