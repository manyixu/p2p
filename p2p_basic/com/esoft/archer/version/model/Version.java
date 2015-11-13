package com.esoft.archer.version.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "version")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class Version {
	
	private String id;
	/**
	 * 版本号
	 */
	private String version;
	/**
	 * 更新时间
	 */
	private Date time;
	/**
	 * 备注  更新内容
	 */
	private String rmk;
	
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name = "version", nullable = false, length = 32)
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	@Column(name = "time", nullable = false, length = 19)
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	@Column(name = "rmk", nullable = false, length = 1024)
	public String getRmk() {
		return rmk;
	}
	public void setRmk(String rmk) {
		this.rmk = rmk;
	}
	
	

}
