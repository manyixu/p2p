package com.esoft.jdp2p.message.service.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.esoft.jdp2p.message.exception.SmsSendErrorException;
import com.esoft.jdp2p.message.service.SmsService;
/**
 * 
 * 全利汇丰短信接口
 */
public class QuanLiSmsServiceImpl extends SmsService {
	
	private String wsAdress ="http://cf.51welink.com/submitdata/service.asmx/g_Submit";//接口地址
	private static String sname = "";//提交账户
	private static String spwd = "";//提交账户的密码
	private static String scorpid = "";//企业代码（可空）
	private static String sprdid = "";//产品编号
	
	private static Properties props = new Properties(); 
	//读取配置文件信息
	static{
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("quanlihuifeng_sms_config.properties"));
			sname = props.getProperty("sname");
			spwd = props.getProperty("spwd");
			scorpid = props.getProperty("scorpid");
			sprdid = props.getProperty("sprdid");
//			System.out.println(sname+spwd+scorpid+sprdid);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void send(String content, String mobileNumber) throws SmsSendErrorException {
		
		if (sname == null || spwd == null) {
			
			throw new SmsSendErrorException("短信发送失败，sname或spwd未定义");
		}
		 String PostData =null;
		try {
			PostData = "sname="+sname+"&spwd="+spwd+"&scorpid="+scorpid+"&sprdid="+sprdid+"&sdst="+mobileNumber+"&smsg="+URLEncoder.encode(content,"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
         String ret = SMS(PostData, wsAdress);
         //这里要反序列化ret
         Map<String, Object> map = new HashMap<String, Object>();  
         //将xml格式的字符串转换成Document对象  
         Document doc = null;;
		try {
			doc = DocumentHelper.parseText(ret);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
         //获取根节点  
         Element root = doc.getRootElement();  
         //获取根节点下的所有元素  
         List children = root.elements();  
         //循环所有子元素  
         if(children != null && children.size() > 0) {  
             for(int i = 0; i < children.size(); i++) {  
                 Element child = (Element)children.get(i);  
                 map.put(child.getName(), child.getTextTrim());  
             }  
         } 
         String state = (String) map.get("State");
         if(!state.equals("0")){
 			throw new SmsSendErrorException("短信发送失败，错误代码：" + state);
 		}else {
 			System.out.println("短信发送成功！");
 		}
		
	}
	 public String SMS(String postData, String postUrl) {
        try {
            //发送POST请求
            URL url = new URL(postUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setUseCaches(false);
            conn.setDoOutput(true); 

            conn.setRequestProperty("Content-Length", "" + postData.length());
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            out.write(postData);
            out.flush(); 
            out.close();

            //获取响应状态
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("connect failed!");
                return "";
            }
            //获取响应内容体
            String line, result = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            while ((line = in.readLine()) != null) {
                result += line + "\n";
            }
            in.close();
//            System.out.println(result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
	
	public static void main(String[] args) {
		new QuanLiSmsServiceImpl().send("全利汇丰【全利汇丰】", "18611660365"); 
	}
}