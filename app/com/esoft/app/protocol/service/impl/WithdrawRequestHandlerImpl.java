package com.esoft.app.protocol.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.LockMode;
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
import com.esoft.app.protocol.util.exception.WithdrawException;
import com.esoft.archer.user.UserConstants;
import com.esoft.archer.user.UserBillConstants.OperatorInfo;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.util.DateStyle;
import com.esoft.core.util.DateUtil;
import com.esoft.jdp2p.loan.model.WithdrawCash;
import com.esoft.jdp2p.risk.FeeConfigConstants.FeePoint;
import com.esoft.jdp2p.risk.FeeConfigConstants.FeeType;
import com.esoft.jdp2p.risk.service.impl.FeeConfigBO;
/**
 *
 * 提现
 *
 */
@Service("withdrawRequestHandler")
public class WithdrawRequestHandlerImpl implements RequestHandler{

	@Resource
	private UserBillBO userBillBO;
	@Resource
	HibernateTemplate ht;
	@Resource
	private FeeConfigBO feeConfigBO;

	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");//用户编号
			String bankCardId=json.getString("bankCardId");//银行卡id
			Double fee=json.getDouble("fee");//提现费用
			Double money=json.getDouble("money");//提现金额
			if(userId==null||userId.length()==0){
				throw new WithdrawException("用户编号不能为空");
			}
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
			if(userBillBO.getBalance(userId)<(fee+money)){
				throw new WithdrawException("用户余额不足");
			}
			WithdrawCash cash=new WithdrawCash();
			cash.setUser(user);
//			cash.setBankCard(bankCard);
			cash.setCashFine(0D);
			cash.setId(generateId());
			cash.setTime(new Date());
			cash.setMoney(money);
			cash.setFee(fee);
			userBillBO.freezeMoney(cash.getUser().getId(), cash.getMoney(),
					OperatorInfo.APPLY_WITHDRAW,
					"申请提现，冻结提现金额, 提现编号:" + cash.getId());
			userBillBO.freezeMoney(cash.getUser().getId(), cash.getFee(),
					OperatorInfo.APPLY_WITHDRAW,
					"申请提现，冻结提现手续费, 提现编号:" + cash.getId());
			// 等待审核
			cash.setStatus(UserConstants.WithdrawStatus.WAIT_VERIFY);
			ht.save(cash);
			out.setResultCode(ResponseMsgKey.SUCCESS);
			out.setResultMsg("您的提现申请已经提交成功，请等待审核");
		}catch(WithdrawException e){
			out.setResultCode(ResponseMsgKey.WITHDRAW_ERROR);
			out.setResultMsg(e.getMessage());
		}catch(JSONException e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
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
			//FIXME:这里加锁没用处
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
