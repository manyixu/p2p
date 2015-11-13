package com.esoft.jdp2p.loan.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.esoft.archer.user.model.User;

@Entity
@Table(name = "start_interest_alert_count")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class StartInterestAlertCount {

	private String id;
	/**
	 * 操作时间
	 */
	private String userId;
	/**
	 * 操作项目编号
	 */
	private String loanId;
	
	/**
	 * 发送时间
	 */
	private Date time;
	/**
	 * 备注1
	 */
	private String rmk1;
	/**
	 * 备注2
	 */
	private String rmk2;
	/**
	 * 备注3
	 */
	private String rmk3;

	public StartInterestAlertCount() {
	}

	public StartInterestAlertCount(String id, String user,String loan,Date time,String rmk1,String rmk2,String rmk3) {
		this.id = id;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return id;
	}
	
	@Column(name = "time")
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	
	@Column(name = "user_id", length = 20)
	public String getUserId() {
		return this.userId;
	}
	
	@Column(name = "loan_id", length = 20)
	public String getLoanId() {
		return this.loanId;
	}
	@Column(name = "rmk1", length = 255)
	public String getRmk1() {
		return rmk1;
	}

	public void setRmk1(String rmk1) {
		this.rmk1 = rmk1;
	}
	@Column(name = "rmk2", length = 255)
	public String getRmk2() {
		return rmk2;
	}

	public void setRmk2(String rmk2) {
		this.rmk2 = rmk2;
	}
	@Column(name = "rmk3", length = 255)
	public String getRmk3() {
		return rmk3;
	}

	public void setRmk3(String rmk3) {
		this.rmk3 = rmk3;
	}

	public void setId(String id) {
		this.id = id;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public void setLoanId(String loanId) {
		this.loanId = loanId;
	}


}
