package com.esoft.app.protocol.util;

import com.esoft.app.protocol.model.Page;

/**
 * 
 * @author Hch
 * 分页工具类
 *
 */
public class PageUtil {
	public static Page getPage(Integer curPage,Integer size,Integer count){
		Page page=new Page();
		if(size!=null&&size>0){
			page.setSize(size);
		}
		if(curPage!=null&&curPage>0){
			page.setCurPage(curPage);
		}
		page.setMaxSize(count);
		page.init();
		return page;
	}
}
