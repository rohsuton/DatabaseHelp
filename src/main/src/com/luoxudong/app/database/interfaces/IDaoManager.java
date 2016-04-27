/**
 * Title: IDaoManager.java
 * Description: 
 * Copyright: Copyright (c) 2013-2015 luoxudong.com
 * Company: 个人
 * Author: 罗旭东 (hi@luoxudong.com)
 * Date: 2015年7月13日 上午11:44:57
 * Version: 1.0
 */
package com.luoxudong.app.database.interfaces;

import android.database.sqlite.SQLiteDatabase;

import com.luoxudong.app.database.manager.DaoManager;
import com.luoxudong.app.database.model.BaseModel;

/** 
 * ClassName: IDaoManager
 * Description:数据库操作接口类
 * Create by: 罗旭东
 * Date: 2015年7月13日 上午11:44:57
 */
public interface IDaoManager {
	/**
	 * 打开数据库
	 * @return 数据库管理对象
	 */
	public DaoManager openDatabase();
	
	/**
	 * 关闭数据库
	 */
	public void closeDatabase();
	
	/**
	 * 获取数据库对象
	 * @return 数据库对象
	 */
	public SQLiteDatabase getDatabase();
	
	/**
	 * 重新打开数据库
	 * @return 数据库管理对象
	 */
	public DaoManager reOpenDatabase();
	
	/**
	 * 删除表
	 * @param clazz
	 */
	public <M extends BaseModel> void deleteTable(Class<M> entityClass);
	
	/**
	 * 获取数据库对象
	 * @param clazz
	 * @param entityClass
	 * @return
	 */
	public <T extends IBaseDao<M, Long>, M extends BaseModel> T getDataHelper(Class<T> clazz, Class<M> entityClass);
}
