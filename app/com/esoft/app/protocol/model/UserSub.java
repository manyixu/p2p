package com.esoft.app.protocol.model;

import com.esoft.archer.user.model.User;

public class UserSub{
	
	private String id;
	
	private String email;
	private String mobileNumber;
	
	private String photo;
	
	/**
	 * 提供给APP端的参数：用户是否实名认证：1已认证，0未认证
	 */
	private String isRealName;
	/**
	 * 提供给APP端的参数：用户可用余额
	 */
	private double availablebalance;
	/**
	 * 提供给APP端的参数：用户总收益
	 */
	private double totalIncome;
	

	

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getIsRealName() {
		return isRealName;
	}

	public void setIsRealName(String isRealName) {
		this.isRealName = isRealName;
	}

	public double getAvailablebalance() {
		return availablebalance;
	}

	public void setAvailablebalance(double availablebalance) {
		this.availablebalance = availablebalance;
	}

	public double getTotalIncome() {
		return totalIncome;
	}

	public void setTotalIncome(double totalIncome) {
		this.totalIncome = totalIncome;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	

}
