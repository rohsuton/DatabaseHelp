/**
 * Title: BaseDao.java
 * Description: 
 * Copyright: Copyright (c) 2013-2015 luoxudong.com
 * Company: 个人
 * Author: 罗旭东 (hi@luoxudong.com)
 * Date: 2015年7月13日 上午11:46:29
 * Version: 1.0
 */
package com.luoxudong.app.database.manager;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.luoxudong.app.database.annotations.DbFields;
import com.luoxudong.app.database.annotations.DbTables;
import com.luoxudong.app.database.annotations.Id;
import com.luoxudong.app.database.annotations.Transient;
import com.luoxudong.app.database.exception.DatabaseException;
import com.luoxudong.app.database.interfaces.IBaseDao;
import com.luoxudong.app.database.model.BaseModel;
import com.luoxudong.app.database.model.Page;
import com.luoxudong.app.database.utils.DbLogUtil;
import com.luoxudong.app.database.vo.ColumnInfo;

/** 
 * ClassName: BaseDao
 * Description:数据库操作基类实现类
 * Create by: 罗旭东
 * Date: 2015年7月13日 上午11:46:29
 */
public abstract class BaseDao<T extends BaseModel, ID extends Serializable> implements IBaseDao<T, ID>
{
	private static final String TAG = BaseDao.class.getName();
	
	/** 同步锁 */
	private Object mSyncObj = new Object();
	
	/** 数据库对象  */
	private SQLiteDatabase mDatabase = null;
	
	/** 数据库表列属性集合 */
	private List<ColumnInfo> columnInfos = null;
	
	/** 数据库是否初始化 */
	private boolean mIsDbInited = false;
	
	/** 数据库对象实体类 */
	private Class<T> mEntityClass = null;
	
	/** 表名 */
	private String mTableName = null;
	
	/** 主键名称 */
	private String mPrimaryKey = null;
	
	@Override
	public synchronized boolean init(Class<T> entityClass, SQLiteDatabase database)
	{
		if(!mIsDbInited)
		{
			this.mDatabase = database;
			mEntityClass = entityClass;

			if (mDatabase == null)
			{
				DbLogUtil.w(TAG, "[init]数据库对象为空!");
				return false;
			}
			
			if(!mDatabase.isOpen())
			{
				DbLogUtil.w(TAG, "[init]数据库已关闭!");
				return false;
			}
			
			DbTables tables = entityClass.getAnnotation(DbTables.class);
			
			if (tables == null || tables.tableName() == null || "".equals(tables.tableName().trim())){//如果没有自定义表名则使用类名
				mTableName = entityClass.getSimpleName();
			}else{
				mTableName = tables.tableName().trim();
			}
			
			//数据库表不存在则先创建
			createTable();

			columnInfos = new ArrayList<ColumnInfo>();

			//初始化表信息
			initColumnInfoList(entityClass);
			
			mPrimaryKey = getPrimaryKey();

			mIsDbInited = true;
		}
		return true;
	}
	
	@Override
	public boolean isDbInited() {
		return mIsDbInited;
	}
	
	@Override
	public void checkTableColumn() {
		String sql = "select * from " + getTableName() + " limit 1,0";
		Cursor cursor = null;
		String fieldName = null;
		boolean isFieldExist = false;
		
		cursor = mDatabase.rawQuery(sql, null);
		String[] columnNames = cursor.getColumnNames();
		Field[] fields = mEntityClass.getDeclaredFields();
		
		//遍历所有列
		//StringBuilder updateSql = new StringBuilder();
		for (Field fieldItem : fields){
			DbFields dbFields = fieldItem.getAnnotation(DbFields.class);
			
			if (fieldItem.getAnnotation(Transient.class) != null){//ORM框架忽略标记
				continue;
			}
			
			if (hasGetMethod(fieldItem, mEntityClass) == null){//无对应的get方法
				continue;
			}
			
			ColumnInfo columnInfo = new ColumnInfo();
			
			if (dbFields != null && dbFields.columnName() != null && !"".equals(dbFields.columnName()))
			{
				fieldName = dbFields.columnName();
			}
			else
			{
				fieldName = fieldItem.getName();
			}
			
			isFieldExist = false;
			for (String columnName : columnNames)
			{
				columnInfo.setColumnName(columnName);
				
				if (columnName.equals(fieldName))
				{
					isFieldExist = true;
					break;
				}
			}
			
			if (!isFieldExist){//有新添加的字段
				
			}
		}
		if (cursor != null && !cursor.isClosed())
		{
			try
			{
				cursor.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public long insert(T entity) throws Exception {
		synchronized (mSyncObj) {
			if (!checkDbStatus())
			{
				return -1;
			}
			
			String tableName = getTableName();
			Map<String, String> values = getValues(entity);
			Long result = mDatabase.insert(tableName, null, getContentValues(values));
			return result;
		}
	}

	@Override
	public boolean batchInsert(List<T> entityList) throws Exception {
		boolean ret = false;
		
		if(!checkDbStatus())
		{
			return ret;
		}

		String tableName = getTableName();

		int size = 0;
		if (entityList != null)
		{
			mDatabase.beginTransaction();
			for (T item : entityList)
			{
				Map<String, String> values = getValues(item);
				Long result = mDatabase.insert(tableName, null, getContentValues(values));
				if (result != -1)
				{
					size++;
				}
				
				if (size == entityList.size())//全部成功则设置事务成功，否则不提交事务
				{
					mDatabase.setTransactionSuccessful();
					ret = true;
				}
			}
			
			mDatabase.endTransaction();
		}
		
		return ret;
	}
	
	@Override
	public int delete(T where) throws Exception {
		synchronized (mSyncObj)
		{
			if(!checkDbStatus())
			{
				return 0;
			}
			
			String tableName = getTableName();
			Map<String, String> values = getValues(where);
			Condition condition = new Condition(values);
			int result = mDatabase.delete(tableName, condition.getWhereClause(), condition.getWhereArgs());
			return result;
		}
	}
	
	@Override
	public int deleteAll() {
		return mDatabase.delete(getTableName(), null, null);
	}
	
	@Override
	public int deleteById(Object id) throws Exception {
		return mDatabase.delete(getTableName(), " " + mPrimaryKey + "=?", new String[]{String.valueOf(id)});
	}

	@Override
	public boolean batchDelete(List<T> whereList) throws Exception {
		int num = 0;
		boolean ret = false;
		
		if(!checkDbStatus())
		{
			return ret;
		}
		
		String tableName = getTableName();
		mDatabase.beginTransaction();//事务开始
		for (T entity : whereList)
		{
			Map<String, String> values = getValues(entity);
			Condition condition = new Condition(values);
			int count = mDatabase.delete(tableName, condition.getWhereClause(), condition.getWhereArgs());
			
			num = num + count;
		}
		
		if (num == whereList.size())
		{
			//全部删除成功
			mDatabase.setTransactionSuccessful();
			ret = true;
		}
		
		mDatabase.endTransaction();//事务结束
		
		return ret;
	}


	@Override
	public int update(T entity, T where) throws Exception {
		synchronized (mSyncObj)
		{
			if(!checkDbStatus())
			{
				return 0;
			}

			String tableName = getTableName();
			Map<String, String> values = getValues(entity);
			Condition condition = new Condition(getValues(where));

			int result = mDatabase.update(tableName, getContentValues(values), condition.getWhereClause(), condition.getWhereArgs());
			
			return result;
		}
	}

	@Override
	public int update(T entity, String whereClause, String[] whereArgs) {
		synchronized (mSyncObj) {
			if(!checkDbStatus())
			{
				return 0;
			}
			
			String tableName = getTableName();
			Map<String, String> values = getValues(entity);
			int result = mDatabase.update(tableName, getContentValues(values), whereClause, whereArgs);
			return result;
		}
	}
	
	@Override
	public int updateById(T entity, Object id) {
		if (!checkDbStatus()) {
			return 0;
		}
		
		return update(entity, " " + mPrimaryKey + "=?", new String[] { String.valueOf(id) });
	}
	
	@Override
	public int update(Map<String, String> values, String whereClause, String[] whereArgs) {
		synchronized (mSyncObj) {
			if(!checkDbStatus())
			{
				return 0;
			}
			String tableName = getTableName();
			int result = mDatabase.update(tableName, getContentValues(values), whereClause, whereArgs);
			return result;
		}
	}
	
	@Override
	public T queryById(Object id) {
		T result = null;
		if(!checkDbStatus())
		{
			return null;
		}
		
		Cursor cursor = null;
		try
		{
			String sql = "select * from " + getTableName() + " where " + mPrimaryKey + "=?";
			cursor = mDatabase.rawQuery(sql, new String[]{String.valueOf(id)});
			List<T> resultList = getResult(cursor, mEntityClass);
			
			if (resultList != null && resultList.size() > 0){
				result = resultList.get(0);
			}
		}
		catch (Exception e)
		{
			throw new DatabaseException("[queryById]查询时出错.", e);
		}
		finally
		{
			if (cursor != null && !cursor.isClosed())
			{
				cursor.close();
			}
		}
		return result;
	}

	@Override
	public List<T> queryByCondition(T condition) {
		return queryByCondition(condition, null);
	}

	@Override
	public List<T> queryByCondition(T condition, String order) {
		
		return queryByCondition(condition, order, null);
	}
	
	@Override
	public List<T> queryByCondition(T condition, String order, String limit) {
		List<T> result = new ArrayList<T>();
		if(!checkDbStatus())
		{
			return result;
		}
		
		Cursor cursor = null;
		try
		{
			Condition conditions = new Condition(getValues(condition));
			String sql = "select * from " + getTableName() + " where " + conditions.getWhereClause();
			
			if (order != null && order.length() > 0){
				sql += " order by " + order.replace("order by", "");
			}
			
			if (limit != null && limit.length() > 0){
				sql += " limit " + limit.replace("limit", "");
			}
			
			cursor = mDatabase.rawQuery(sql, conditions.getWhereArgs());
			result = getResult(cursor, mEntityClass);
		}
		catch (Exception e)
		{
			throw new DatabaseException("[queryByCondition]查询时出错.", e);
		}
		finally
		{
			if (cursor != null && !cursor.isClosed())
			{
				cursor.close();
			}
		}
		return result;
	}

	@Override
	public List<T> queryAll() {
		List<T> result = new ArrayList<T>();
		if(!checkDbStatus())
		{
			return result;
		}
		
		Cursor cursor = null;
		try
		{
			String sql = "select * from " + getTableName();
			
			cursor = mDatabase.rawQuery(sql, null);
			result = getResult(cursor, mEntityClass);
		}
		catch (Exception e)
		{
			throw new DatabaseException("[queryAll]查询时出错.", e);
		}
		finally
		{
			if (cursor != null && !cursor.isClosed())
			{
				cursor.close();
			}
		}
		return result;
	}

	@Override
	public Page<T> queryByPage(Page<T> page) {
		if (page == null){
			return null;
		}
		
		List<T> result = queryByCondition(page.getConditions(), page.getOrder(), ((page.getPageNum() - 1) * page.getPageSize()) + "," + page.getPageSize());
		page.setTotalSize((int)queryCount(page.getConditions()));
		page.setResult(result);
		page.setResultNum(result == null ? 0 : result.size());
		return page;
	}
	
	@Override
	public List<T> query(String sql, String[] selectionArgs, Class<T> entityClass) {
		List<T> result = new ArrayList<T>();
		if(!checkDbStatus())
		{
			return result;
		}
		
		Cursor cursor = null;
		try
		{
			cursor = mDatabase.rawQuery(sql, selectionArgs);
			result = getResult(cursor, entityClass);
		}
		catch (Exception e)
		{
			throw new DatabaseException("查询时出错.", e);
		}
		finally
		{
			if (cursor != null && !cursor.isClosed())
			{
				cursor.close();
			}
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> query(String sql, String[] selectionArgs) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		
		if(!checkDbStatus())
		{
			return result;
		}
		
		Cursor cursor = null;
		try
		{
			cursor = mDatabase.rawQuery(sql, selectionArgs);
			result = getResult(cursor);
		}
		catch (Exception e)
		{
			throw new DatabaseException("查询时出错.", e);
		}
		finally
		{
			if (cursor != null && !cursor.isClosed())
			{
				cursor.close();
			}
		}
		return result;
	}

	@Override
	public long queryCount(T where) {
		if(!checkDbStatus())
		{
			return 0;
		}
		String tableName = getTableName();
		Map<String, String> values = getValues(where);
		Cursor cursor = null;
		int count = 0;
		try
		{
			Condition condition = new Condition(values);
			cursor = mDatabase.query(tableName, new String[] { "count(*) as recordCount" }, condition.getWhereClause(), condition.getWhereArgs(), null, null, null, null);
			if (cursor.moveToFirst())
			{
				count = cursor.getInt(cursor.getColumnIndex("recordCount"));
			}
		}
		catch (Exception e)
		{
			throw new DatabaseException("查询符合条件的记录数出错", e);
		}
		finally
		{
			if (cursor != null && !cursor.isClosed())
			{
				cursor.close();
			}
		}
		return count;
	}

	public SQLiteDatabase getDatabase() {
		return mDatabase;
	}
	
	public Class<T> getEntityClass() {
		return mEntityClass;
	}
	
	/**
	 * 创建数据库表,保护类型，可用于子类重写
	 * @return 创建表sql语句
	 */
	protected String getCreateTableSql(){
		Field[] fields = mEntityClass.getDeclaredFields();
		if (fields == null || fields.length == 0){
			DbLogUtil.e(TAG, "找不到表属性!");
		}
		
		StringBuilder createSql = new StringBuilder("create table if not exists " + getTableName() + "(");
		
		//遍历所有列
		String fieldName = null;
		int count = 0;
		for (Field fieldItem : fields)
		{
			DbFields dbFields = fieldItem.getAnnotation(DbFields.class);
			
			if (fieldItem.getAnnotation(Transient.class) != null){//ORM框架忽略标记
				continue;
			}
			
			if (hasGetMethod(fieldItem, mEntityClass) == null){//无对应的get方法
				continue;
			}
			
			if (count != 0){
				createSql.append(",");
			}
			
			if (dbFields != null && dbFields.columnName() != null && !"".equals(dbFields.columnName()))
			{
				fieldName = dbFields.columnName();
			}
			else
			{
				fieldName = fieldItem.getName();
			}
			
			Class<?> classType = fieldItem.getType();
			if (classType == Byte.class || classType == byte.class
				|| classType == Short.class || classType == short.class
				|| classType == Integer.class || classType == int.class
				|| classType == Long.class || classType == long.class
				|| classType == Boolean.class || classType == boolean.class){
				createSql.append(fieldName).append(" INTEGER");
			}else if (classType == String.class
					|| classType == Character.class || classType == char.class){
				createSql.append(fieldName).append(" TEXT");
			}else if (classType == Float.class || classType == float.class
					|| classType == Double.class || classType == double.class){
				createSql.append(fieldName).append(" REAL");
			}else if (classType == Byte[].class || classType == byte[].class){
				createSql.append(fieldName).append(" BLOB");
			}else{
				DbLogUtil.e(TAG, "不支持数据类型.>>>" + classType.getName());
			}
			
			if (dbFields != null && !dbFields.nullable()){//不允许为空
				createSql.append(" NOT NULL");
			}
			
			if (fieldItem.getAnnotation(Id.class) != null){//主键
				createSql.append(" PRIMARY KEY AUTOINCREMENT");
			}
			
			if (dbFields != null && dbFields.unique()){//唯一性
				createSql.append(" UNIQUE");
			}
			count++;
		}
		
		createSql.append(")");
		
		DbLogUtil.i(TAG, "create sql:" + createSql.toString());
		
		return createSql.toString();
	}
	
	/**
	 * 
	 * @description:获取数据库名称
	 * @return String 数据库名称
	 * @throws
	 */
	protected String getTableName(){
		return mTableName;
	}
	
	/**
	 * 创建表
	 */
	private void createTable()
	{
		String sql = getCreateTableSql();
		mDatabase.execSQL(sql);
	}
	
	/**
	 * 初始化数据库表属性
	 * @param entityClass
	 */
	private void initColumnInfoList(Class<T> entityClass)
	{
		String sql = "select * from " + getTableName() + " limit 1,0";
		Cursor cursor = null;
		try
		{
			cursor = mDatabase.rawQuery(sql, null);
			String[] columnNames = cursor.getColumnNames();
			Field[] fields = entityClass.getDeclaredFields();
			
			if (columnNames == null || fields == null){
				return;
			}
			
			for (Field field : fields)
			{
				field.setAccessible(true);
			}
			
			//遍历所有列
			String fieldName = null;
			for (Field fieldItem : fields){
				DbFields dbFields = fieldItem.getAnnotation(DbFields.class);
				
				if (fieldItem.getAnnotation(Transient.class) != null){//ORM框架忽略标记
					continue;
				}
				
				if (hasGetMethod(fieldItem, entityClass) == null){//无对应的get方法
					continue;
				}
				
				ColumnInfo columnInfo = new ColumnInfo();
				
				if (dbFields != null && dbFields.columnName() != null && !"".equals(dbFields.columnName()))
				{
					fieldName = dbFields.columnName();
				}
				else
				{
					fieldName = fieldItem.getName();
				}
				
				for (String columnName : columnNames)
				{
					columnInfo.setColumnName(columnName);
					
					if (columnName.equals(fieldName))
					{
						columnInfo.setField(fieldItem);
						columnInfo.setSetMethod(hasSetMethod(fieldItem, entityClass));
						columnInfo.setGetMethod(hasGetMethod(fieldItem, entityClass));
						break;
					}
				}
				
				columnInfos.add(columnInfo);
			}

		}
		catch (Exception e)
		{
			DbLogUtil.e(TAG, "初始化表列信息出错");
			throw new DatabaseException("初始化表列信息出错.", e);
		}
		finally
		{
			if (cursor != null && !cursor.isClosed())
			{
				try
				{
					cursor.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * 判断表对应的类中是否有列名对应的set方法
	 * @param field 表属性名
	 * @param clazz 表对象
	 * @return set方法对象
	 */
	private Method hasSetMethod(Field field, Class<? extends Object> clazz)
	{
		Method result = null;
		Method[] methods = clazz.getDeclaredMethods();
		String fieldName = field.getName();
		String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase(Locale.getDefault()) + fieldName.substring(1);
		
		for (Method method : methods)
		{
			if (method.getName().equals(setMethodName))
			{
				result = method;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 判断表对应的类中是否有列名对应的get方法
	 * @param field 表属性名
	 * @param clazz 表对象
	 * @return get方法对象
	 */
	private Method hasGetMethod(Field field, Class<? extends Object> clazz)
	{
		Method result = null;
		Method[] methods = clazz.getDeclaredMethods();
		String fieldName = field.getName();
		String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase(Locale.getDefault()) + fieldName.substring(1);
		for (Method method : methods)
		{
			if (method.getName().equals(getMethodName))
			{
				result = method;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 检测数据库状态
	 * @return
	 */
	private boolean checkDbStatus()
	{
		if(!mIsDbInited)
		{
			DbLogUtil.w(TAG, "[checkDbStatus]数据库未初始化!");
			return false;
		}
		if(mDatabase == null)
		{
			DbLogUtil.w(TAG, "[checkDbStatus]数据库对象为空!");
			return false;
		}
		if(!mDatabase.isOpen())
		{
			DbLogUtil.w(TAG, "[checkDbStatus]数据库未打开!");
			return false;
		}
		
		return true;
	}
	
	/**
	 * 获取数据库表的键值对应列表
	 * @param entity
	 * @return
	 */
	private Map<String, String> getValues(T entity)
	{
		Map<String, String> result = new HashMap<String, String>();

		if (entity == null)
		{
			DbLogUtil.d(TAG, getClass().getSimpleName() + "  实体类为空");
			return result;
		}

		for (ColumnInfo columnInfo : columnInfos)
		{
			try
			{
				//检测get方法是否存在
				if (columnInfo.getGetMethod() != null)
				{
					Field field = columnInfo.getField();
					
					if (field.get(entity) != null){//过滤掉值为空的属性
						result.put(columnInfo.getColumnName(), field.get(entity) == null ? null : field.get(entity).toString());
					}
					
				}
			}
			catch (IllegalArgumentException e)
			{
				DbLogUtil.e(TAG, getClass().getSimpleName() + "  对象反射成map对象时出错.");
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				DbLogUtil.e(TAG, getClass().getSimpleName() + "  对象反射成map对象时出错.");
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * map转换成ContentValues对象
	 * @param map
	 * @return
	 */
	private ContentValues getContentValues(Map<String, String> map)
	{
		ContentValues result = new ContentValues();
		Set<String> keys = map.keySet();
		for (String key : keys)
		{
			String value = map.get(key);
			if (value != null)
			{
				result.put(key, value);
			}
		}
		return result;
	}
	
	/**
	 * 获取主键属性名称
	 * @return String
	 */
	private String getPrimaryKey()
	{
		String fieldName = null;
		
		for (ColumnInfo columnInfo : columnInfos)
		{
			try
			{
				//检测get方法是否存在
				if (columnInfo.getGetMethod() != null)
				{
					Field field = columnInfo.getField();
					
					if (field.getAnnotation(Id.class) != null)
					{
						fieldName = columnInfo.getColumnName();
						break;
					}
				}
			}catch (IllegalArgumentException e)
			{
				DbLogUtil.e(TAG, getClass().getSimpleName() + "  对象反射成map对象时出错.");
				e.printStackTrace();
			}
		}
		
		return fieldName;
	}
	/**
	 * 根据数据库cursor获取list对象结果集
	 * @param cursor 查询结果游标
	 * @param entityClass 实体类
	 * @return 结果对象列表
	 * @throws Exception
	 */
	private List<T> getResult(Cursor cursor, Class<T> entityClass) throws Exception
	{
		List<T> resultList = new ArrayList<T>();
		if (entityClass == null)
		{
			entityClass = mEntityClass;
		}
		while (cursor.moveToNext())
		{
			T item = entityClass.newInstance();
			for (ColumnInfo columnInfo : columnInfos)
			{
				String columnName = columnInfo.getColumnName();
				Field field = columnInfo.getField();
				Method method = columnInfo.getSetMethod();
				if (method != null && field != null)
				{
					Class<? extends Object> type = field.getType();
					Integer columnIndex = cursor.getColumnIndex(columnName);
					if (columnIndex != -1)
					{
						if (type == String.class)
						{
							method.invoke(item, cursor.getString(columnIndex));
						}
						else if (type == Long.class || type == long.class)
						{
							method.invoke(item, cursor.getLong(columnIndex));
						}
						else if (type == Integer.class || type == int.class)
						{
							method.invoke(item, cursor.getInt(columnIndex));
						}
						else if (type == Float.class || type == float.class)
						{
							method.invoke(item, cursor.getFloat(columnIndex));
						}
						else if (type == Double.class || type == double.class)
						{
							method.invoke(item, cursor.getDouble(columnIndex));
						}
						else if (type == byte[].class)
						{
							method.invoke(item, cursor.getBlob(columnIndex));
						}
						else
						{
							method.invoke(item, cursor.getString(columnIndex));
							DbLogUtil.e(TAG, getClass().getSimpleName() + " 实体类的类型和数据库字段类型不一致.");
						}
					}
					else
					{
						DbLogUtil.w(TAG, getClass().getSimpleName() + " 查询语句选择列表不包含此列  columnName:" + columnName);
					}
				}
			}
			resultList.add(item);
		}
		return resultList;
	}
	
	/**
	 * 根据数据库cursor获取list键值对应结果集
	 * @param cursor 数据库查询结果游标
	 * @return 查询结果
	 */
	private List<Map<String, Object>> getResult(Cursor cursor)
	{
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		String[] columnNames = cursor.getColumnNames();

		while (cursor.moveToNext())
		{
			Map<String, Object> item = new HashMap<String, Object>();
			if (columnNames != null)
			{
				for (String columnName : columnNames)
				{
					int columnIndex = cursor.getColumnIndex(columnName);
					item.put(columnName, cursor.getString(columnIndex));
				}
				resultList.add(item);
			}
			else
			{
				DbLogUtil.w(TAG, getClass().getSimpleName() + "  选择列表为空.");
			}
		}
		return resultList;
	}

	/**
	 * 查询条件
	 */
	private class Condition
	{
		private String whereClause;

		private String[] whereArgs;

		public Condition(Map<String, String> map)
		{
			List<String> list = new ArrayList<String>();
			StringBuilder sb = new StringBuilder("");
			sb.append(" 1=1 ");
			Set<String> keys = map.keySet();
			for (String key : keys)
			{
				String value = map.get(key);
				if (value != null)
				{
					sb.append(" and " + key + " = ?");
					list.add(value);
				}
			}
			whereClause = sb.toString();
			whereArgs = list.toArray(new String[list.size()]);
		}

		private String getWhereClause()
		{
			return whereClause;
		}

		private String[] getWhereArgs()
		{
			return whereArgs;
		}
		
		@Override
		public String toString()
		{
			return "Condition [whereClause=" + whereClause + ", whereArgs=" + Arrays.toString(whereArgs) + "]";
		}
	}
}
