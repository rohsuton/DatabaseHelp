/**
 * Title: DatabaseException.java
 * Description: 
 * Copyright: Copyright (c) 2013-2015 luoxudong.com
 * Company: 个人
 * Author: 罗旭东 (hi@luoxudong.com)
 * Date: 2015年7月13日 上午11:39:23
 * Version: 1.0
 */
package com.luoxudong.app.database.exception;

import com.luoxudong.app.database.utils.DbLogUtil;

/** 
 * ClassName: DatabaseException
 * Description:数据库异常处理类
 * Create by: 罗旭东
 * Date: 2015年7月13日 上午11:39:23
 */
public class DatabaseException extends RuntimeException {
	private static final String TAG = DatabaseException.class.getName();

	private static final long serialVersionUID = 1L;

	private int exceptionCode = -1;

	public DatabaseException(String message) {
		super(message);
		DbLogUtil.e(TAG, message);
	}

	public DatabaseException(String message, Throwable throwable) {
		super(message, throwable);
		DbLogUtil.e(TAG, message);
	}

	public DatabaseException(int exceptionCode, Throwable throwable) {
		super(exceptionCode + "", throwable);
		DbLogUtil.e(TAG, exceptionCode + "");

	}

	public DatabaseException(int exceptionCode, String message) {
		super(exceptionCode + "" + message);
		DbLogUtil.e(TAG, exceptionCode + "");

	}

	public int getExceptionCode() {
		return exceptionCode;
	}

	public void setExceptionCode(int exceptionCode) {
		this.exceptionCode = exceptionCode;
	}
}
