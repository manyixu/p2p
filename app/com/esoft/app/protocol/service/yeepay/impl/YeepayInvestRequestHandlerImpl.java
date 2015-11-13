package com.esoft.app.protocol.service.yeepay.impl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.hibernate.ObjectNotFoundException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.YeepayUtil;
import com.esoft.app.protocol.util.exception.InvestException;
import com.esoft.app.protocol.util.exception.YeepayException;
import com.esoft.archer.common.exception.NoMatchingObjectsException;
import com.esoft.archer.config.service.ConfigService;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.UserBillService;
import com.esoft.core.annotations.Logger;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.coupon.exception.ExceedDeadlineException;
import com.esoft.jdp2p.coupon.exception.UnreachedMoneyLimitException;
import com.esoft.jdp2p.invest.InvestConstants;
import com.esoft.jdp2p.invest.exception.ExceedMaxAcceptableRate;
import com.esoft.jdp2p.invest.exception.ExceedMoneyNeedRaised;
import com.esoft.jdp2p.invest.exception.IllegalLoanStatusException;
import com.esoft.jdp2p.invest.model.Invest;
import com.esoft.jdp2p.invest.service.InvestService;
import com.esoft.jdp2p.loan.LoanConstants;
import com.esoft.jdp2p.loan.LoanConstants.LoanStatus;
import com.esoft.jdp2p.loan.exception.InsufficientBalance;
import com.esoft.jdp2p.loan.model.Loan;
import com.esoft.jdp2p.loan.service.LoanCalculator;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
import com.esoft.yeepay.trusteeship.exception.YeePayOperationException;

/**
 * 
 * @author hch
 * 投资
 */
@Service("yeepayInvestRequestHandler")
public class YeepayInvestRequestHandlerImpl implements RequestHandler,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4319569491756160191L;
	@Logger
	private static Log log;
	@Resource
	private HibernateTemplate ht;
	@Resource
	private LoanCalculator loanCalculator;
	@Resource
	private UserBillService userBillService;
	@Resource
	private InvestService investService;
	@Resource
	private TrusteeshipOperationBO trusteeshipOperationBO;
	@Resource
	private ConfigService configService;
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			String loanId=json.getString("loanId");//项目编号
			double investMoney=json.getDouble("investMoney");//投资金额
			String freezeMoney = "1";
			try {
				freezeMoney = configService.getConfigValue("freeze_money");
			} catch (ObjectNotFoundException e) {}
			if(userId!=null&&userId.length()>0){
				Loan loan=ht.get(Loan.class, loanId);
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
										if(loan.getUser().getId().equals(userId)){
											log.error("你不能投自己的项目！");
											throw new InvestException("你不能投自己的项目！");
										}else{
											Invest invest=new Invest();
											invest.setMoney(investMoney);
											invest.setUser(new User(userId));
											invest.setLoan(loan);
											invest.setIsAutoInvest(false);
											if("0".equals(freezeMoney)){
												try {
													ht.evict(loan);
													loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);
													invest.setInvestMoney(invest.getMoney());
													if (!loan.getStatus().equals(LoanStatus.RAISING)) {
														log.error("loan.getStatus()"+loan.getStatus());
														throw new IllegalLoanStatusException(loan.getStatus());
													}
													// 判断项目尚未认购的金额，如果用户想认购的金额大于此金额，则。。。
													double remainMoney = loanCalculator.calculateMoneyNeedRaised(loan
															.getId());
													if (invest.getMoney() > remainMoney) {
														log.error("invest.getMoney() > remainMoney"+invest.getMoney()+">>>>"+remainMoney);
														throw new ExceedMoneyNeedRaised();
													}
													// 是否大于用户账户可用余额
													if (invest.getMoney() > userBillService.getBalance(invest.getUser().getId())) {
														log.error("是否大于用户账户可用余额");
														throw new InsufficientBalance();
													}
													invest.setStatus(InvestConstants.InvestStatus.CANCEL);
													invest.setRate(loan.getRate());
													invest.setTime(new Date());

													// 投资成功以后，判断项目是否有尚未认购的金额，如果没有，则更改项目状态。
													invest.setId(investService.generateId(invest.getLoan().getId()));
													if (invest.getTransferApply() == null
															|| StringUtils.isEmpty(invest.getTransferApply().getId())) {
														invest.setTransferApply(null);
													}
												} catch (InsufficientBalance e) {
													log.error("账户余额不足，请充值！"+e);
													throw new YeePayOperationException("账户余额不足，请充值！", e);
												} catch (ExceedMoneyNeedRaised e) {
													log.error("投资金额不能大于尚未募集的金额！"+e);
													throw new YeePayOperationException("投资金额不能大于尚未募集的金额！", e);
												} catch (IllegalLoanStatusException e) {
													log.error("当前借款不可投资"+e);
													throw new YeePayOperationException("当前借款不可投资", e);
												} catch (NoMatchingObjectsException e) {
													log.error("找不到匹配配置"+e);
													throw new YeePayOperationException("找不到匹配配置", e);
												}
											}else{
												try {
													investService.create(invest);
												} catch (InsufficientBalance e) {
													log.error("账户余额不足，请充值！"+e);
													throw new YeePayOperationException("账户余额不足，请充值！", e);
												} catch (ExceedMoneyNeedRaised e) {
													log.error("投资金额不能大于尚未募集的金额！"+e);
													throw new YeePayOperationException("投资金额不能大于尚未募集的金额！", e);
												} catch (ExceedMaxAcceptableRate e) {
													log.error("竞标利率不能大于借款者可接受的最高利率！"+e);
													throw new YeePayOperationException("竞标利率不能大于借款者可接受的最高利率！", e);
												} catch (ExceedDeadlineException e) {
													log.error("优惠券超过有效期！"+e);
													throw new YeePayOperationException("优惠券超过有效期！", e);
												} catch (UnreachedMoneyLimitException e) {
													log.error("金额未达到优惠券使用条件！"+e);
													throw new YeePayOperationException("金额未达到优惠券使用条件！", e);
												} catch (IllegalLoanStatusException e) {
													log.error("当前借款不可投资"+e);
													throw new YeePayOperationException("当前借款不可投资", e);
												}
												invest.setStatus(InvestConstants.InvestStatus.WAIT_AFFIRM);
											}
											invest.setLoan(ht.get(Loan.class, invest.getLoan().getId()));
											invest.setUser(ht.get(User.class, invest.getUser().getId()));
											ht.saveOrUpdate(invest);

											StringBuffer content = new StringBuffer();
											content.append("<?xml version='1.0' encoding='utf-8'?>");
											// 商户编号:商户在易宝唯一标识
											content.append("<request platformNo='"
													+ YeePayConstants.Config.MER_CODE + "'>");
											// 商户平台会员标识:会员在商户平台唯一标识
											content.append("<platformUserNo>" + invest.getUser().getId()
													+ "</platformUserNo>");
											// 请求流水号:接入方必须保证参数内的 requestNo 全局唯一，并且需要保存，留待后续业务使用
											content.append("<requestNo>"+YeePayConstants.RequestNoPre.INVEST + invest.getId() + "</requestNo>");
											// 标的号:标识要自动还款的标的号
											content.append("<orderNo>" + invest.getLoan().getId() + "</orderNo>");
											// 标的金额
											content.append("<transferAmount>" + invest.getLoan().getLoanMoney()
													+ "</transferAmount>");
											// 目标会员编号
											content.append("<targetPlatformUserNo>"
													+ invest.getLoan().getUser().getId()
													+ "</targetPlatformUserNo>");
											// 冻结金额
											content.append("<paymentAmount>" + invest.getMoney()
													+ "</paymentAmount>");
											Date date = DateUtil.addMinute(new Date(), 10);
											SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											content.append("<expired>" + sdf.format(date) + "</expired>");
											// 页面回跳URL
											content.append("<callbackUrl>"
													+ YeePayConstants.ResponseWebUrl.PRE_RESPONSE_URL
													+ YeePayConstants.OperationType.INVEST + "</callbackUrl>");
											// 服务器通知 URL:服务器通知 URL
											content.append("<notifyUrl>"
													+ YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL
													+ YeePayConstants.OperationType.INVEST + "</notifyUrl>");
											// 提现金额:如果传入此，将使用该金额提现且用户将不可更改
											content.append("</request>");
											log.debug(content.toString());
											// 包装参数
											Map<String, String> params = new HashMap<String, String>();
											params.put("req", content.toString());
											String sign = CFCASignUtil.sign(content.toString());
											params.put("sign", sign);

											// 保存本地
											TrusteeshipOperation to = new TrusteeshipOperation();
											to.setId(IdGenerator.randomUUID());
											to.setMarkId(invest.getId());
											to.setOperator(invest.getId());
											to.setRequestUrl(YeePayConstants.RequestUrl.MOBILE_INVEST);
											to.setRequestData(MapUtil.mapToString(params));
											to.setStatus(TrusteeshipConstants.Status.UN_SEND);
											to.setType(YeePayConstants.OperationType.INVEST);
											to.setTrusteeship("yeepay");
											trusteeshipOperationBO.save(to);
											log.debug("app端保存TrusteeshipOperation成功"+YeePayConstants.OperationType.INVEST);
											String form=YeepayUtil.getFormStr(to.getId(), "utf-8", ht);
											out.encryptResult(form);
											out.sign();
											out.setResultCode(ResponseMsgKey.SUCCESS);
											out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
											log.debug("app充值End");
										}
									}else{
										log.error("该用户没有投资人权限");
										throw new YeepayException("该用户没有投资人权限");
									}
								}else{
									log.error("要投资的金额大于用户剩余金额:"+userSyje);
									throw new YeepayException("要投资的金额大于用户剩余金额:"+userSyje);
								}
							}else{
								log.error("投资金额介于"+loan.getMinInvestMoney()+"与"+syje+"之间");
								throw new YeepayException("投资金额介于"+loan.getMinInvestMoney()+"与"+syje+"之间");
							}
						}else{
							log.error("投资金额不是"+loan.getCardinalNumber()+"的倍数");
							throw new YeepayException("投资金额不是"+loan.getCardinalNumber()+"的倍数");
						}
					}else{
						log.error("该项目的状态不是筹款中!");
						throw new YeepayException("该项目的状态不是筹款中!");
					}
				}else{
					log.error("项目不存在!");
					throw new YeepayException("项目不存在!");
				}
			}else{
				log.error("用户未登录!");
				throw new YeepayException("用户未登录!");
			}
		}catch(JSONException e){
			log.error(e);
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch(YeepayException e){
			log.error(e);
			out.setResultCode(ResponseMsgKey.YEEPAY_ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch (Exception e) {
			log.error(e);
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}
		return out;
	}

}
