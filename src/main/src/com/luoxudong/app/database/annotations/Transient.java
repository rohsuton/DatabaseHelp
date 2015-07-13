/**
 * Title: Transient.java
 * Description: 
 * Copyright: Copyright (c) 2013-2015 luoxudong.com
 * Company: 个人
 * Author: 罗旭东 (hi@luoxudong.com)
 * Date: 2015年7月13日 上午11:36:37
 * Version: 1.0
 */
package com.luoxudong.app.database.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** 
 * ClassName: Transient
 * Description:自定义注释,表示该属性并非一个到数据库表的字段的映射,ORM框架将忽略该属性
 * Create by: 罗旭东
 * Date: 2015年7月13日 上午11:36:37
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Transient {
	/** 是否忽略改属性映射 */
	public boolean value() default true;
}