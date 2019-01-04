package com.zeba.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zeba.db.annotation.DbColumn;

public class ZDbInitUtil {

    public static void createOrAlterTable(SQLiteDatabase db,DbTableInfo tableInfo){
        db.beginTransaction();
        String sql="select name from sqlite_master where type = 'table' and name = '"+tableInfo.getTableName()+"'";
        Cursor cursor= db.rawQuery(sql,null);
        if(cursor.getCount()<=0){
            db.execSQL(tableInfo.getCreateSql());
            cursor.close();
        }else{
            cursor.close();
            sql="PRAGMA table_info("+tableInfo.getTableName()+")";
            cursor=db.rawQuery(sql,null);
            Map<String,String> map=new HashMap<>();
            while(cursor.moveToNext()){
                map.put(cursor.getString(cursor.getColumnIndex("name")),"1");
            }
            cursor.close();
            List<Field> fieldList= tableInfo.getFieldList();
            for(Field field:fieldList){
                DbColumn dbColumn= field.getAnnotation(DbColumn.class);
                String cn=dbColumn.name();
                if(cn!=null&&!"".equals(cn)&&dbColumn.isSave()){
                    if(map.get(cn)==null){
                        StringBuffer sb=new StringBuffer("ALTER TABLE "+tableInfo.getTableName()+" ADD COLUMN ");
                        sb.append(dbColumn.name()+" "+dbColumn.type());
                        if(dbColumn.length()!=null&&!"".equals(dbColumn.length())){
                            sb.append("("+dbColumn.length()+")");
                        }
                        sb.append(";");
                        db.execSQL(sb.toString());
                    }
                }
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }
}
