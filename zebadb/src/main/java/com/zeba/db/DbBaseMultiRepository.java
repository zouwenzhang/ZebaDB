package com.zeba.db;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class DbBaseMultiRepository<T extends BaseRecord> extends DbBaseRepository<T>{
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void postRunnable(Runnable runnable) {
        executorService.execute(runnable);
    }

    @Override
    protected void postCloseDB() {
        closeDB();
    }
}
