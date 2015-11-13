package com.esoft.archer.experience.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "experienceinvestment")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class Experienceinvestment {
    private String id;

    private String userid;//用户id

    private Integer pday;//天数
    
    private String eid;//体验金表id

    private String ename;//体验金名称
    
    private Double money;//体验金

    private Double pinterestrate;//利率

    private Double profit;//收益钱数

    private Date startdate;//投资日期

    private Date enddate;//结束日期
    
    private String etype;//状态

    private String rmk11;

    private String rmk12;

    private String rmk13;
    
    public Experienceinvestment(){
    	
    }
    
    public Experienceinvestment(String id,String userid,Integer pday,String ename,String eid,Double pinterestrate,Double profit,Date startdate,Date enddate,String etype,String rmk11,String rmk12,String rmk13){
    	this.id = id;
    	this.userid = userid;
    	this.pday = pday;
    	this.eid = eid;
    	this.ename = ename;
    	this.pinterestrate = pinterestrate;
    	this.profit = profit;
    	this.startdate = startdate;
    	this.enddate = enddate;
    	this.etype = etype;
    	this.rmk11 = rmk11;
    	this.rmk12 = rmk12;
    	this.rmk13 = rmk13;
    }

    @Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "userid")
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid == null ? null : userid.trim();
    }

    @Column(name = "pday")
    public Integer getPday() {
        return pday;
    }

    public void setPday(Integer pday) {
        this.pday = pday;
    }

    @Column(name = "ename")
    public String getEname() {
        return ename;
    }

    public void setEname(String ename) {
        this.ename = ename;
    }
    
    @Column(name = "eid")
    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    @Column(name = "pinterestrate")
    public Double getPinterestrate() {
        return pinterestrate;
    }

    public void setPinterestrate(Double pinterestrate) {
        this.pinterestrate = pinterestrate;
    }

    @Column(name = "profit")
    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
    }

    @Column(name = "startdate")
    public Date getStartdate() {
        return startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    @Column(name = "enddate")
    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    @Column(name = "rmk11")
    public String getRmk11() {
        return rmk11;
    }

    public void setRmk11(String rmk11) {
        this.rmk11 = rmk11 == null ? null : rmk11.trim();
    }

    @Column(name = "rmk12")
    public String getRmk12() {
        return rmk12;
    }

    public void setRmk12(String rmk12) {
        this.rmk12 = rmk12 == null ? null : rmk12.trim();
    }

    @Column(name = "rmk13")
    public String getRmk13() {
        return rmk13;
    }

    public void setRmk13(String rmk13) {
        this.rmk13 = rmk13 == null ? null : rmk13.trim();
    }

    @Column(name = "etype")
	public String getEtype() {
		return etype;
	}

	public void setEtype(String etype) {
		this.etype = etype;
	}
	
	@Column(name = "money")
	public Double getMoney() {
		return money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}
	
    
}