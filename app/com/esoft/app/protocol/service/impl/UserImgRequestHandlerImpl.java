package com.esoft.app.protocol.service.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

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
import com.esoft.app.protocol.util.Base64;
import com.esoft.archer.user.model.User;
import com.esoft.core.jsf.util.FacesUtil;
/**
 * 
 * 获取头像
 *
 */
@Service("userImgRequestHandler")
public class UserImgRequestHandlerImpl implements RequestHandler{

	@Resource
	HibernateTemplate ht;
	@Override
	public Out handle(In in,Out out) {
		// TODO Auto-generated method stub
		try {
			JSONObject json=new JSONObject(in.getValue());
			String userId=json.getString("userId");
			if(userId!=null&&userId.length()>0){
				User user=ht.get(User.class, userId);
				if(user!=null){
					//String absPath = FacesUtil.getRealPath(user.getPhoto()) ;
					//String s = getImageStr(absPath);
					StringBuilder str=new StringBuilder("{");
					//str.append("\"file\":\"").append(s).append("\",");
					str.append("\"photo\":\"").append(user.getPhoto()).append("\"");
					str.append("}");
					out.encryptResult(str.toString());
					out.sign();
					out.setResultCode(ResponseMsgKey.SUCCESS);
					out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.SUCCESS)));
				}else{
					out.setResultCode("用户名不存在");
					out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get("用户名不存在")));
				}
			}else{
				out.setResultCode(ResponseMsgKey.PARAMETER_INVALID);
				out.setResultMsg(String.valueOf(AppConstants.rMsgProps.get(ResponseMsgKey.PARAMETER_INVALID)));
			}
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
	 * 
	 * @param userId
	 * @return 获取个人已经投资成功的总的收益
	 */
	public double getInvestsInterest(String userId){
		StringBuilder hql =new StringBuilder("select sum(interest+defaultInterest-fee)");
		hql.append(" from InvestRepay where time is not null and invest.id in(select id");
		hql.append(" from Invest where user.id=?)");
		double money=0;
		List<Object> result=ht.find(hql.toString(),userId);
		if(result!=null&&result.get(0)!=null){
			money=(Double)result.get(0);
		}
		return money;
	}
	
	/**
	 * 依据原始文件名生成新文件名
	 * @return
	 */
	private static String getName(String fileExt) {
		Random random = new Random();
		return "" + random.nextInt(10000)
				+ System.currentTimeMillis() + "."+fileExt;
	}
	
	//图片转化成base64字符串
    public String getImageStr(String imgFile)
    {//将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        //String imgFile = "d://test.jpg";//待处理的图片
        InputStream in = null;
        byte[] data = null;
        //读取图片字节数组
        String s = null;
        try{
            in = new FileInputStream(imgFile);        
            data = new byte[in.available()];
            in.read(data);
            in.close();
            s = Base64.encryptBASE64(data);
        } 
        catch (IOException e){
            e.printStackTrace();
        }catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return s;//返回Base64编码过的字节数组字符串
    }

}
