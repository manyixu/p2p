package com.esoft.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class AppConstants {
	private static Properties props;
	/**返回信息 response messages*/
	public static Properties rMsgProps;
	static {
		props = new Properties();
		try {
			props.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("app.properties"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("app.properties文件", e);
		} catch (IOException e) {
			throw new RuntimeException("app.properties文件出错", e);
		}
		rMsgProps = new Properties();
		try {
			rMsgProps.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("responseMessages.properties"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("responseMessages.properties文件", e);
		} catch (IOException e) {
			throw new RuntimeException("responseMessages.properties文件出错", e);
		}
	}

	public static final class Config {
		public static final String THREE_DES_BASE64_KEY = props
				.getProperty("three_des_base64_key");
		public static final String THREE_DES_IV = props
				.getProperty("three_des_iv");
		public static final String THREE_DES_ALGORITHM = props
				.getProperty("three_des_algorithm");
	}
}
