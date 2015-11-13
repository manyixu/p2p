package com.esoft.app.protocol.service.impl;

import javax.annotation.Resource;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.NumberUtil;
import com.esoft.app.protocol.util.exception.CalculateFeeException;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.jdp2p.risk.FeeConfigConstants.FeePoint;
import com.esoft.jdp2p.risk.FeeConfigConstants.FeeType;
import com.esoft.jdp2p.risk.service.impl.FeeConfigBO;
/**
 * 提现和充值---计算手续费和罚金
 */
@Service("calculateFeeRequestHandler")
public class CalculateFeeRequestHandlerImpl implements RequestHandler{
	@Resource
	private FeeConfigBO feeConfigBO;

	@Resource
	private UserBillBO userBillBO;

	@Override
	public Out handle(In in, Out out) {
		// TODO Auto-generated method stub
		try{
			JSONObject json=new JSONObject(in.getValue());
			double money=json.getDouble("money");//金额
			String userId=json.getString("userId");
			String op=json.getString("op");//操作(tixian:提现;chongzhi:充值;)
			if("tixian".equals(op)){
				double fee =feeConfigBO.getFee(FeePoint.WITHDRAW, FeeType.FACTORAGE, null,null,money);
				if(userBillBO.getBalance(userId)<(fee+money)){
					throw new CalculateFeeException("用户余额不足");
				}else{
					out=setOut(out, money, fee);
				}
			}else if("chongzhi".equals(op)){
				double fee=feeConfigBO.getFee(FeePoint.RECHARGE, FeeType.FACTORAGE, null,null, money);
				out=setOut(out, money, fee);
			}else{
				throw new CalculateFeeException("op参数值异常");
			}
		}catch(CalculateFeeException e){
			out.setResultCode(ResponseMsgKey.CALCULATE_FEE_ERROR);
			out.setResultMsg(e.getMessage());
		}catch(JSONException e){
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(e.getMessage());
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			out.setResultCode(ResponseMsgKey.ERROR);
			out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.ERROR)));
			e.printStackTrace();
		}
		return out;
	}
	private Out setOut(Out out,Double money,Double fee){
		StringBuilder str=new StringBuilder("{");
		str.append("\"fee\":\"").append(NumberUtil.getNumber(fee)).append("\",");
		str.append("\"money\":\"").append(NumberUtil.getNumber(money)).append("\"");
		str.append("}");
		out.encryptResult(str.toString());
		out.sign();
		out.setResultCode(ResponseMsgKey.SUCCESS);
		out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
		return out;
	}
}
