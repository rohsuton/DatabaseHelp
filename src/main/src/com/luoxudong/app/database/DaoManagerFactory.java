/**
 * Title: DaoManagerFactory.java
 * Description: 
 * Copyright: Copyright (c) 2013-2015 luoxudong.com
 * Company: 个人
 * Author: 罗旭东 (hi@luoxudong.com)
 * Date: 2015年7月13日 上午11:48:03
 * Version: 1.0
 */
package com.luoxudong.app.database;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.luoxudong.app.database.exception.DatabaseException;
import com.luoxudong.app.database.interfaces.IDaoManager;
import com.luoxudong.app.database.manager.DaoManager;
import com.luoxudong.app.singletonfactory.SingletonFactory;

/** 
 * ClassName: DaoManagerFactory
 * Description:数据库管理工厂类,支持多数据库管理
 * Create by: 罗旭东
 * Date: 2015年7月13日 上午11:48:03
 */
public class DaoManagerFactory {
	/**
	 * 缓存数据库对象列表
	 */
	private static Map<String, IDaoManager> sDatabaseManagerCache = Collections.synchronizedMap(new HashMap<String, IDaoManager>());

	public static IDaoManager getDaoManager(String databasePath)
	{
		return getDatabaseManagerInstance(databasePath);
	}
	
	/**
	 * 
	 * getDatabaseManagerInstance
	 *  创建一个数据库对象，并放入数据库对象列表中
	 */
	public static IDaoManager getDatabaseManagerInstance(String path)
	{
		IDaoManager result = null;
		synchronized (SingletonFactory.class)
		{
			if (sDatabaseManagerCache == null)
			{
				sDatabaseManagerCache = Collections.synchronizedMap(new HashMap<String, IDaoManager>());
			}
			
		}
		
		synchronized (sDatabaseManagerCache)
		{
			result = sDatabaseManagerCache.get(path);
			if (result == null)
			{
				result = createInstance(DaoManager.class,new Class[]{String.class},new Object[]{path});
				sDatabaseManagerCache.put(path, result);
			}
		}
		return result;

	}
	
	/**
	 * 利用反射创建对象,构造函数带参数
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T> T createInstance(Class<T> clazz,Class<?>[] parameterTypes,Object[] paramValues)
	{
		
		try
		{
			Constructor<?> constructor = clazz.getConstructor(parameterTypes);//获取构造函数
			
			T result = (T) constructor.newInstance(paramValues);//构造函数参数
			return result;
		}
		catch (InstantiationException e)
		{
			throw new DatabaseException("创建对象实例失败!", e);
		}
		catch (Exception e)
		{
			throw new DatabaseException("创建对象实例失败!", e);
		}

	}
	
	/**
	 * 释放单例对象缓存
	 */
	public static void releaseCache()
	{
		//释放数据库对象
		if (sDatabaseManagerCache != null)
		{
			synchronized (sDatabaseManagerCache)
			{
				for(String key : sDatabaseManagerCache.keySet())
				{
					IDaoManager daoManager = sDatabaseManagerCache.get(key);
					daoManager.closeDatabase();
					//daoManager = null;
				}
				sDatabaseManagerCache.clear();
			}
		}
		
	}
	
	@Override
	protected void finalize() throws Throwable {
		releaseCache();
		super.finalize();
	}
}
