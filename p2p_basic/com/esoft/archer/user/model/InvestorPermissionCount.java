package com.esoft.archer.user.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 记录实名认真次数
 */
@Entity
@Table(name = "investor_permission_count")
public class InvestorPermissionCount implements java.io.Serializable {

	// Fields

	private static final long serialVersionUID = -4560631695514690692L;

	private String id;
	private Integer count;

	/** default constructor */
	public InvestorPermissionCount() {
	}

	/** minimal constructor */
	public InvestorPermissionCount(String id, String userId, Integer count, Date registerTime) {
		this.id = id;
		this.count = count;
	}

	// Property accessors
	@Id
	// @GeneratedValue(generator = "system-uuid")
	// @GenericGenerator(name = "system-uuid", strategy = "uuid.hex")
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}
	@Column(name = "count", nullable = false, length = 50)
	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
	


}