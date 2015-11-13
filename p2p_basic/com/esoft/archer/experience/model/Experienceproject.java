package com.esoft.archer.experience.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "experienceproject")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class Experienceproject {
    private String id;

    private String pname;//体验项目名

    private Double money;//需要投资钱数
    
    private Double pinterestrate;//利率

    private Integer pday;//天数

    private Double profit;//利率

    private String rmk1;

    private String rmk2;

    private String rmk3;

    public Experienceproject(){
    	
    }
    public Experienceproject(String id,String pname,Double money,Double pinterestrate,Integer pday,Double profit,String rmk1,String rmk2,String rmk3){
    	this.id = id;
    	this.pname = pname;
    	this.money = money;
    	this.pinterestrate = pinterestrate;
    	this.pday = pday;
    	this.profit = profit;
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

    @Column(name = "profit")
    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
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
    
}