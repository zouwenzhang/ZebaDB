package com.zeba.db.sl;

import android.os.Handler;
import android.os.Looper;

import com.zwz.zeba.orm.base.BaseRepository;
import com.zwz.zeba.orm.base.curd.Query;
import com.zwz.zeba.orm.base.curd.Update;
import com.zwz.zeba.orm.base.curd.Where;

import java.util.List;

public class DbRepository<T> extends BaseRepository<T> {
    private Handler resultHandler=new Handler(Looper.getMainLooper());
    public DbRepository() throws Exception {

    }

    public DbRepository(Handler handler) throws Exception {
        this.resultHandler=handler;
    }

    public void findList(Object[] w,DbCallback<List<T>> suc,DbCallback<Exception> err){
        execAsync(()->{
            try {
                List<T> list=findByList(w);
                resultHandler.post(()->suc.onResult(list));
            } catch (Exception e) {
                e.printStackTrace();
                onErr(e,err);
            }
        });
    }

    public void findList(Query query,DbCallback<List<T>> suc,DbCallback<Exception> err){
        execAsync(()->{
            try {
                List<T> list=findByList(query);
                resultHandler.post(()->suc.onResult(list));
            } catch (Exception e) {
                e.printStackTrace();
                onErr(e,err);
            }
        });
    }

    public void findOne(Where where,DbCallback<T> suc,DbCallback<Exception> err){
        execAsync(()->{
            try {
                T t=findOne(where);
                resultHandler.post(()->suc.onResult(t));
            } catch (Exception e) {
                e.printStackTrace();
                onErr(e,err);
            }
        });
    }

    public void findOne(Object[] w,DbCallback<T> suc,DbCallback<Exception> err){
        execAsync(()->{
            try {
                T t=findOne(w);
                resultHandler.post(()->suc.onResult(t));
            } catch (Exception e) {
                e.printStackTrace();
                onErr(e,err);
            }
        });
    }

    public void add(T t,DbCallback<T> suc,DbCallback<Exception> err){
        execAsync(()->{
            try {
                T tr=addOne(t);
                resultHandler.post(()->suc.onResult(tr));
            } catch (Exception e) {
                e.printStackTrace();
                onErr(e,err);
            }
        });
    }

    public void addList(List<T> list,DbCallback<List<T>> suc,DbCallback<Exception> err){
        execAsync(()->{
            try {
                addList(list);
                resultHandler.post(()->suc.onResult(list));
            } catch (Exception e) {
                e.printStackTrace();
                onErr(e,err);
            }
        });
    }

    public void update(T t,DbCallback<T> suc,DbCallback<Exception> err){
        execAsync(()->{
            try {
                update(t);
                resultHandler.post(()->suc.onResult(t));
            } catch (Exception e) {
                e.printStackTrace();
                onErr(e,err);
            }
        });
    }

    public void updateByWhere(Update update, DbCallback<T> suc, DbCallback<Exception> err){
        execAsync(()->{
            try {
                updateByWhere(update);
                resultHandler.post(()->suc.onResult(null));
            } catch (Exception e) {
                e.printStackTrace();
                onErr(e,err);
            }
        });
    }

    public void updateList(List<T> list,DbCallback<List<T>> suc, DbCallback<Exception> err){
        execAsync(()->{
            try {
                updateList(list);
                resultHandler.post(()->suc.onResult(list));
            } catch (Exception e) {
                e.printStackTrace();
                onErr(e,err);
            }
        });
    }

    public void delete(T t,DbCallback<T> suc, DbCallback<Exception> err){
        execAsync(()->{
            try {
                delete(t);
                resultHandler.post(()->suc.onResult(t));
            } catch (Exception e) {
                e.printStackTrace();
                onErr(e,err);
            }
        });
    }

    public void deleteByWhere(Object[] where,DbCallback<T> suc, DbCallback<Exception> err){
        execAsync(()->{
            try {
                deleteByWhere(where);
                resultHandler.post(()->suc.onResult(null));
            } catch (Exception e) {
                e.printStackTrace();
                onErr(e,err);
            }
        });
    }

    public void deleteByWhere(Where where,DbCallback<T> suc, DbCallback<Exception> err){
        execAsync(()->{
            try {
                deleteByWhere(where);
                resultHandler.post(()->suc.onResult(null));
            } catch (Exception e) {
                e.printStackTrace();
                onErr(e,err);
            }
        });
    }

    public void deleteList(List<T> list,DbCallback<List<T>> suc, DbCallback<Exception> err){
        execAsync(()->{
            try {
                deleteList(list);
                resultHandler.post(()->suc.onResult(list));
            } catch (Exception e) {
                e.printStackTrace();
                onErr(e,err);
            }
        });
    }

    private void execAsync(Runnable runnable){
        new Thread(runnable).start();
    }

    private void onErr(Exception e,DbCallback<Exception> err){
        if(err!=null){
            resultHandler.post(()->{
                err.onResult(e);
            });
        }
    }

}
