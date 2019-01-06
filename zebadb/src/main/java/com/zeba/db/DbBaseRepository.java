package com.zeba.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;

import com.zeba.db.annotation.DbColumn;
import com.zeba.db.listener.DbErrorCB;
import com.zeba.db.listener.DbExecResult;
import com.zeba.db.listener.DbQueryResult;
import com.zeba.db.listener.DbSuccessCB;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DbBaseRepository<T extends BaseRecord>{
    private Class<T> mClass;
    private DbTableInfo tableInfo;
    private SQLiteDatabase sqLiteDatabase;
    private SQLiteOpenHelper sqLiteOpenHelper;

    public DbBaseRepository(){
        tableInfo=new DbTableInfo(getRecordClass());
        initDB();
    }

    public DbTableInfo getTableInfo(){
        return tableInfo;
    }

    public Class<T> getAnClass(){
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] ptype = ((ParameterizedType) type).getActualTypeArguments();
            return (Class<T>) ptype[0];
        } else {
            return null;
        }
    }

    private Class<T> getRecordClass(){
        if(mClass==null){
            mClass=getAnClass();
        }
        return mClass;
    }

    public void initDB(){
        sqLiteOpenHelper= onCreateDB();
        if(sqLiteOpenHelper==null){
            return;
        }
        SQLiteDatabase db=sqLiteOpenHelper.getWritableDatabase();
        ZDbInitUtil.createOrAlterTable(db,tableInfo);
    }

    private Handler mainHandler;
    public Handler initHandler(){
        Looper looper=Looper.myLooper();
        if(Looper.myLooper()==null||looper.equals(Looper.getMainLooper())){
            if(mainHandler==null){
                mainHandler=new Handler(Looper.getMainLooper());
            }
            return mainHandler;
        }
        return new Handler(Looper.myLooper());
    }

    private AtomicInteger openCount=new AtomicInteger();
    private AtomicBoolean dbLock=new AtomicBoolean(false);

    private void lockDB(){
        while(!dbLock.compareAndSet(false,true)){

        }
    }

    private void unLockDB(){
        while(!dbLock.compareAndSet(true,false)){

        }
    }

    protected abstract void postCloseDB();

    protected void closeDB(){
        lockDB();
        openCount.decrementAndGet();
        if(openCount.get()<=0){
            if(sqLiteDatabase!=null&&sqLiteDatabase.isOpen()){
                sqLiteDatabase.close();
                sqLiteDatabase=null;
            }
        }
        unLockDB();
    }

    public abstract SQLiteOpenHelper onCreateDB();
    public abstract void postRunnable(Runnable runnable);

    public SQLiteDatabase openDB(){
        lockDB();
        openCount.incrementAndGet();
        if(sqLiteDatabase==null){
            sqLiteDatabase=sqLiteOpenHelper.getWritableDatabase();
        }
        unLockDB();
        return sqLiteDatabase;
    }

    public void findOne(final ZDbWhere where, final DbSuccessCB<T> successCB, final DbErrorCB errorCB){
        findList(new ZDbQuery().count(1).index(1).where(where), new DbSuccessCB<DbListData<T>>() {
            @Override
            public void success(DbListData<T> tDbListData) {
                if(successCB==null){
                    return;
                }
                if(tDbListData!=null&&tDbListData.getData().size()!=0){
                    successCB.success(tDbListData.getData().get(0));
                }else{
                    successCB.success(null);
                }
            }
        },errorCB);
    }

    public DbQueryResult<T> findOne(ZDbWhere where){
        return findList(new ZDbQuery().count(1).index(1).where(where),true);
    }

    public void findAll(final DbSuccessCB<DbListData<T>> successCB, final DbErrorCB errorCB){
        findList(new ZDbQuery(),successCB,errorCB);
    }

    public DbQueryResult<T> findAll(){
        return findList(new ZDbQuery(),true);
    }

    public void findList(final ZDbQuery query, final DbSuccessCB<DbListData<T>> successCB, final DbErrorCB errorCB){
        final Handler handler = initHandler();
        postRunnable(
            new Runnable() {
                @Override
                public void run() {
                    final DbQueryResult<T> result=findList(query,false);
                    if(result.isSuccess()){
                        if(successCB==null){
                            closeDB();
                        }else{
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    successCB.success(result.getData());
                                    postCloseDB();
                                }
                            });
                        }
                    }else{
                        errorClose(handler,errorCB,result.getMsg());
                    }
                }
        });
    }

    public DbQueryResult<T> findList(ZDbQuery query){
        return findList(query,true);
    }

    private DbQueryResult<T> findList(final ZDbQuery query,boolean isCloseDB){
        SQLiteDatabase sqLiteDatabase=null;
        Cursor cursor=null;
        DbQueryResult<T> result=new DbQueryResult<>();
        try {
            sqLiteDatabase= openDB();
            StringBuffer sqlBuffer=new StringBuffer("SELECT "+tableInfo.getSelectColumns()+" FROM "+tableInfo.getTableName());
            sqlBuffer.append(query.toSqlString());
            sqlBuffer.append(";");
//                        Log.e("zwz","query sql "+sqlBuffer.toString());
            cursor= sqLiteDatabase.rawQuery(sqlBuffer.toString(),null);
            List<T> listData = new ArrayList<T>();
            List<Field> fields=tableInfo.getFieldList();
            Method method= getRecordClass().getMethod(BaseRecord.ID_METHOD_NAME,long.class);
            while(cursor.moveToNext()){
                T t = getRecordClass().newInstance();
                boolean isFindId=false;
                for(int i=0;i<fields.size();i++){
                    DbColumn column=fields.get(i).getAnnotation(DbColumn.class);
                    int index= cursor.getColumnIndex(column.name());
                    if(BaseRecord.ID_NAME.equals(column.name())){
                        isFindId=true;
                    }
                    Field field=fields.get(i);
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
                            }else if(fields.get(i).getType()==Double.class){
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
                if(!isFindId){
                    if(method!=null){
                        int index= cursor.getColumnIndex(BaseRecord.ID_NAME);
                        if(index!=-1){
                            method.invoke(t,cursor.getLong(index));
                        }
                    }
                }
                listData.add(t);
            }
            final DbListData<T> umListData=new DbListData<T>();
            umListData.setCount(cursor.getCount());
            umListData.setData(listData);
            umListData.setPageCount(query.getPageCount()==null?listData.size():query.getPageCount());
            umListData.setPageIndex(query.getPageIndex()==null?1:query.getPageIndex());
            if(cursor!=null&&!cursor.isClosed()){
                cursor.close();
            }
            result.setSuccess(true);
            result.setData(umListData);
            if(isCloseDB){
                closeDB();
            }
            return result;
        }catch (final Exception e){
            e.printStackTrace();
            if(cursor!=null&&!cursor.isClosed()){
                cursor.close();
            }
            if(isCloseDB){
                closeDB();
            }
            result.setSuccess(false);
            result.setMsg("查询失败,"+e.getMessage());
            return result;
        }
    }

    public void addOne(final T obj, final DbSuccessCB<Integer> successCB,final DbErrorCB errorCB){
        List<T> list=new ArrayList<>();
        list.add(obj);
        addList(list,successCB,errorCB);
    }

    public DbExecResult addOne(T obj){
        List<T> list=new ArrayList<>();
        list.add(obj);
        return addList(list);
    }

    public void addList(final List<T> objectList, final DbSuccessCB<Integer> successCB,final DbErrorCB errorCB){
        if(objectList==null||objectList.size()==0){
            if(successCB!=null){
                successCB.success(0);
            }
            return;
        }
        final Handler handler=initHandler();
        postRunnable(
            new Runnable() {
                @Override
                public void run() {
                    final DbExecResult result=addList(objectList,false);
                    if(result.isSuccess()){
                        if(successCB==null){
                            closeDB();
                        }else{
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    successCB.success(result.getSuccessCount());
                                    postCloseDB();
                                }
                            });
                        }
                    }else{
                        errorClose(handler,errorCB,result.getMsg());
                    }
                }
            }
        );
    }

    public DbExecResult addList(List<T> objectList){
        return addList(objectList,true);
    }

    private DbExecResult addList(List<T> objectList,boolean isCloseDB){
        DbExecResult result=new DbExecResult();
        if(objectList==null||objectList.size()==0){
            result.setSuccess(true);
            result.setSuccessCount(0);
            return result;
        }
        SQLiteDatabase sqLiteDatabase= null;
        boolean openTran=true;
        if(objectList.size()==1){
            openTran=false;
        }
        try {
            sqLiteDatabase= openDB();
            if(openTran){
                sqLiteDatabase.beginTransaction();
            }
            int successCount=0;
            for(int i=0;i<objectList.size();i++){
                T obj=objectList.get(i);
                if(obj==null){
                    continue;
                }
                sqLiteDatabase.insert(tableInfo.getTableName(),null,tableInfo.getContentValues(obj));
                successCount++;
            }
            if(openTran){
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
            }
            int count=successCount;
            if(isCloseDB){
                closeDB();
            }
            result.setSuccess(true);
            result.setSuccessCount(count);
            return result;
        }catch ( Exception e){
            e.printStackTrace();
            if(openTran){
                sqLiteDatabase.endTransaction();
            }
            if(isCloseDB){
                closeDB();
            }
            result.setSuccess(false);
            result.setMsg("插入数据失败," + e.getMessage());
            return result;
        }
    }

    public void update(final T obj,final DbSuccessCB<Integer> successCB,final DbErrorCB errorCB){
        List<T> list=new ArrayList<>();
        list.add(obj);
        updateList(list,successCB,errorCB);
    }

    public DbExecResult update(final T obj){
        List<T> list=new ArrayList<>();
        list.add(obj);
        return updateList(list);
    }

    public void updateList(final List<T> obj,final DbSuccessCB<Integer> successCB,final DbErrorCB errorCB){
        if(obj==null||obj.size()==0){
            if(successCB!=null){
                successCB.success(0);
            }
            return;
        }
        final Handler handler=initHandler();
        postRunnable(new Runnable() {
            @Override
            public void run() {
                final DbExecResult result=updateList(obj,false);
                if(result.isSuccess()){
                    if(successCB==null){
                        closeDB();
                    }else{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                successCB.success(result.getSuccessCount());
                                postCloseDB();
                            }
                        });
                    }
                }else{
                    errorClose(handler,errorCB,result.getMsg());
                }
            }
        });
    }

    public DbExecResult updateList(final List<T> obj){
        return updateList(obj,true);
    }

    private DbExecResult updateList(final List<T> obj,boolean isCloseDB){
        DbExecResult result=new DbExecResult();
        if(obj==null||obj.size()==0){
            result.setSuccess(true);
            result.setSuccessCount(0);
            return result;
        }
        SQLiteDatabase sqLiteDatabase= null;
        boolean openTran=true;
        if(obj!=null&&obj.size()==1){
            openTran=false;
        }
        try {
            sqLiteDatabase= openDB();
            int successCount=0;
            if(openTran){
                sqLiteDatabase.beginTransaction();
            }
            for(T table:obj){
                if(table==null){
                    continue;
                }
                sqLiteDatabase.update(tableInfo.getTableName(),
                        tableInfo.getContentValues(table),BaseRecord.ID_NAME+"=?",
                        new String[]{String.valueOf(table.getBaseId())});
                successCount++;
            }
            if(openTran){
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
            }
            if(isCloseDB){
                closeDB();
            }
            result.setSuccessCount(successCount);
            result.setSuccess(true);
            return result;
        }catch (final Exception e){
            e.printStackTrace();
            if(openTran){
                sqLiteDatabase.endTransaction();
            }
            if(isCloseDB){
                closeDB();
            }
            result.setSuccess(false);
            result.setMsg("更新数据失败," + e.getMessage());
            return result;
        }
    }

    public void delete(final T obj, final DbSuccessCB<Integer> successCB,final DbErrorCB errorCB){
        List<T> list=new ArrayList<>();
        list.add(obj);
        deleteList(list,successCB,errorCB);
    }

    public DbExecResult delete(final T obj){
        List<T> list=new ArrayList<>();
        list.add(obj);
        return deleteList(list);
    }

    public void deleteList(final List<T> objectList, final DbSuccessCB<Integer> successCB,final DbErrorCB errorCB){
        if(objectList==null||objectList.size()==0){
            if(successCB!=null){
                successCB.success(0);
            }
            return;
        }
        final Handler handler=initHandler();
        postRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        final DbExecResult result=deleteList(objectList,false);
                        if(result.isSuccess()){
                            if(successCB==null){
                                closeDB();
                            }else{
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        successCB.success(result.getSuccessCount());
                                        postCloseDB();
                                    }
                                });
                            }
                        }else{
                            errorClose(handler,errorCB,result.getMsg());
                        }
                    }
                }
        );
    }

    public DbExecResult deleteList(final List<T> objectList){
        return deleteList(objectList,true);
    }

    private DbExecResult deleteList(final List<T> objectList,boolean isCloseDB){
        DbExecResult result=new DbExecResult();
        if(objectList==null||objectList.size()==0){
            result.setSuccess(true);
            result.setSuccessCount(0);
            return result;
        }
        SQLiteDatabase sqLiteDatabase= null;
        boolean openTran=true;
        if(objectList==null||objectList.size()==1){
            openTran=false;
        }
        try {
            sqLiteDatabase= openDB();
            if(openTran){
                sqLiteDatabase.beginTransaction();
            }
            int successCount=0;
            for(int i=0;i<objectList.size();i++){
                BaseRecord obj=objectList.get(i);
                if(obj==null){
                    continue;
                }
                sqLiteDatabase.execSQL("DELETE FROM "+tableInfo.getTableName()+" WHERE "+BaseRecord.ID_NAME+"="+obj.getBaseId());
                successCount++;
            }
            if(openTran){
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
            }
            result.setSuccess(true);
            result.setSuccessCount(successCount);
            return result;
        }catch (final Exception e){
            e.printStackTrace();
            if(openTran){
                sqLiteDatabase.endTransaction();
            }
            result.setSuccess(false);
            result.setMsg("删除数据失败," + e.getMessage());
            return result;
    }
    }

    private void errorClose(Handler handler,final DbErrorCB errorCB,final String msg){
        if(errorCB==null){
            closeDB();
            return;
        }
        if(handler!=null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    errorCB.error(msg);
                    postCloseDB();
                }
            });
        }else{
            errorCB.error(msg);
            closeDB();
        }
    }
}
