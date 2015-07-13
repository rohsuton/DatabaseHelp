/**
 * Title: DbFields.java
 * Description: 
 * Copyright: Copyright (c) 2013-2015 luoxudong.com
 * Company: 个人
 * Author: 罗旭东 (hi@luoxudong.com)
 * Date: 2015年7月13日 上午11:34:30
 * Version: 1.0
 */
package com.luoxudong.app.database.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** 
 * ClassName: DbFields
 * Description:自定义数据库表属性注解 
 * Create by: 罗旭东
 * Date: 2015年7月13日 上午11:34:30
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface DbFields {
	/** 列名 */
	public String columnName() default "";
	
	/** 是否唯一 */
	public boolean unique() default false;
	
	/** 是否允许为空 */
	public boolean nullable() default true;
}