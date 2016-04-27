/**
 * Title: DaoManager.java
 * Description: 
 * Copyright: Copyright (c) 2013-2015 luoxudong.com
 * Company: 个人
 * Author: 罗旭东 (hi@luoxudong.com)
 * Date: 2015年7月13日 上午11:47:07
 * Version: 1.0
 */
package com.luoxudong.app.database.manager;

import java.io.File;

import android.database.sqlite.SQLiteDatabase;

import com.luoxudong.app.database.annotations.DbTables;
import com.luoxudong.app.database.exception.DatabaseException;
import com.luoxudong.app.database.interfaces.IBaseDao;
import com.luoxudong.app.database.interfaces.IDaoManager;
import com.luoxudong.app.database.model.BaseModel;
import com.luoxudong.app.database.utils.DbLogUtil;
import com.luoxudong.app.singletonfactory.SingletonFactory;

/** 
 * ClassName: DaoManager
 * Description:基础数据库管理类
 * Create by: 罗旭东
 * Date: 2015年7月13日 上午11:47:07
 */
public class DaoManager implements IDaoManager{
	private static final String TAG = DaoManager.class.getName();
	
	/** 数据库文件地址 */
	private String mDatabasePath = null;
	
	/** 数据库对象 */
	private SQLiteDatabase mDatabase = null;
	
	public DaoManager(String databasePath)
	{
		mDatabasePath = databasePath;
		openDatabase();
	}
	
	@Override
	public DaoManager openDatabase() {
		if (mDatabasePath == null)
		{
			throw new DatabaseException("数据库路径不能为空!");
		}
		
		if (isDatabseOpened())
		{
			DbLogUtil.w(TAG, "数据库已经打开");
			return this;
		}
		
		File databaseFile = new File(mDatabasePath);
		
		//保存数据库的文件夹不存在则创建
		if (databaseFile != null && !databaseFile.getParentFile().exists())
		{
			databaseFile.getParentFile().mkdirs();
		}
		
		mDatabase = SQLiteDatabase.openOrCreateDatabase(mDatabasePath, null);
		DbLogUtil.d(TAG, "数据库创建成功![" + mDatabasePath + "]");
		return this;
	}
	
	@Override
	public synchronized void closeDatabase() {
		if (mDatabase == null)
		{
			DbLogUtil.d(TAG, "数据库对象为空!");
			return ;
		}
		
		if (mDatabase.isOpen())
		{
			mDatabase.close();
			DbLogUtil.d(TAG, "关闭数据库成功!");
		}
		else
		{
			DbLogUtil.d(TAG, "数据库已经关闭!");
		}
	}
	
	@Override
	public synchronized DaoManager reOpenDatabase() {
		DbLogUtil.e(TAG, "重新打开数据库:" + mDatabasePath);
		
		// 关闭数据库
		closeDatabase();
		
		// 打开数据库
		openDatabase();
		
		return this;
	}
	
	@Override
	public SQLiteDatabase getDatabase() {
		return mDatabase;
	}
	
	@Override
	public <M extends BaseModel> void deleteTable(Class<M> entityClass) {
		DbTables tables = entityClass.getAnnotation(DbTables.class);
		
		String tableName = null;
		
		if (tables == null || tables.tableName() == null || "".equals(tables.tableName().trim())){//如果没有自定义表名则使用类名
			tableName = entityClass.getSimpleName();
		}else{
			tableName = tables.tableName().trim();
		}
		
		try {
			mDatabase.execSQL("DROP TABLE IF EXISTS " + tableName);
		} catch (Exception e) {
		}
		
	}
	
	/**
	 * 获取管理容器中的数据访问帮助类
	 * @param clazz
	 * @param entityClass
	 * @return
	 */
	@Override
	public <T extends IBaseDao<M, Long>, M extends BaseModel> T getDataHelper(Class<T> clazz, Class<M> entityClass)
	{
		T baseDao = SingletonFactory.getInstance(clazz);
		if (!baseDao.isDbInited())
		{
			baseDao.init(entityClass, mDatabase);
		}
		return baseDao;
	}
	
	/**
	 * 判断数据库是否已经打开
	 * @return
	 */
	private boolean isDatabseOpened()
	{
		if (mDatabase == null)
		{
			return false;
		}
		
		return mDatabase.isOpen();
	}
}
