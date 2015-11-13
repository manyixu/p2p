package com.esoft.app.protocol.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.exception.InvestException;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.UserBillService;
import com.esoft.jdp2p.coupon.exception.ExceedDeadlineException;
import com.esoft.jdp2p.coupon.exception.UnreachedMoneyLimitException;
import com.esoft.jdp2p.invest.exception.ExceedMaxAcceptableRate;
import com.esoft.jdp2p.invest.exception.ExceedMoneyNeedRaised;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.invest.service.InvestService;
import com.esoft.jdp2p.loan.LoanConstants;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.loan.service.LoanCalculator;

/**
 * 
 * 项目投标
 *
 */
@Service("loanInvestRequestHandler")
public class LoanInvestRequestHandlerImpl implements RequestHandler{
	@Resource
	private HibernateTemplate ht;
	@Resource
	private LoanCalculator loanCalculator;
	@Resource
	private UserBillService userBillService;
	@Resource
	private InvestService investService;
	
	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			String loanId=json.getString("loanId");//项目编号
			double investMoney=json.getDouble("investMoney");//投资金额
			if(userId!=null&&userId.length()>0){
				Loan loan=ht.load(Loan.class, loanId);
				if(loan!=null){
					if(LoanConstants.LoanStatus.RAISING.equals(loan.getStatus())){
						if(investMoney%loan.getCardinalNumber()==0){
							double syje=loanCalculator.calculateMoneyNeedRaised(loan.getId());//剩余金额
							if(investMoney>=loan.getMinInvestMoney()&&investMoney<=syje){
								double userSyje=userBillService.getBalance(userId);//用户剩余金额
								if(investMoney<=userSyje){
									StringBuilder hql=new StringBuilder("select user from User user join user.roles role where");
									hql.append(" user.id=? and role.id=?");
									List<User> userList=ht.find(hql.toString(),new Object[]{userId,"INVESTOR"});
									if(userList!=null&&userList.size()>0){
										if (loan.getUser().getId().equals(userId)) {
											throw new InvestException("你不能投自己的项目！");
										} else {
											Invest invest=new Invest();
											invest.setMoney(investMoney);
											invest.setUser(new User(userId));
											invest.setLoan(loan);
											invest.setIsAutoInvest(false);
											investService.create(invest);
											out.setResultCode(ResponseMsgKey.SUCCESS);
											out.setResultMsg("投资成功");
										}
									}else{
										throw new InvestException("该用户没有投资人权限");
									}
								}else{
									throw new InvestException("要投资的金额大于用户剩余金额:"+userSyje);
								}
							}else{
								throw new InvestException("投资金额介于"+loan.getMinInvestMoney()+"与"+syje+"之间");
							}
						}else{
							throw new InvestException("投资金额不是"+loan.getCardinalNumber()+"的倍数");
						}
					}else{
						throw new InvestException("该项目的状态不是筹款中!");
					}
				}else{
					throw new InvestException("项目不存在!");
				}
			}else{
				throw new InvestException("用户未登录!");
			}
		}catch(InvestException e){
			out.setResultCode(ResponseMsgKey.INVEST_ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch (InsufficientBalance e) {
			out.setResultCode(ResponseMsgKey.INVEST_ERROR);
			out.setResultMsg("账户余额不足，请充值！");
			e.printStackTrace();
		} catch (ExceedMoneyNeedRaised e) {
			out.setResultCode(ResponseMsgKey.INVEST_ERROR);
			out.setResultMsg("投资金额不能大于尚未募集的金额！");
			e.printStackTrace();
		} catch (ExceedMaxAcceptableRate e) {
			out.setResultCode(ResponseMsgKey.INVEST_ERROR);
			out.setResultMsg("竞标利率不能大于借款者可接受的最高利率！");
			e.printStackTrace();
		} catch (ExceedDeadlineException e) {
			out.setResultCode(ResponseMsgKey.INVEST_ERROR);
			out.setResultMsg("优惠券已过期");
			e.printStackTrace();
		} catch (UnreachedMoneyLimitException e) {
			out.setResultCode(ResponseMsgKey.INVEST_ERROR);
			out.setResultMsg("投资金额未到达优惠券使用条件");
			e.printStackTrace();
		}catch(JSONException e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		} catch(Exception e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.ERROR)));
			e.printStackTrace();
		}
		return out;
	}

}
