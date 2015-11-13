package com.esoft.app.protocol.model;

import com.esoft.archer.user.model.UserBill;

public class UserBillSub extends UserBill{
	private String moneyStr;
	private String balanceStr;
	private String frozenMoneyStr;
	public String getMoneyStr() {
		return moneyStr;
	}
	public void setMoneyStr(String moneyStr) {
		this.moneyStr = moneyStr;
	}
	public String getBalanceStr() {
		return balanceStr;
	}
	public void setBalanceStr(String balanceStr) {
		this.balanceStr = balanceStr;
	}
	public String getFrozenMoneyStr() {
		return frozenMoneyStr;
	}
	public void setFrozenMoneyStr(String frozenMoneyStr) {
		this.frozenMoneyStr = frozenMoneyStr;
	}
}
