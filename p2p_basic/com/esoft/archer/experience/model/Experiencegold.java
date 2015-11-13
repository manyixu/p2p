package com.esoft.archer.experience.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "experiencegold")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class Experiencegold {
    private String id;

    private String userid;//用户id

    private Double money;//体验金钱数

    private String utype;//状态

    private Date etime;//注册日期
    
    private String projectid;//	项目id
    private String projectname;//	项目名（除新手体验金外）
 
    private Integer pday;//	体验天数
    private Double pinterestrate;//	年利率
    private Double profit;//	到期收益  

    
    private Date rmk21;

    private String rmk22;

    private String rmk23;
    /**
     * Integer id,String userid,Double money,String utype,String etime,String rmk21,String rmk22,String rmk23
     */
    public Experiencegold(){
    	
    }
    public Experiencegold(String id,String userid,Double money,String utype,Date etime,String projectid,String projectname,Integer pday,Double pinterestrate,Double profit,Date rmk21,String rmk22,String rmk23){
    	this.id = id;
    	this.userid = userid;
    	this.money = money;
    	this.utype = utype;
    	this.etime = etime;
    	this.rmk21 = rmk21;
    	this.rmk22 = rmk22;
    	this.rmk23 = rmk23;
    	
    	
    	this.projectid = projectid;
    	this.projectname = projectname;
    	this.pday = pday;
    	this.pinterestrate = pinterestrate;
    	this.profit = profit;
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

    @Column(name = "money")
    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    @Column(name = "utype")
    public String getUtype() {
        return utype;
    }

    public void setUtype(String utype) {
        this.utype = utype == null ? null : utype.trim();
    }

    @Column(name = "etime")
    public Date getEtime() {
        return etime;
    }

    public void setEtime(Date etime) {
        this.etime = etime;
    }

    @Column(name = "rmk21")
    public Date getRmk21() {
        return rmk21;
    }

    public void setRmk21(Date rmk21) {
        this.rmk21 = rmk21;
    }

    @Column(name = "rmk22")
    public String getRmk22() {
        return rmk22;
    }

    public void setRmk22(String rmk22) {
        this.rmk22 = rmk22 == null ? null : rmk22.trim();
    }

    @Column(name = "rmk23")
    public String getRmk23() {
        return rmk23;
    }

    public void setRmk23(String rmk23) {
        this.rmk23 = rmk23 == null ? null : rmk23.trim();
    }
    @Column(name = "projectid")
	public String getProjectid() {
		return projectid;
	}
	public void setProjectid(String projectid) {
		this.projectid = projectid;
	}
	 @Column(name = "projectname")
	public String getProjectname() {
		return projectname;
	}
	public void setProjectname(String projectname) {
		this.projectname = projectname;
	}
	 @Column(name = "pday")
	public Integer getPday() {
		return pday;
	}
	public void setPday(Integer pday) {
		this.pday = pday;
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
    
}