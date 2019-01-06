package com.zeba.db.listener;

import com.zeba.db.DbListData;

public class DbQueryResult <T>{
    private boolean isSuccess=false;
    private DbListData<T> data;
    private String msg;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public DbListData<T> getData() {
        return data;
    }

    public void setData(DbListData<T> data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
