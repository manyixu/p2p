package com.esoft.jdp2p.loan.model;

import java.util.Date;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "operation_log")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class OperationLog {

	private Integer id;
	/**
	 * 操作时间
	 */
	private Date operationDate;
	/**
	 * 操作项目编号
	 */
	private String operationId;
	/**
	 * 操作IP
	 */
	private String operationIp;
	/**
	 * 操作项目名称
	 */
	private String operationName;
	/**
	 * 操作动作
	 */
	private String operationAction;
	/**
	 * 操作类型
	 */
	private String operationType;
	/**
	 * 操作人
	 */
	private String operationUser;
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
	/**
	 *备注4
	 */
	private String rmk4;

	public OperationLog() {
	}

	public OperationLog(Integer id, Date operationDate, String operationId,String operationIp,String operationName,String operationAction,String operationType,String operationUser,String rmk1,String rmk2,String rmk3,String rmk4) {
		this.id = id;
		this.operationDate = operationDate;
		this.operationId = operationId;
		this.operationIp = operationIp;
		this.operationName = operationName;
		this.operationAction = operationAction;
		this.operationType = operationType;
		this.operationUser = operationUser;
		this.rmk1 = rmk1;
		this.rmk2 = rmk2;
		this.rmk3 = rmk3;
		this.rmk4 = rmk4;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY) 
	@Column(name = "id", unique = true, nullable = false, length = 20)
	public Integer getId() {
		return id;
	}
	@Column(name = "operation_date")
	public Date getOperationDate() {
		return operationDate;
	}
	
	@Column(name = "operation_ip", length = 20)
	public String getOperationIp() {
		return operationIp;
	}

	public void setOperationIp(String operationIp) {
		this.operationIp = operationIp;
	}

	public void setOperationDate(Date operationDate) {
		this.operationDate = operationDate;
	}
	@Column(name = "operation_id", length = 255)
	public String getOperationId() {
		return operationId;
	}

	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}
	@Column(name = "operation_name", length = 522)
	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}
	@Column(name = "operation_action", length = 20)
	public String getOperationAction() {
		return operationAction;
	}

	public void setOperationAction(String operationAction) {
		this.operationAction = operationAction;
	}
	@Column(name = "operation_type", length = 20)
	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	@Column(name = "operation_user", length = 20)
	public String getOperationUser() {
		return operationUser;
	}

	public void setOperationUser(String operationUser) {
		this.operationUser = operationUser;
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
	@Column(name = "rmk4", length = 255)
	public String getRmk4() {
		return rmk4;
	}

	public void setRmk4(String rmk4) {
		this.rmk4 = rmk4;
	}

	public void setId(Integer id) {
		this.id = id;
	}


}
