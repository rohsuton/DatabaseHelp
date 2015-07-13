/**
 * Title: Page.java
 * Description: 
 * Copyright: Copyright (c) 2013-2015 luoxudong.com
 * Company: 个人
 * Author: 罗旭东 (hi@luoxudong.com)
 * Date: 2015年7月13日 上午11:42:48
 * Version: 1.0
 */
package com.luoxudong.app.database.model;

import java.io.Serializable;
import java.util.List;

/** 
 * ClassName: Page
 * Description:分页对象
 * Create by: 罗旭东
 * Date: 2015年7月13日 上午11:42:48
 */
public class Page<T> implements Serializable{
	private static final long serialVersionUID = 1L;

	/**
	 * 当前第几页,开始页为1
	 */
	private int pageNum = 1;
	
	/**
	 * 每一页的个数
	 */
	private int pageSize = 20;
	
	/**
	 * 总记录条数
	 */
	private int totalSize = 0;
	
	/**
	 * 当前查询结果记录数
	 */
	private int resultNum = 0;
	
	/**
	 * 查询条件
	 */
	private T conditions = null;
	
	/**
	 * 排序规则
	 */
	private String order = null;
	
	/**
	 * 结果
	 */
	private List<T> result = null;

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}

	public T getConditions() {
		return conditions;
	}

	public void setConditions(T conditions) {
		this.conditions = conditions;
	}

	public List<T> getResult() {
		return result;
	}

	public void setResult(List<T> result) {
		this.result = result;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public int getResultNum() {
		return resultNum;
	}

	public void setResultNum(int resultNum) {
		this.resultNum = resultNum;
	}
}
