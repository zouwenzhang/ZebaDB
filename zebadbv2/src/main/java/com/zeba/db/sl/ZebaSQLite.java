package com.zeba.db.sl;

import android.content.Context;

import com.zwz.zeba.orm.base.BaseConnection;
import com.zwz.zeba.orm.base.BaseDataSource;
import com.zwz.zeba.orm.base.ZebaORM;

import java.sql.SQLException;

public class ZebaSQLite extends BaseDataSource {

    private static String dbPath;

    public static void init(Context context,String name){
        dbPath=context.getDatabasePath(name).getAbsolutePath();
        ZebaORM.setDataSource(new ZebaSQLite());
    }

    @Override
    public BaseConnection getConnection(String requestId) throws SQLException {
        return new SqliteConnection(dbPath);
    }
}
