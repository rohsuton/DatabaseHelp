/**
 * Title: ColumnInfo.java
 * Description: 
 * Copyright: Copyright (c) 2013-2015 luoxudong.com
 * Company: 个人
 * Author: 罗旭东 (hi@luoxudong.com)
 * Date: 2015年7月13日 上午11:44:12
 * Version: 1.0
 */
package com.luoxudong.app.database.vo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** 
 * ClassName: ColumnInfo
 * Description:数据库表列属性
 * Create by: 罗旭东
 * Date: 2015年7月13日 上午11:44:12
 */
public class ColumnInfo {
	/** 列属性名称 */
	private String mColumnName;

	/** 属性 */
	private Field mField;

	/** set方法 */
	private Method mSetMethod;

	/** get方法 */
	private Method mGetMethod;

	public String getColumnName() {
		return mColumnName;
	}

	public void setColumnName(String columnName) {
		mColumnName = columnName;
	}

	public Field getField() {
		return mField;
	}

	public void setField(Field field) {
		mField = field;
	}

	public Method getSetMethod() {
		return mSetMethod;
	}

	public void setSetMethod(Method setMethod) {
		mSetMethod = setMethod;
	}

	public Method getGetMethod() {
		return mGetMethod;
	}

	public void setGetMethod(Method getMethod) {
		mGetMethod = getMethod;
	}
}
