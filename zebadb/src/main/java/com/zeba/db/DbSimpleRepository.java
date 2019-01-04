package com.zeba.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbSimpleRepository<T extends BaseRecord> extends DbBaseSimpleRepository<T> {

    private SQLiteOpenHelper sqLiteOpenHelper;

    public void init(Context context,String dbName,int dbVersion){
        sqLiteOpenHelper=new SQLiteOpenHelper(context,dbName,null,dbVersion) {

            @Override
            public void onConfigure(SQLiteDatabase db) {
                super.onConfigure(db);

            }

            @Override
            public void onCreate(SQLiteDatabase db) {

            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            }
        };
        initDB();
    }

    @Override
    public SQLiteOpenHelper onCreateDB() {
        return sqLiteOpenHelper;
    }
}
