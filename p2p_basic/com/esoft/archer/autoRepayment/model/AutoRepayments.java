package com.esoft.archer.autoRepayment.model;

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
/**
 * 划扣实体
 * @author Administrator
 *
 */
@Entity
@Table(name = "autoRepayment")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class AutoRepayments {
    private Integer id;

    private String userid;//用户id

    private String utype;//状态

    private String utime;//注册日期
    
    private String bak11;

    private String bak12;

    public AutoRepayments(){
    	
    }
    public AutoRepayments(Integer id,String userid,String utype,String utime,String bak11,String bak12){
    	this.id = id;
    	this.userid = userid;
    	this.utype = utype;
    	this.utime = utime;
    	this.bak11 = bak11;
    	this.bak12 = bak12;
    }

    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY) 
	@Column(name = "id", unique = true, nullable = false, length = 23)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "userid")
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid == null ? null : userid.trim();
    }

    @Column(name = "utype")
    public String getUtype() {
        return utype;
    }

    public void setUtype(String utype) {
        this.utype = utype == null ? null : utype.trim();
    }

    @Column(name = "utime")
    public String getUtime() {
        return utime;
    }

    public void setUtime(String utime) {
        this.utime = utime == null ? null : utime.trim();
    }

    @Column(name = "bak11")
    public String getBak11() {
        return bak11;
    }

    public void setBak11(String bak11) {
        this.bak11 = bak11 == null ? null : bak11.trim();
    }

    @Column(name = "bak12")
    public String getBak12() {
        return bak12;
    }

    public void setBak12(String bak12) {
        this.bak12 = bak12 == null ? null : bak12.trim();
    }
}