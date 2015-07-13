/**
 * Title: IBaseDao.java
 * Description: 
 * Copyright: Copyright (c) 2013-2015 luoxudong.com
 * Company: 个人
 * Author: 罗旭东 (hi@luoxudong.com)
 * Date: 2015年7月13日 上午11:41:49
 * Version: 1.0
 */
package com.luoxudong.app.database.interfaces;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import android.database.sqlite.SQLiteDatabase;

import com.luoxudong.app.database.model.BaseModel;
import com.luoxudong.app.database.model.Page;

/** 
 * ClassName: IBaseDao
 * Description:数据库操作Dao层接口
 * Create by: 罗旭东
 * Date: 2015年7月13日 上午11:41:49
 */
public interface IBaseDao<T extends BaseModel, ID extends Serializable> {
	
	/**
	 * 初始化数据库表
	 * @param entityClass 数据库实体类
	 * @param database 数据库对象
	 * @return
	 */
	public boolean init(Class<T> entityClass, SQLiteDatabase database);
	
	/**
	 * @return 数据库是否初始化
	 */
	public boolean isDbInited();
	
	/**
	 * 表结构检测
	 */
	public void checkTableColumn();
	
	/**
	 * 插入一条记录,保存对象信息到数据库中
	 * @param entity 需要保存到数据库中的对象
	 * @return 插入成功后的记录ID,失败返回-1
	 * @throws Exception
	 */
	public long insert(T entity) throws Exception;

	/**
	 * 批量插入一条记录,保存对象信息到数据库中
	 * @param entityList 需要保存到数据库中的对象集合
	 * @return 批量插入是否成功
	 * @throws Exception
	 */
	public boolean batchInsert(List<T> entityList) throws Exception;

	/**
	 * 删除数据
	 * @param entity 删除记录条件
	 * @return 删除记录行数
	 * @throws Exception
	 */
	public int delete(T where) throws Exception;
	
	/**
	 * 删除表中所有记录
	 * @return int 删除记录行数
	 */
	public int deleteAll();
	
	/**
	 * 删除指定ID项内容
	 * @param id
	 * @return int
	 */
	public int deleteById(Object id) throws Exception;

	/**
	 * 批量删除数据
	 * @param entityList 删除条件
	 * @return 删除是否成功
	 * @throws Exception
	 */
	public boolean batchDelete(List<T> whereList) throws Exception;
	
	/**
	 * 更新数据
	 * @param entity 需要更新的数据对象
	 * @param where 更新记录条件
	 * @return 更新成功的记录行数
	 * @throws Exception
	 */
	public int update(T entity, T where) throws Exception;

	/**
	 * 更新数据
	 * @param entity 需要更新的数据对象
	 * @param whereClause 需要更新的记录条件
	 * @param whereArgs 满足条件的参数
	 * @return 更新成功的记录行数
	 */
	public int update(T entity, String whereClause, String[] whereArgs);
	
	/**
	 * 更新主键对应的记录
	 * @param entity
	 * @param id
	 * @return int
	 */
	public int updateById(T entity, Object id);
	
	/**
	 * 更新数据
	 * @param values 需要更新的数据字段
	 * @param whereClause 需要更新的记录条件
	 * @param whereArgs 满足条件的参数
	 */
	public int update(Map<String, String> values, String whereClause, String[] whereArgs);
	

	/**
	 * 根据ID查询记录，ID必须使用@PrimaryKey注释标记,不支持组合主键
	 * @param id
	 * @return
	 * @return T
	 */
	public T queryById(Object id);
	
	/**
	 * 根据查询条件查询
	 * @param condition 查询条件对象
	 * @return
	 */
	public List<T> queryByCondition(T condition);
	
	/**
	 * 根据查询条件查询
	 * @param condition 查询条件对象
	 * @param order 排序
	 * @return
	 */
	public List<T> queryByCondition(T condition, String order);
	
	/**
	 * 根据条件分页查询排序
	 * @param condition 查询条件
	 * @param order 排序规则
	 * @param limit 分页
	 */
	public List<T> queryByCondition(T condition, String order, String limit);
	
	/**
	 * 查询所有记录
	 * @return
	 */
	public List<T> queryAll();
	
	/**
	 * 分页查询
	 * @param page
	 * @return
	 */
	public Page<T> queryByPage(Page<T> page);
	
	/**
	 * 获取总记录条数
	 * @param where 查询条件
	 */
	public long queryCount(T where);
	
	/**
	 * 查询记录,查询结果将以列表形式返回，返回对象列表
	 * @param sql 查询sql语句
	 * @param selectionArgs 查询条件对应的参数
	 * @param entityClass 模板类
	 * @return 返回对象列表
	 */
	public List<T> query(String sql, String[] selectionArgs, Class<T> entityClass);

	/**
	 * 查询记录，查询结果以列表返回，返回简直对应列表
	 * @param sql 查询sql语句
	 * @param selectionArgs 查询条件对应的参数
	 * @return 结果列表
	 */
	public List<Map<String, Object>> query(String sql, String[] selectionArgs);
}
