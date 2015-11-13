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
@Table(name = "experience_coupons")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class ExperienceCoupons {
    private String id;
    
    private Experienceproject experienceproject;//体验项目编号

    private String pname;//体验项目名

    private Double money;//需要投资钱数
    
    private Double pinterestrate;//利率

    private Integer pday;//天数
    
    private Integer pronumber;//数量
    
    private String couponsNum;//优惠券序号

    private Date startdate;
    
    private Date enddate;

    private String etype;//状态

    
    private String rmk1;

    private String rmk2;

    private String rmk3;

    public ExperienceCoupons(){
    	
    }
   
    
    public ExperienceCoupons(String id, Experienceproject experienceproject,
			String pname, Double money, Double pinterestrate, Integer pday,
			Integer pronumber, String couponsNum, Date startdate, Date enddate,
			String etype, String rmk1, String rmk2, String rmk3) {
		this.id = id;
		this.experienceproject = experienceproject;
		this.pname = pname;
		this.money = money;
		this.pinterestrate = pinterestrate;
		this.pday = pday;
		this.pronumber = pronumber;
		this.couponsNum = couponsNum;
		this.startdate = startdate;
		this.enddate = enddate;
		this.etype = etype;
		this.rmk1 = rmk1;
		this.rmk2 = rmk2;
		this.rmk3 = rmk3;
	}




	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "pname")
    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname == null ? null : pname.trim();
    }

    @Column(name = "money")
    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    @Column(name = "pday")
    public Integer getPday() {
        return pday;
    }

    public void setPday(Integer pday) {
        this.pday = pday;
    }

    @Column(name = "rmk1")
    public String getRmk1() {
        return rmk1;
    }

    public void setRmk1(String rmk1) {
        this.rmk1 = rmk1 == null ? null : rmk1.trim();
    }

    @Column(name = "rmk2")
    public String getRmk2() {
        return rmk2;
    }

    public void setRmk2(String rmk2) {
        this.rmk2 = rmk2 == null ? null : rmk2.trim();
    }

    @Column(name = "rmk3")
    public String getRmk3() {
        return rmk3;
    }

    public void setRmk3(String rmk3) {
        this.rmk3 = rmk3 == null ? null : rmk3.trim();
    }
    
    @Column(name = "pinterestrate")
	public Double getPinterestrate() {
		return pinterestrate;
	}
	public void setPinterestrate(Double pinterestrate) {
		this.pinterestrate = pinterestrate;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "projectid")
	public Experienceproject getExperienceproject() {
		return experienceproject;
	}
	public void setExperienceproject(Experienceproject experienceproject) {
		this.experienceproject = experienceproject;
	}
	@Column(name = "pronumber")
	public Integer getPronumber() {
		return pronumber;
	}
	public void setPronumber(Integer pronumber) {
		this.pronumber = pronumber;
	}
	@Column(name = "couponsNum")
	public String getCouponsNum() {
		return couponsNum;
	}
	public void setCouponsNum(String couponsNum) {
		this.couponsNum = couponsNum;
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
	@Column(name = "etype")
	public String getEtype() {
		return etype;
	}
	public void setEtype(String etype) {
		this.etype = etype;
	}
	
	
    
}