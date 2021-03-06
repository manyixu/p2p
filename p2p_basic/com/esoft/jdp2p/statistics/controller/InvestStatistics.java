package com.esoft.jdp2p.statistics.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.esoft.app.protocol.util.NumberUtil;
import com.esoft.core.annotations.ScopeType;
import com.esoft.jdp2p.invest.InvestConstants;
import com.esoft.jdp2p.invest.model.InvestPulished;
import com.esoft.jdp2p.loan.LoanConstants.LoanStatus;

/**
 * 投资统计
 * 
 * @author Administrator
 * 
 */
@Component
@Scope(ScopeType.REQUEST)
public class InvestStatistics {

	@Resource
	private HibernateTemplate ht;

	/**
	 * 已收本金
	 * 
	 * @return
	 */
	public double getReceivedCorpus(String userId) {
		// InvestRepay
		String hql = "Select sum(corpus) from InvestRepay where time is not null and invest.user.id = ? and time <= ?";
		List<Object> result = ht.find(hql, userId, new Date());

		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}

		return money;
	}
	
	/**
	 * 已收本金(微信)
	 * 
	 * @return
	 */
	public String getReceivedCorpusWeiXin(String userId) {
		// InvestRepay
		String hql = "Select sum(corpus) from InvestRepay where time is not null and invest.user.id = ? and time <= ?";
		List<Object> result = ht.find(hql, userId, new Date());

		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}

		return NumberUtil.getNumber(money);
	}
	
	/**
	 * 所有用户的已收本金return 
	 * 
	 * @return
	 */
	public double getReceivedCorpus() {
		// InvestRepay
		String hql = "Select sum(corpus) from InvestRepay where time is not null and time <= ?";
		List<Object> result = ht.find(hql, new Date());

		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}

		return money;
	}

	/**
	 * 应收（待收）本金
	 * 
	 * @return
	 */
	public double getReceivableCorpus(String userId) {
		String hql = "Select sum(corpus) from InvestRepay where time is null and invest.user.id = ? ";
		List<Object> result = ht.find(hql, userId);
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return money;
	}
	
	/**
	 * 应收（待收）本金（微信）
	 * 
	 * @return
	 */
	public String getReceivableCorpusWeiXin(String userId) {
		String hql = "Select sum(corpus) from InvestRepay where time is null and invest.user.id = ? ";
		List<Object> result = ht.find(hql, userId);
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return NumberUtil.getNumber(money);
	}
	
	/**
	 * 所有用户的应收（待收）本金
	 * 
	 * @return
	 */
	public double getReceivableCorpus() {
		String hql = "Select sum(corpus) from InvestRepay where time is null";
		List<Object> result = ht.find(hql);
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return money;
	}

	/**
	 * 应收（待收）本金，还款日在repayDay之前
	 * 
	 * @return
	 */
	public double getReceivableCorpus(String userId, Date repayDay) {
		String hql = "Select sum(corpus) from InvestRepay where time is null and invest.user.id = ? and repayDay <? ";
		List<Object> result = ht.find(hql, new Object[] { userId, repayDay });
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return money;
	}

	/**
	 * 已收利息
	 * 
	 * @return
	 */
	public double getReceivedInterest(String userId) {
		// InvestRepay
		String hql = "Select sum(interest+defaultInterest-fee) from InvestRepay where time is not null and invest.user.id = ? and time <= ?";

		List<Object> result = ht.find(hql, userId, new Date());
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		
		return money;
	}
	
	/**
	 * 已收利息(微信)
	 * 
	 * @return
	 */
	public String getReceivedInterestWeiXin(String userId) {
		// InvestRepay
		String hql = "Select sum(interest+defaultInterest-fee) from InvestRepay where time is not null and invest.user.id = ? and time <= ?";

		List<Object> result = ht.find(hql, userId, new Date());
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		
		return NumberUtil.getNumber(money);
	}
	
	/**
	 * 所有用户已收利息
	 * 
	 * @return
	 */
	public double getReceivedInterest() {
		// InvestRepay
		String hql = "Select sum(interest+defaultInterest-fee) from InvestRepay where time is not null and time <= ?";

		List<Object> result = ht.find(hql, new Date());
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return money;
	}

	/**
	 * 应收（待收）利息
	 * 
	 * @return
	 */
	public double getReceivableInterest(String userId) {
		String hql = "Select sum(interest+defaultInterest-fee) from InvestRepay where time is null and invest.user.id = ?";

		List<Object> result = ht.find(hql, userId);
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return money;
	}
	/**
	 * 应收（待收）利息(微信)
	 * 
	 * @return
	 */
	public String getReceivableInterestWeiXin(String userId) {
		String hql = "Select sum(interest+defaultInterest-fee) from InvestRepay where time is null and invest.user.id = ?";

		List<Object> result = ht.find(hql, userId);
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return NumberUtil.getNumber(money);
	}
	
	/**
	 * 所有用户的应收（待收）利息
	 * 
	 * @return
	 */
	public double getReceivableInterest() {
		String hql = "Select sum(interest+defaultInterest-fee) from InvestRepay where time is null";

		List<Object> result = ht.find(hql);
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return money;
	}

	/**
	 * 应收（待收）利息 ，还款日在repayDay之前
	 * 
	 * @return
	 */
	public double getReceivableInterest(String userId, Date repayDay) {
		String hql = "Select sum(interest+defaultInterest-fee) from InvestRepay where time is null and invest.user.id = ? and repayDay <? ";

		List<Object> result = ht.find(hql, new Object[] { userId, repayDay });
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return money;

	}
	
	/**
	 * 计算平台上所有成功投资的总人数
	 */
	public long getAllInvestUserNumber(){
		String hql = "select count(distinct user) from Invest invest where invest.status <> ?";
		return (Long) ht.find(hql,InvestConstants.InvestStatus.CANCEL).get(0);
	}
	
	/**
	 * 计算平台上所有实现借款的总人数
	 */
	public long getAllLoanUserNumber(){
		String hql = "select count(distinct user) from Loan";
		return (Long) ht.find(hql).get(0);
	}
	

	/**
	 * 计算在平台上所有用户已经投资成功的总的金额
	 * 
	 * @return
	 */
	public double getAllInvestsMoney() {
		String hql = "select sum(invest.investMoney) from Invest invest "
				+ "where invest.status not in (?,?)";
		List<Object> oos = ht.find(hql, new String[] {
				InvestConstants.InvestStatus.WAIT_AFFIRM,
				InvestConstants.InvestStatus.CANCEL });
		Object o = oos.get(0);
		if (o == null) {
			return 0;
		}
		return (Double) o;
	}

	/**
	 * 计算在平台上所有用户已经投资成功的总的金额
	 * 
	 * @return
	 */
	public double getAllInvestsMoney(String businessType) {
		String hql = "select sum(invest.investMoney) from Invest invest join invest.loan loan "
				+ "where invest.status not in (?,?) and loan.status in(?,?,?,?) and invest.loan.businessType=?";
		List<Object> oos = ht.find(hql, new String[] {
				InvestConstants.InvestStatus.WAIT_AFFIRM,
				InvestConstants.InvestStatus.CANCEL, 
				LoanStatus.COMPLETE, LoanStatus.RAISING,
				LoanStatus.RECHECK, LoanStatus.REPAYING,
				businessType });
		Object o = oos.get(0);
		if (o == null) {
			return 0;
		}
		return (Double) o;
	}
	/**
	 * 计算在平台上所有用户已经投资成功的总的金额（新手专用）
	 * 
	 * @return
	 */
	public double getAllInvestsMoneyByAttr(String attrId) {
		/*String hql = "select sum(invest.investMoney) from Invest invest , Loan loan left join loan.loanAttrs attr "
					+" where invest.status not in (?,?) and attr.id =  ?";*/
		String hql = "select sum(invest.investMoney) from Invest invest join invest.loan loan left join loan.loanAttrs attr "
				+" where invest.status not in (?,?) and loan.status in(?,?,?,?) and attr.id =  ?";
		List<Object> oos = ht.find(hql, new String[] {
				InvestConstants.InvestStatus.WAIT_AFFIRM,
				InvestConstants.InvestStatus.CANCEL,
				LoanStatus.COMPLETE, LoanStatus.RAISING,
				LoanStatus.RECHECK, LoanStatus.REPAYING,
				attrId });
					
		
		Object o = oos.get(0);
		if (o == null) {
			return 0;
		}
		return (Double) o;
	}

	/**
	 * 计算在平台上所有用户已经投资成功的总的收益
	 * 
	 * @return
	 */
	public double getAllInvestsInterest() {
		String hql = "Select sum(interest+defaultInterest-fee) from InvestRepay where time is not null";
		List<Object> oos = ht.find(hql);
		Object o = oos.get(0);
		if (o == null) {
			return 0;
		}
		return (Double) o;
	}

	/**
	 * 获取所有成功的投资数量
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public long getAllSuccessInvestsNum() {
		String hql = "select count(invest) from Invest invest "
				+ "where invest.status not in (?,?)";
		List<Object> oos = ht.find(hql, new String[] {
				InvestConstants.InvestStatus.WAIT_AFFIRM,
				InvestConstants.InvestStatus.CANCEL });
		if (oos.get(0) == null) {
			return 0;
		}
		return (Long) oos.get(0);
	}

	/**
	 * 获取所有成功的投资数量
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public long getAllSuccessInvestsNum(String businessType) {
		String hql = "select count(invest) from Invest invest "
				+ "where invest.status not in (?,?) and invest.loan.businessType=?";
		List<Object> oos = ht.find(hql, new String[] {
				InvestConstants.InvestStatus.WAIT_AFFIRM,
				InvestConstants.InvestStatus.CANCEL, businessType });
		if (oos.get(0) == null) {
			return 0;
		}
		return (Long) oos.get(0);
	}

	/**
	 * 投资排行榜
	 * 
	 * @return
	 */
	public List<InvestPulished> getIps() {
		Calendar c1 = Calendar.getInstance();
		c1.set(1000, 1, 1);
		
		Calendar c2 = Calendar.getInstance();
		c2.set(9000, 12, 31);
		
		return getIps(c1.getTime(), c2.getTime());
	}
	
	/**
	 * 投资排行榜
	 * 
	 * @return
	 */
	public List<InvestPulished> getIps(final Date startTime ,final Date endTime) {
		List<InvestPulished> ips = new ArrayList<InvestPulished>();
		final String hql = "SELECT invest.user.id, SUM(ir.corpus), SUM(ir.interest) FROM InvestRepay ir where ir.invest.time >= ? and ir.invest.time <= ? GROUP BY ir.invest.user ORDER BY SUM(ir.corpus) desc";
		@SuppressWarnings("unchecked")
		List<Object[]> objs = ht
				.execute(new HibernateCallback<List<Object[]>>() {
					public List<Object[]> doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query query = session.createQuery(hql);
						
						query.setParameter(0, startTime);
						query.setParameter(1, endTime);
						// 从第0行开始
						query.setFirstResult(0);
						query.setMaxResults(5);
						return query.list();
					}
				});
		if (objs.size() > 0) {
			for (Object obj : objs) {
				Object[] objs2 = (Object[]) obj;
				InvestPulished ip = new InvestPulished((String) objs2[0],
						(Double) objs2[1], (Double) objs2[2]);
				ips.add(ip);
			}
		}
		return ips;
	}

}
