package com.zeba.db;

import android.content.ContentValues;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.zeba.db.annotation.DbColumn;
import com.zeba.db.annotation.DbColumnType;
import com.zeba.db.annotation.DbTable;

public class DbTableInfo {
    private String tableName;
    private String selectColumns;
    private List<Field> fieldList;
    private Class mClass;

    public DbTableInfo(Class cls){
        mClass=cls;
    }

    public void setTableName(String tableName){
        this.tableName=tableName;
    }

    public String getTableName(){
        if(tableName==null&&mClass.isAnnotationPresent(DbTable.class)){
            DbTable Um=(DbTable) mClass.getAnnotation(DbTable.class);
            tableName=Um.value();
        }
        return tableName;
    }

    public String getCreateSql(){
        StringBuffer sb=new StringBuffer("CREATE TABLE IF NOT EXISTS ");
        sb.append(getTableName());
        sb.append(" ("+BaseRecord.ID_NAME+" INTEGER PRIMARY KEY AUTOINCREMENT,");
        List<Field> list=getFieldList();
        int count=0;
        for (Field f : list) {
            DbColumn annotation = f.getAnnotation(DbColumn.class);
            if(!annotation.isSave()){
                continue;
            }
            if(!BaseRecord.ID_NAME.equals(annotation.name())){
                sb.append(annotation.name()+" "+annotation.type());
                if(annotation.length()!=null&&!"".equals(annotation.length())){
                    sb.append("("+annotation.length()+")");
                }
                sb.append(",");
                count++;
            }
        }
        if(count==0){
            return null;
        }
        sb.delete(sb.length()-1,sb.length());
        sb.append(");");
        return sb.toString();
    }

    public String getSelectColumns(){
        if(selectColumns==null){
            List<Field> fieldList=getFieldList();
            StringBuffer stringBuffer=new StringBuffer();
            int size=fieldList.size();
            boolean isFindId=false;
            for(int i=0;i<size;i++){
                DbColumn annotation = fieldList.get(i).getAnnotation(DbColumn.class);
                if(BaseRecord.ID_NAME.equals(annotation.name())){
                    isFindId=true;
                }
                stringBuffer.append(annotation.name());
                if(i+1<size){
                    stringBuffer.append(",");
                }
            }
            if(!isFindId){
                stringBuffer.append(","+BaseRecord.ID_NAME);
            }
            selectColumns=stringBuffer.toString();
        }
        return selectColumns;
    }

    public List<Field> getFieldList(){
        if(fieldList==null){
            fieldList=new ArrayList<>();
            Field[] fields= mClass.getDeclaredFields();
            for (Field f : fields) {
                if (f.isAnnotationPresent(DbColumn.class)) {
                    DbColumn annotation = f.getAnnotation(DbColumn.class);
                    if (annotation != null) {
                        f.setAccessible(true);
                        fieldList.add(f);
                    }
                }
            }
        }
        return fieldList;
    }

    public ContentValues getContentValues(Object obj){
        ContentValues cv=new ContentValues();
        List<Field> fieldList=getFieldList();
        try {
            for(int i=0;i<fieldList.size();i++){
                DbColumn column= fieldList.get(i).getAnnotation(DbColumn.class);
                if(!column.isSave()||BaseRecord.ID_NAME.equals(column.name())){
                    continue;
                }
                Object v= fieldList.get(i).get(obj);
                if(v instanceof String){
                    if(column.type().getType().equals(DbColumnType.BLOB.getType())){
                        cv.put(column.name(),((String)v).getBytes("utf-8"));
                    }else{
                        cv.put(column.name(),(String)v);
                    }
                }else if(v instanceof Integer){
                    cv.put(column.name(),(Integer) v);
                }else if(v instanceof Long){
                    cv.put(column.name(),(Long)v);
                }else if(v instanceof byte[]){
                    cv.put(column.name(),(byte[]) v);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return cv;
    }

}
