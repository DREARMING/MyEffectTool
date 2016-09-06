package com.mvp.myeffecttools.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mvp.myeffecttools.dao.DaoMaster;
import com.mvp.myeffecttools.dao.DaoSession;
import com.mvp.myeffecttools.dao.LockApp;
import com.mvp.myeffecttools.dao.LockAppDao;
import com.mvp.myeffecttools.interfaces.DaoListener;

import java.util.List;

/**
 * Created by 爱的LUICKY on 2016/8/30.
 */
public class DaoUtils {


    public static void insertLockApp(Context context,String packageName){
        SQLiteOpenHelper hp = new DaoMaster.DevOpenHelper(context,"MyDb.db",null);
        SQLiteDatabase db = hp.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);
        DaoSession session =  master.newSession();
        LockAppDao lockAppDao = session.getLockAppDao();
        LockApp item = new LockApp(packageName,false);
        lockAppDao.insert(item);
        db.close();
    }

    public static void deleteLockApp(Context context,String packageName){
        SQLiteOpenHelper hp = new DaoMaster.DevOpenHelper(context,"MyDb.db",null);
        SQLiteDatabase db = hp.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);
        DaoSession session =  master.newSession();
        LockAppDao lockAppDao = session.getLockAppDao();
        db.delete(lockAppDao.getTablename(), LockAppDao.Properties.PackageName.columnName+"=?",new String[]{packageName});
        db.close();
    }

    public static List<LockApp> getLockApps(Context context){
        SQLiteOpenHelper hp = new DaoMaster.DevOpenHelper(context,"MyDb.db",null);
        SQLiteDatabase db = hp.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);
        DaoSession session =  master.newSession();
        LockAppDao lockAppDao = session.getLockAppDao();
        List<LockApp> list = lockAppDao.loadAll();
        db.close();
        return list;
    }


}
