package com.esoft.yeepay.trusteeship;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class YeePayConstants {

	private static Properties props;
	static {
		props = new Properties();
		try {
			props.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("yeepay.properties"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("找不到yeepay.properties文件", e);
		} catch (IOException e) {
			throw new RuntimeException("读取yeepay.properties文件出错", e);
		}
	}

	public static final class Config {
		/** 默认的签名版本号 */
		public static String SIGN_VERSION = props.getProperty("sign.version");
		/** 默认的加密版本号 */
		public static String ENCYPT_VERSION = props
				.getProperty("encypt.version");
		/** 默认服务版本号 */
		public static String SERVER_VERSION = props
				.getProperty("server.version");
		/** 商户ID */
		public static String MER_CODE = props.getProperty("merCode");
		/** 密码 */
		public static String PASS_WORD = props.getProperty("passWord");
		/** 证书名字 */
		public static String CERTIFICATE_NAME = props
				.getProperty("certificateName");
		/** 平台用户标识信息 */
		public static String SYSTEM_IDENTITY_ID = props
				.getProperty("system.identityId");
	}

	/**
	 * 请求地址
	 */
	public static final class RequestUrl {
		/**
		 * 直连接口请求地址
		 */
		public static final String RequestDirectUrl = props
				.getProperty("request.directUrl");
		/**
		 * 开户
		 */
		public static final String CREATE_YEEPAY_ACCT = props
				.getProperty("request.createAcctUrl");
		/**
		 * 企业开户
		 */
		public static final String ENTERPRISE_REGISTER = props
				.getProperty("request.enterpriseRegister");
		/**
		 * 开户(手机)
		 */
		public static final String MOBILE_CREATE_YEEPAY_ACCT = props
				.getProperty("request.mobile.createAcctUrl");
		/**
		 * 绑卡
		 */
		public static final String BINDING_YEEPAY_BANKCARD = props
				.getProperty("request.bindingBankcardUrl");
		
		/**
		 * 绑卡(手机)
		 */
		public static final String MOBILE_BINDING_YEEPAY_BANKCARD = props
				.getProperty("request.mobile.bindingBankcardUrl");
		/**
		 * 取消绑卡
		 */
		public static final String UNBINDING_YEEPAY_BANKCARD = props
				.getProperty("request.unbindingBankcardUrl");

		/**
		 * 充值
		 */
		public static final String RECHARGE = props
				.getProperty("request.rechargeUrl");
		/**
		 * 直接划款
		 */
/*		public static final String RECHARGE2 = props
				.getProperty("request.directUrldhc");*/

		/**
		 * 充值(手机端)
		 */
		public static final String MOBILE_RECHARGE = props
				.getProperty("request.mobile.rechargeUrl");
		/**
		 * 直接划款(手机端)
		 */
		/*public static final String MOBILE_RECHARGE2 = props
				.getProperty("request.mobile.rechargeUrldhc");
*/
		/**
		 * 投标
		 */
		public static final String INVEST = props
				.getProperty("request.investUrl");

		/**
		 * 投标(手机端)
		 */
		public static final String MOBILE_INVEST = props
				.getProperty("request.mobile.investUrl");

		/**
		 * 还款
		 */
		public static final String REPAY = props
				.getProperty("request.repayUrl");
		/**
		 * 还款(手机)
		 */
		public static final String MOBILE_REPAY = props
				.getProperty("request.mobile.repayUrl");

		/**
		 * 提现
		 */
		public static final String WITHDRAW_CASH = props
				.getProperty("request.withdrawCashUrl");
		/**
		 * 提现（手机端）
		 */
		public static final String MOBILE_WITHDRAW_CASH = props
				.getProperty("request.mobile.withdrawCashUrl");

		/**
		 * 债权转让
		 */
		public static final String TRANSFER = props
				.getProperty("request.transferUrl");

		/**
		 * 自动投标授权
		 */
		public static final String AUTHORIZE_AUTO_TRANSFER = props
				.getProperty("request.authorizeAutoTransferUrl");
		
		/**
		 * 修改绑定手机号
		 */
		public static final String UPDATE_BOUND_MOBILE = props
				.getProperty("request.updateMobileUrl");
		/**
		 * 修改绑定手机号(手机端)
		 */
		public static final String MOBILE_UPDATE_BOUND_MOBILE = props
				.getProperty("request.mobile.updateMobileUrl");

	}

	public static final class ResponseWebUrl {
		/**
		 * 同步回调地址前缀
		 */
		public static final String PRE_RESPONSE_URL = props
				.getProperty("resopnse.webUrl");
	}

	public static final class ResponseS2SUrl {
		/**
		 * 异步回调地址前缀
		 */
		public static final String PRE_RESPONSE_URL = props
				.getProperty("resopnse.s2sUrl");
	}
	
	public static final class ResponseWeiXinWebUrl {
		/**
		 * 微信同步回调地址前缀
		 */
		public static final String PRE_RESPONSE_URL = props
				.getProperty("resopnse.weixinWebUrl");
	}

	public static final class ResponseWeiXinS2SUrl {
		/**
		 * 微信异步回调地址前缀
		 */
		public static final String PRE_RESPONSE_URL = props
				.getProperty("resopnse.weixinS2sUrl");
	}

	public static final class OperationType {
		/**
		 * 开户
		 */
		public static final String CREATE_ACCOUNT = "create_account";
		/**
		 * 企业开户
		 */
		public static final String ENTERPRISE_REGISTER = "enterprise_register";
		/**
		 * 充值
		 */
		public static final String RECHARGE = "recharge";
		/**
		 * 直接划款(体验金或节日活动)
		 */
		public static final String RECHARGE2 = "DIRECT_TRANSACTION";
		/**
		 * 直接划款(邀请返利)
		 */
		public static final String RECHARGE3 = "CP_TRANSACTION";
		
		/**
		 * 划扣款
		 */
		public static final String HUAKOU = "WHDEBITNOCARD_RECHARGE";

		/**
		 * 投标
		 */
		public static final String INVEST = "invest";

		/**
		 * 放款
		 */
		public static final String GIVE_MOENY_TO_BORROWER = "give_moeny_to_borrower";

		/**
		 * 还款
		 */
		public static final String REPAY = "repay";
		/**
		 * 提前还款
		 */
		public static final String ADVANCE_REPAY = "advance_repay";

		/**
		 * 逾期 还款
		 */
		public static final String OVERDUE_REPAY = "overdue_repay";

		/**
		 * 准备金还款
		 */
		public static final String RESERVE_REPAY = "reserve_repay";

		/**
		 * 提现
		 */
		public static final String WITHDRAW_CASH = "withdraw_cash";
		/**
		 * 绑定银行卡
		 */
		public static final String BINDING_YEEPAY_BANKCARD = "binding_card";
		/**
		 * 解绑银行卡
		 */
		public static final String UNBINDING_YEEPAY_BANKCARD = "unbinding_card";
		/**
		 * 开启自动投标
		 */
		public static final String AUTHORIZE_AUTO_TRANSFER = "Authorize_auto_transfer";
		/**
		 * 自动投标
		 */
		public static final String AUTO_INVEST = "auto_invest";
		/**
		 * 债权转让
		 */
		public static final String TRANSFER = "transfer";
		/**
		 * 绑定手机号修改
		 */
		public static final String UPDATE_MOBILE = "update_mobile";

	}

	public static final class RequestNoPre {
		/**
		 * 开户
		 */
		public static final String CREATE_ACCOUNT = "01";

		/**
		 * 充值
		 */
		public static final String RECHARGE = "02";

		/**
		 * 投标
		 */
		public static final String INVEST = "03";

		/**
		 * 放款
		 */
		public static final String GIVE_MOENY_TO_BORROWER = "04";

		/**
		 * 还款
		 */
		public static final String REPAY = "05";
		/**
		 * 提前还款
		 */
		public static final String ADVANCE_REPAY = "06";

		/**
		 * 逾期 还款
		 */
		public static final String OVERDUE_REPAY = "07";

		/**
		 * 准备金还款
		 */
		public static final String RESERVE_REPAY = "08";

		/**
		 * 提现
		 */
		public static final String WITHDRAW_CASH = "09";
		/**
		 * 绑定银行卡
		 */
		public static final String BINDING_YEEPAY_BANKCARD = "10";
		/**
		 * 解绑银行卡
		 */
		public static final String UNBINDING_YEEPAY_BANKCARD = "11";
		/**
		 * 开启自动投标
		 */
		public static final String AUTHORIZE_AUTO_TRANSFER = "12";
		/**
		 * 自动投标
		 */
		public static final String AUTO_INVEST = "13";
		/**
		 * 债权转让
		 */
		public static final String TRANSFER = "14";

		/**
		 * 流标
		 */
		public static final String CANCEL_LOAN = "15";
		/**
		 * 企业开户
		 */
		public static final String ENTERPRISE_REGISTER = "16";
		/**
		 * 修改绑定的手机号
		 */
		public static final String UPDATE_MOBILE = "17";
		/**
		 * 直接划款
		 */
		public static final String REPAY2 = "18";
		/**
		 * 划扣款
		 */
		public static final String HUAKOU = "19";
	}
	public static final class WithdrawWay {
		/**
		 * 正常提现：t+1
		 */
		public static final String NORMAL = "NORMAL";
		/**
		 * 加急提现：t+0
		 */
		public static final String URGENT = "URGENT";
	}
	/**
	 *  yee手续费收取方:1：平台支付 2：提现方支付
	 *
	 */
	public static final class WithdrawYeeFee {
		/**
		 * 平台支付
		 */
		public static final String PCPAY = "1";
		/**
		 * 提现方支付
		 */
		public static final String USERPAY = "2";
	}
	/**
	 * 快速提现费率
	 * @author Zhangwq20150821
	 *
	 */
	public static final class UrgentWithdrawRate {
		//快速提现费率
		public static final String URGENT_RATE = props.getProperty("yeepay.fee");
	}
}