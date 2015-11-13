package com.esoft.archer.holiday.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "holiday_table")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class HolidayTable implements Serializable {
	
	private String id;
	/**
	 * 用户id
	 */
	private String userid;
	/**
	 * 状态
	 */
	private String hstate;
	/**
	 * 节日类型号（年月+节日手写字母）
	 */
	private String htype;
	/**
	 * 钱
	 */
	private Double money;
	/**
	 * 更新时间
	 */
	private Date starttime;
	/**
	 * 备注  更新内容
	 */
	private String rmk1;
	/**
	 * 备注  更新内容
	 */
	private String rmk2;
	/**
	 * 备注  更新内容
	 */
	private String rmk3;
	
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name = "userid", nullable = false, length = 32)
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	@Column(name = "hstate", nullable = false, length = 2)
	public String getHstate() {
		return hstate;
	}
	public void setHstate(String hstate) {
		this.hstate = hstate;
	}
	@Column(name = "htype", nullable = false, length = 20)
	public String getHtype() {
		return htype;
	}
	public void setHtype(String htype) {
		this.htype = htype;
	}
	
	@Column(name = "starttime", nullable = false, length = 32)
	public Date getStarttime() {
		return starttime;
	}
	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}
	@Column(name = "rmk1", nullable = false, length = 32)
	public String getRmk1() {
		return rmk1;
	}
	public void setRmk1(String rmk1) {
		this.rmk1 = rmk1;
	}
	@Column(name = "rmk2", nullable = false, length = 32)
	public String getRmk2() {
		return rmk2;
	}
	public void setRmk2(String rmk2) {
		this.rmk2 = rmk2;
	}
	@Column(name = "rmk3", nullable = false, length = 32)
	public String getRmk3() {
		return rmk3;
	}
	public void setRmk3(String rmk3) {
		this.rmk3 = rmk3;
	}
	@Column(name = "money", nullable = false, length = 23)
	public Double getMoney() {
		return money;
	}
	public void setMoney(Double money) {
		this.money = money;
	}
}
