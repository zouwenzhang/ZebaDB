package com.zeba.db;

import android.os.Handler;
import android.os.HandlerThread;

public abstract class DbBaseSimpleRepository<T extends BaseRecord> extends DbBaseRepository<T>{
    private HandlerThread myHandlerThread ;
    private Handler myHandler ;

    public DbBaseSimpleRepository(){
        super();
        myHandlerThread = new HandlerThread( getTableInfo().getTableName()+"-handler-thread");
        myHandlerThread.start();
        myHandler=new Handler(myHandlerThread.getLooper());
    }

    @Override
    protected void postCloseDB() {
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                closeDB();
            }
        });
    }

    @Override
    public void postRunnable(Runnable runnable) {
        myHandler.post(runnable);
    }
}
