package com.esoft.archer.autoRepayment.controller;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.autoRepayment.model.AutoRepayments;
import com.esoft.archer.autoRepayment.service.AutoRepaymentMapper;
import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.config.ConfigConstants;
import com.esoft.archer.config.service.ConfigService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.bankcard.BankCardConstants;
import com.esoft.jdp2p.bankcard.model.BankCardDaihuakou;
import com.google.gson.Gson;
/**
 * 划扣功能
 * @author Administrator
 *
 */
@Component
@Scope(ScopeType.VIEW)
public class AutoRepaymentController extends EntityHome<AutoRepayments> {
	@Resource
	HibernateTemplate ht;
	@Resource
	private LoginUserInfo loginUser;
	@Resource
	private AutoRepaymentMapper autoRepaymentMapper;
	@Resource
	ConfigService configService;
	//查询带条件的
	private List<AutoRepayments> findAllwhere;
	//查询银行卡表
	private List<BankCardDaihuakou>banksList;
	
	@Override
	@Transactional(readOnly=false)
	public String save() {
		return null;
	}
	
	/**
	 * 保存修改
	 * @param autoRepayments
	 */
	@Transactional(readOnly=false)
	public String saveorupdate(String utype){
		BankCardDaihuakou bc=new BankCardDaihuakou();
		String sql="from BankCardDaihuakou where user.id='"+loginUser.getLoginUserId()+"' ";
		bc = (BankCardDaihuakou) ht.find(sql).get(0);
			//查询出可以自动的银行
			String startInterestAlert = "0";
			try {
				startInterestAlert = configService.getConfigValue(bc.getBankNo());
			} catch (ObjectNotFoundException onfe) {
				onfe.printStackTrace();
			}
			if(utype.equals("Y")){
			//匹配
			if(startInterestAlert == null || startInterestAlert.equals("0")){
				FacesUtil.addErrorMessage("您所填写的银行卡不支持自动代划扣！");
				return null;
			}
			}
			
		
		String hsql="from AutoRepayments where userid='"+loginUser.getLoginUserId()+"'";
		List alist = autoRepaymentMapper.find(hsql);
		AutoRepayments autoRepayments=new AutoRepayments();
		//如果新表中有记录则取出
		if(alist.size()>0){
			autoRepayments = (AutoRepayments) alist.get(0);
		}else{
			//如果没有记录则输入进去
			autoRepayments.setUserid(loginUser.getLoginUserId());
		}
		autoRepayments.setUtime(DateUtil.DateToString(new Date(), "yyyy-MM-dd"));
		autoRepayments.setUtype(utype);
		//如果bankCard不为空则表示新卡号用该号
		//如果不为空则以旧号为主
		autoRepaymentMapper.saveOrUpdate(autoRepayments);
		FacesUtil.addInfoMessage("操作成功！！！");
		return null;
	}
	

	public List<AutoRepayments> getFindAllwhere() {
		String hsql = "from AutoRepayments where userid='"+loginUser.getLoginUserId()+"'";
		List<AutoRepayments> aList = autoRepaymentMapper.find(hsql);
		if(aList.size()>0){
			return aList;
		}else{
			AutoRepayments a = new AutoRepayments();
			aList.add(a);
			//a.setUtype("N");
			return aList;
		}
	}

	public void setFindAllwhere(List<AutoRepayments> findAllwhere) {
		this.findAllwhere = findAllwhere;
	}

	public List<BankCardDaihuakou> getBanksList() {
		String hql="from BankCardDaihuakou where user.id = '"+loginUser.getLoginUserId()+"' ";
		banksList=ht.find(hql);
		if(banksList.size()>0){
			//bank=banksList.get(0).getCardNo();//暂时赋值给一个变量，下一个方法用
		}else{
			BankCardDaihuakou bank2=new BankCardDaihuakou();
			bank2.setId("");
			banksList.add(bank2);
		}
		return banksList;
	}

	public void setBanksList(List<BankCardDaihuakou> banksList) {
		this.banksList = banksList;
	}

}
