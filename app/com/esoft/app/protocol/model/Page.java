package com.esoft.app.protocol.model;

import java.util.ArrayList;
import java.util.List;

public class Page {
	private int curPage;//当前页数
	private int maxPage;//最大页数
	private int size=20;//每页显示条数
	private int maxSize=0;//最大条数
	
	private int start;//当前页数在整体数据中的位置
	private int limit;//当前页数截止在整体数据中的位置
	
	private List data;//所对应的json数据
	/**
	 * 初始化方法
	 */
	public void init(){
		if(maxSize!=0&&maxSize>0){
			this.maxPage=maxSize%size==0?maxSize/size:maxSize/size+1;
			int cPage=0;
			if(curPage>0){
				cPage=curPage-1;
			}
			this.start=cPage*size;
			this.limit=start+size;
		}
	}
	
	public int getCurPage() {
		return curPage;
	}
	public void setCurPage(int curPage) {
		this.curPage = curPage;
	}
	public int getMaxPage() {
		return maxPage;
	}
	public void setMaxPage(int maxPage) {
		this.maxPage = maxPage;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public List getData() {
		return data;
	}

	public void setData(List data) {
		this.data = data;
	}
}
