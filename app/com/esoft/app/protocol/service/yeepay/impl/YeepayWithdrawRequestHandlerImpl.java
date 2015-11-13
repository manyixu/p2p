package com.esoft.app.protocol.service.yeepay.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
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
import com.esoft.app.protocol.util.exception.BankCardException;
import com.esoft.app.protocol.util.exception.WithdrawException;
import com.esoft.archer.config.service.ConfigService;
import com.esoft.archer.user.UserConstants;
import com.esoft.archer.user.UserBillConstants.OperatorInfo;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.annotations.Logger;
import com.esoft.core.util.DateStyle;
import com.esoft.core.util.DateUtil;
import com.esoft.core.util.IdGenerator;
import com.esoft.core.util.MapUtil;
import com.esoft.jdp2p.bankcard.controller.BankCardList;
import com.esoft.jdp2p.bankcard.model.BankCard;
import com.esoft.jdp2p.bankcard.service.BankCardService;
import com.esoft.jdp2p.loan.model.WithdrawCash;
import com.esoft.jdp2p.risk.FeeConfigConstants.FeePoint;
import com.esoft.jdp2p.risk.FeeConfigConstants.FeeType;
import com.esoft.jdp2p.risk.service.impl.FeeConfigBO;
import com.esoft.jdp2p.trusteeship.TrusteeshipConstants;
import com.esoft.jdp2p.trusteeship.model.TrusteeshipOperation;
import com.esoft.jdp2p.trusteeship.service.impl.TrusteeshipOperationBO;
import com.esoft.jdp2p.user.service.WithdrawCashService;
import com.esoft.yeepay.sign.CFCASignUtil;
import com.esoft.yeepay.trusteeship.YeePayConstants;
/**
 *
 *@author hch
 *提现
 *
 */
@Service("yeepayWithdrawRequestHandler")
public class YeepayWithdrawRequestHandlerImpl implements RequestHandler,Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6710613560227897924L;
	@Resource
	private UserBillBO userBillBO;
	@Resource
	private HibernateTemplate ht;
	@Resource
	private FeeConfigBO feeConfigBO;
	
	@Resource
	private BankCardList bankCardList;
	@Resource
	private WithdrawCashService wcs;
	@Resource
	private ConfigService configService;
	@Logger
	private static Log log;
	@Resource
	private TrusteeshipOperationBO trusteeshipOperationBO;
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
//			String bankCardId=json.getString("bankCardId");//银行卡id
			Double fee=json.getDouble("fee");//提现费用
			Double money=json.getDouble("money");//提现金额
			if(userId==null||userId.length()==0){
				throw new WithdrawException("用户编号不能为空");
			}
			
			/*List<BankCard> blist = bankCardList.getBankCardListbyUser(userId);
			if(blist!=null&&blist.size()>0){
				if("BOCO".equals(blist.get(0).getBankNo())){
					out.sign();
					out.setResultCode(ResponseMsgKey.ERROR);
					out.setResultMsg("因交通银行系统升级维护，暂时无法提供提现功能！将于9月27日24:00以后恢复。如有疑问，请联系客服。");
					return out;
				}
			}*/
//			if(bankCardId==null||bankCardId.length()==0){
//				throw new  WithdrawException("请先选择银行卡");
//			}
			double newFee =feeConfigBO.getFee(FeePoint.WITHDRAW, FeeType.FACTORAGE, null,null,money);
			if(fee!=newFee){
				throw new  WithdrawException("提现费用不能随意输入");
			}
			User user=ht.get(User.class, userId);
			if(user==null){
				throw new WithdrawException("该用户编号不存在");
			}
//			BankCard bankCard=ht.get(BankCard.class,bankCardId);
//			if(bankCard==null){
//				throw new WithdrawException("该银行卡序号不存在");
//			}
			if(money==0){
				throw new BankCardException("金额必须大于或等于1");
			}
			if(userBillBO.getBalance(userId)<(fee+money)){
				throw new WithdrawException("用户余额不足");
			}
			String freezeMoney = "1";
			try {
				freezeMoney = configService.getConfigValue("freeze_money");
			} catch (ObjectNotFoundException e) {}
			WithdrawCash cash=new WithdrawCash();
			cash.setUser(user);
//			cash.setBankCard(bankCard);
			cash.setCashFine(0D);
			cash.setId(generateId());
			cash.setTime(new Date());
			cash.setMoney(money);
			cash.setFee(fee);
			if(!"0".equals(freezeMoney)){
				userBillBO.freezeMoney(cash.getUser().getId(), cash.getMoney(),
						OperatorInfo.APPLY_WITHDRAW,
						"申请提现，冻结提现金额, 提现编号:" + cash.getId());
				userBillBO.freezeMoney(cash.getUser().getId(), cash.getFee(),
						OperatorInfo.APPLY_WITHDRAW,
						"申请提现，冻结提现手续费, 提现编号:" + cash.getId());
			}
			// 等待审核
			cash.setStatus(UserConstants.WithdrawStatus.WAIT_VERIFY);
			ht.save(cash);
			StringBuffer content = new StringBuffer();
			content.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
			// 商户编号:商户在易宝唯一标识
			content.append("<request platformNo='"
					+ YeePayConstants.Config.MER_CODE + "'>");
			// 商户平台会员标识:会员在商户平台唯一标识
			content.append("<platformUserNo>" + cash.getUser().getId()
					+ "</platformUserNo>");
			// 请求流水号:接入方必须保证参数内的 requestNo 全局唯一，并且需要保存，留待后续业务使用
			content.append("<requestNo>"+YeePayConstants.RequestNoPre.WITHDRAW_CASH + cash.getId() + "</requestNo>");
			// 费率模式:见费率模式
			// yee手续费收取方:1：平台支付 2：提现方支付
			String feeType = "2";
			try {
				feeType = configService.getConfigValue("yeepay.withdraw_fee_type");
			} catch (ObjectNotFoundException e) {
				if (log.isDebugEnabled()) {
					log.debug(e);
				}
			}
			
			// 平台手续费
			if (feeType.equals("1")) {
				content.append("<feeMode>" + "PLATFORM" + "</feeMode>");
			} else {
				// 如果是用户自己承担手续费，假如提现100，手续费3块，则账户扣除103
				content.append("<feeMode>" + "USER" + "</feeMode>");
			}

			// 回调通知 URL:回调通知 URL
			content.append("<callbackUrl>"
					+ YeePayConstants.ResponseWebUrl.PRE_RESPONSE_URL
					+ YeePayConstants.OperationType.WITHDRAW_CASH
					+ "</callbackUrl>");
			// 服务器通知 URL:服务器通知 URL
			content.append("<notifyUrl>"
					+ YeePayConstants.ResponseS2SUrl.PRE_RESPONSE_URL
					+ YeePayConstants.OperationType.WITHDRAW_CASH + "</notifyUrl>");
			// 提现金额:如果传入此，将使用该金额提现且用户将不可更改
			content.append("<amount>" + cash.getMoney() + "</amount>");
			content.append("</request>");

			// 生成cfca签名
			String contentString = content.toString();
			String sign = CFCASignUtil.sign(contentString);

			// 包装参数
			Map<String, String> params = new HashMap<String, String>();
			params.put("req", content.toString());
			params.put("sign", sign);

			log.debug("req:" + contentString + "sign" + sign);
			// 保存本地
			TrusteeshipOperation to = new TrusteeshipOperation();
			to.setId(IdGenerator.randomUUID());
			// 用来查询一条提现记录
			to.setMarkId(cash.getId());
			to.setOperator(cash.getId());
			to.setRequestUrl(YeePayConstants.RequestUrl.MOBILE_WITHDRAW_CASH);
			to.setRequestData(MapUtil.mapToString(params));
			to.setStatus(TrusteeshipConstants.Status.UN_SEND);
			to.setType(YeePayConstants.OperationType.WITHDRAW_CASH);
			to.setTrusteeship("yeepay");
			trusteeshipOperationBO.save(to);
			log.debug("app端保存TrusteeshipOperation成功"+YeePayConstants.OperationType.WITHDRAW_CASH);
			String form=YeepayUtil.getFormStr(to.getId(), "utf-8", ht);
			System.out.println("form:"+form);
			out.encryptResult(form);
			out.sign();
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
			log.debug("app端提现End");
		}catch(WithdrawException e){
			log.debug(e);
			out.setResultCode(ResponseMsgKey.WITHDRAW_ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch(BankCardException e){
			log.debug(e);
			out.setResultCode(ResponseMsgKey.BANK_CARD_ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch(JSONException e){
			log.debug(e);
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			log.debug(e);
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.ERROR)));
			e.printStackTrace();
		}
		return out;
	}
	/**
	 * 生成id
	 * 
	 * @return
	 */
	private String generateId() {
		String gid = DateUtil.DateToString(new Date(), DateStyle.YYYYMMDD);
		String hql = "select withdraw from WithdrawCash withdraw where withdraw.id = (select max(withdrawM.id) from WithdrawCash withdrawM where withdrawM.id like ?)";
		List<WithdrawCash> contractList = ht.find(hql, gid + "%");
		Integer itemp = 0;
		if (contractList.size() == 1) {
			WithdrawCash withdrawCash = contractList.get(0);
			ht.lock(withdrawCash, LockMode.UPGRADE);
			String temp = withdrawCash.getId();
			temp = temp.substring(temp.length() - 6);
			itemp = Integer.valueOf(temp);
		}
		itemp++;
		gid += String.format("%08d", itemp);
		return gid;
	}
}
