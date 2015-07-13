/**
 * Title: DbTables.java
 * Description: 
 * Copyright: Copyright (c) 2013-2015 luoxudong.com
 * Company: 个人
 * Author: 罗旭东 (hi@luoxudong.com)
 * Date: 2015年7月13日 上午11:35:09
 * Version: 1.0
 */
package com.luoxudong.app.database.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** 
 * ClassName: DbTables
 * Description:数据库表名自定义注解
 * Create by: 罗旭东
 * Date: 2015年7月13日 上午11:35:09
 */
@Target(ElementType.TYPE)
@Retention(RUNTIME)
public @interface DbTables {
	/** 表名 */
	public String tableName() default "";

	public Class<?> daoClass() default Void.class;
}