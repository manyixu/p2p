package com.esoft.app.protocol.service.impl;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.primefaces.model.UploadedFile;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.app.AppConstants;
import com.esoft.app.protocol.key.ResponseMsgKey;
import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;
import com.esoft.app.protocol.service.RequestHandler;
import com.esoft.app.protocol.util.Base64;
import com.esoft.app.protocol.util.NumberUtil;
import com.esoft.app.protocol.util.exclusionStrategy.UserExclusionStrategy;
import com.esoft.archer.user.model.User;
import com.esoft.archer.user.service.impl.UserBillBO;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.HashCrypt;
import com.esoft.core.util.ImageUploadUtil;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * 
 * 上传头像
 *
 */
@Service("uploadImgRequestHandler")
public class UploadImgRequestHandlerImpl implements RequestHandler{

	@Resource
	HibernateTemplate ht;
	private final static String UPLOAD_PATH = "/upload";
	private static SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd");
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Out handle(In in,Out out) {
		try {
			JSONObject json=new JSONObject(in.getValue());
			String f=json.getString("file");
			String picType=json.getString("picType");
			String userId=json.getString("userId");
			if(f!=null&&picType!=null&&userId!=null&&f.length()>0&&picType.length()>0&&userId.length()>0){
				String path = UPLOAD_PATH + "/" + formater.format(new Date());
				String absPath = FacesUtil.getRealPath(path) ;
				User user=ht.get(User.class, userId);
				if(user!=null){
					String fileName = getName(picType);
					byte []a = Base64.decryptBASE64(f);
					File dir = new File( absPath);
					if (!dir.exists()) {
						try {
							dir.mkdirs();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					File picFile = new File(dir, fileName);
					if (picFile.exists()) {//delete exists file
						picFile.delete();
					}
					FileUtils.writeByteArrayToFile(picFile, a);
					user.setPhoto(path+"/"+fileName);
					ht.update(user);
					StringBuilder str=new StringBuilder("{");
					str.append("\"photo\":\"").append(user.getPhoto()).append("\",");
					str.append("\"file\":\"").append(f).append("\"");
					str.append("}");
					out.sign();
					out.encryptResult(str.toString());
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

}
