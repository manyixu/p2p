package com.esoft.app;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.axis2.transport.http.util.URIEncoderDecoder;

import com.esoft.app.protocol.key.ResponseMsgKey;

public class Test {
	public static void main(String[] args)throws Exception{
		String str=URLEncoder.encode((String)AppConstants.rMsgProps.get(ResponseMsgKey.METHOD_NOT_FOUND),"UTF-8");
		System.out.println(URLDecoder.decode(URLDecoder.decode(str,"UTF-8"),"UTF-8"));
	}
}
