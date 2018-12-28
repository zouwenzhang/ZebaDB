package zwz.core.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import zwz.core.db.annotation.DbColumn;
import zwz.core.db.listener.DbErrorCB;
import zwz.core.db.listener.DbSuccessCB;

public abstract class DbBaseRepository<T extends BaseRecord>{
    private Class<T> mClass;
    private DbTableInfo tableInfo;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private SQLiteDatabase sqLiteDatabase;
    private SQLiteOpenHelper sqLiteOpenHelper;

    public DbBaseRepository(){
        tableInfo=new DbTableInfo(getRecordClass());
        initDB();
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

    public ExecutorService getExecutor(){
        return executorService;
    }

    public Handler initHandler(){
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

    private void closeDB(){
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
                if(tDbListData!=null&&tDbListData.getData().size()!=0&&successCB!=null){
                    successCB.success(tDbListData.getData().get(0));
                }
            }
        },errorCB);
    }

    public void findAll(final DbSuccessCB<DbListData<T>> successCB, final DbErrorCB errorCB){
        findList(new ZDbQuery(),successCB,errorCB);
    }

    public void findList(final ZDbQuery query, final DbSuccessCB<DbListData<T>> successCB, final DbErrorCB errorCB){
        final Handler handler = initHandler();
        getExecutor().execute(
            new Runnable() {
                @Override
                public void run() {
                    SQLiteDatabase sqLiteDatabase=null;
                    Cursor cursor=null;
                    try {
                        sqLiteDatabase= openDB();
                        StringBuffer sqlBuffer=new StringBuffer("SELECT "+tableInfo.getSelectColumns()+" FROM "+tableInfo.getTableName());
                        sqlBuffer.append(query.toSqlString());
                        sqlBuffer.append(";");
//                        Log.e("zwz","query sql "+sqlBuffer.toString());
                        cursor= sqLiteDatabase.rawQuery(sqlBuffer.toString(),null);
                        List<T> listData = new ArrayList<T>();
                        List<Field> fields=tableInfo.getFieldList();
                        while(cursor.moveToNext()){
                            T t = getRecordClass().newInstance();
                            boolean isFindId=false;
                            for(int i=0;i<fields.size();i++){
                                DbColumn column=fields.get(i).getAnnotation(DbColumn.class);
                                int index= cursor.getColumnIndex(column.name());
                                if(BaseRecord.ID_NAME.equals(column.name())){
                                    isFindId=true;
                                }
                                switch(cursor.getType(index)){
                                    case Cursor.FIELD_TYPE_INTEGER:
                                        if(fields.get(i).getType()==Integer.class){
                                            fields.get(i).set(t,cursor.getInt(index));
                                        }else if(fields.get(i).getType()==Long.class){
                                            fields.get(i).set(t,cursor.getLong(index));
                                        }
                                        break;
                                    case Cursor.FIELD_TYPE_STRING:
                                        if(fields.get(i).getType()==String.class){
                                            fields.get(i).set(t,cursor.getString(index));
                                        }
                                        break;
                                }
                            }
                            if(!isFindId){
                                Method method= t.getClass().getMethod(BaseRecord.ID_METHOD_NAME,long.class);
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
                        closeDB();
                        if(successCB==null){
                            return;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                successCB.success(umListData);
                            }
                        });
                    }catch (final Exception e){
                        e.printStackTrace();
                        if(cursor!=null&&!cursor.isClosed()){
                            cursor.close();
                        }
                        closeDB();
                        error(handler,errorCB,"查询失败,"+e.getMessage());
                    }
                }
        });
    }

    public void addOne(final T obj, final DbSuccessCB<Integer> successCB,final DbErrorCB errorCB){
        List<T> list=new ArrayList<>();
        list.add(obj);
        addList(list,successCB,errorCB);
    }

    public void addList(final List<T> objectList, final DbSuccessCB<Integer> successCB,final DbErrorCB errorCB){
        if(objectList==null||objectList.size()==0){
            if(successCB!=null){
                successCB.success(0);
            }
            return;
        }
        final Handler handler=initHandler();
        getExecutor().execute(
            new Runnable() {
                @Override
                public void run() {
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
                        boolean isSuccess=true;
                        int successCount=0;
                        for(int i=0;i<objectList.size();i++){
                            T obj=objectList.get(i);
                            if(obj==null){
                                continue;
                            }
                            sqLiteDatabase.insert(tableInfo.getTableName(),null,tableInfo.getContentValues(obj));
                            successCount++;
                        }
                        if(isSuccess){
                            if(openTran){
                                sqLiteDatabase.setTransactionSuccessful();
                                sqLiteDatabase.endTransaction();
                            }
                            closeDB();
                            if(successCB==null){
                                return;
                            }
                            final int count=successCount;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    successCB.success(count);
                                }
                            });
                        }
                    }catch (final Exception e){
                        e.printStackTrace();
                        if(openTran){
                            sqLiteDatabase.endTransaction();
                        }
                        closeDB();
                        if(errorCB==null){
                            return;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                errorCB.error("插入数据失败," + e.getMessage());
                            }
                        });
                    }
                }
            }
        );
    }

    public void update(final T obj,final DbSuccessCB<Integer> successCB,final DbErrorCB errorCB){
        List<T> list=new ArrayList<>();
        list.add(obj);
        updateList(list,successCB,errorCB);
    }

    public void updateList(final List<T> obj,final DbSuccessCB<Integer> successCB,final DbErrorCB errorCB){
        if(obj==null||obj.size()==0){
            if(successCB!=null){
                successCB.success(0);
            }
            return;
        }
        final Handler handler=initHandler();
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase sqLiteDatabase= null;
                boolean openTran=true;
                if(obj!=null&&obj.size()==1){
                    openTran=false;
                }
                try {
                    sqLiteDatabase= openDB();
                    boolean isSuccess=true;
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
                    if(isSuccess){
                        if(openTran){
                            sqLiteDatabase.setTransactionSuccessful();
                            sqLiteDatabase.endTransaction();
                        }
                        closeDB();
                        final int count=successCount;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                successCB.success(count);
                            }
                        });
                    }
                }catch (final Exception e){
                    e.printStackTrace();
                    if(openTran){
                        sqLiteDatabase.endTransaction();
                    }
                    closeDB();
                    error(handler,errorCB,"更新数据失败," + e.getMessage());
                }
            }
        });
    }

    public void delete(final T obj, final DbSuccessCB<Integer> successCB,final DbErrorCB errorCB){
        List<T> list=new ArrayList<>();
        list.add(obj);
        deleteList(list,successCB,errorCB);
    }

    public void deleteList(final List<T> objectList, final DbSuccessCB<Integer> successCB,final DbErrorCB errorCB){
        if(objectList==null||objectList.size()==0){
            if(successCB!=null){
                successCB.success(0);
            }
            return;
        }
        final Handler handler=initHandler();
        getExecutor().execute(
                new Runnable() {
                    @Override
                    public void run() {
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
                            boolean isSuccess=true;
                            int successCount=0;
                            for(int i=0;i<objectList.size();i++){
                                BaseRecord obj=objectList.get(i);
                                if(obj==null){
                                    continue;
                                }
                                sqLiteDatabase.execSQL("DELETE FROM "+tableInfo.getTableName()+" WHERE "+BaseRecord.ID_NAME+"="+obj.getBaseId());
                                successCount++;
                            }
                            if(isSuccess){
                                if(openTran){
                                    sqLiteDatabase.setTransactionSuccessful();
                                    sqLiteDatabase.endTransaction();
                                }
                                closeDB();
                                if(successCB==null){
                                    return;
                                }
                                final int count=successCount;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        successCB.success(count);
                                    }
                                });
                            }
                        }catch (final Exception e){
                            e.printStackTrace();
                            if(openTran){
                                sqLiteDatabase.endTransaction();
                            }
                            closeDB();
                            error(handler,errorCB,"删除数据失败," + e.getMessage());
                        }
                    }
                }
        );
    }

    private void error(Handler handler,final DbErrorCB errorCB,final String msg){
        if(errorCB==null){
            return;
        }
        if(handler!=null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    errorCB.error(msg);
                }
            });
        }else{
            errorCB.error(msg);
        }
    }
}
