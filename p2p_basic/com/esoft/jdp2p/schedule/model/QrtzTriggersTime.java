package com.esoft.jdp2p.schedule.model;

// default package

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.esoft.archer.user.model.User;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.loan.model.Loan;

/**
 * 定时任务时间管理
 */
@Entity
@Table(name = "qrtz_triggers_time")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "entityCache")
public class QrtzTriggersTime {

	/**
	 * 编号
	 */
	private String id;
	/**
	 * 时间
	 */
	private String time;
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	@Column(name = "time")
	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
}