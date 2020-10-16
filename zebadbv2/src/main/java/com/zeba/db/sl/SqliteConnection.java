package com.zeba.db.sl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.zwz.zeba.orm.base.BaseConnection;
import com.zwz.zeba.orm.base.ColumnEntity;
import com.zwz.zeba.orm.base.TableEntity;
import com.zwz.zeba.orm.base.annotation.Column;
import com.zwz.zeba.orm.base.curd.Delete;
import com.zwz.zeba.orm.base.curd.Insert;
import com.zwz.zeba.orm.base.curd.Query;
import com.zwz.zeba.orm.base.curd.Update;

public class SqliteConnection extends BaseConnection{
	
	private AtomicInteger count=new AtomicInteger(1);
	private SQLiteDatabase db;

	public SqliteConnection(String path){
		db=SQLiteDatabase.openOrCreateDatabase(path,null);
	}

	public SqliteConnection addCount(){
		count.incrementAndGet();
		return this;
	}

	private SQLiteDatabase getWDb(){
		return db;
	}

	private SQLiteDatabase getRDb(){
		return db;
	}
	
	@Override
	public int executeUpdate(Update update) throws SQLException {
		Object[] po=SQLParser.update(update);
		System.out.println("sql="+(String)po[0]);
		int ur= getWDb().update((String)po[0],(ContentValues) po[1],(String)po[2],(String[])po[3]);
		return ur;
	}

	@Override
	public int executeDelete(Delete delete) throws SQLException {
		Object[] po=SQLParser.delete(delete);
		return getWDb().delete((String)po[0],(String)po[1],(String[])po[2]);
	}

	@Override
	public long executeInsert(Insert insert) throws SQLException {
		Object[] po=SQLParser.insert(insert);
		return getWDb().insert(insert.getTableName(),null,(ContentValues)po[0]);
	}

	@Override
	public <T> List<T> executeQuery(Query query) throws SQLException {
		String sql=SQLParser.query(query);
		System.out.println("sql="+sql);
		SQLiteDatabase sdb= getRDb();
		Cursor cursor=null;
		List<T> listData = new ArrayList<T>();
		try{
			cursor= sdb.rawQuery(sql,null);
			while(cursor.moveToNext()){
				T t = (T)query.getCloumnClass().newInstance();
				List<ColumnEntity> columnList= query.getEntity().getColumnList();
				for(ColumnEntity ce:columnList){
					int index= cursor.getColumnIndex(ce.getName());
					Field field=ce.getField();
					switch(cursor.getType(index)){
						case Cursor.FIELD_TYPE_INTEGER:
							if(field.getType()==Integer.class){
								field.set(t,cursor.getInt(index));
							}else if(field.getType()==Long.class){
								field.set(t,cursor.getLong(index));
							}
							break;
						case Cursor.FIELD_TYPE_STRING:
							if(field.getType()==String.class){
								field.set(t,cursor.getString(index));
							}
							break;
						case Cursor.FIELD_TYPE_FLOAT:
							if(field.getType()==Float.class){
								field.set(t,cursor.getFloat(index));
							}else if(field.getType()==Double.class){
								field.set(t,cursor.getDouble(index));
							}
							break;
						case Cursor.FIELD_TYPE_BLOB:
							if(field.getType()==byte[].class){
								field.set(t,cursor.getBlob(index));
							}else if(field.getType()==String.class){
								field.set(t,new String(cursor.getBlob(index),"utf-8"));
							}
							break;
					}
				}
				listData.add(t);
			}
		}catch (Exception e){
			e.printStackTrace();
			new SQLException("query fail",e);
		}finally {
			if(cursor!=null){
				cursor.close();
			}
		}
//		qr.setResultSet(ps.executeQuery());
//		qr.setStatement(ps);
		return listData;
	}

	@Override
	public void beginTransaction() throws SQLException {
		getWDb().beginTransaction();
	}

	@Override
	public void endTransaction() throws SQLException {
		getWDb().endTransaction();
	}

	@Override
	public void close() throws SQLException {
		count.decrementAndGet();
		if(count.get()==0){
			if(db!=null){
				db.close();
				db=null;
			}
		}
	}

}
