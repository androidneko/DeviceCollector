package com.androidcat.dc.demo.db;

import android.content.Context;
import android.util.Log;

import com.androidcat.dc.demo.db.bean.Operator;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * **********************************************************<br>
 * 模块功能: 封装了对sqlite数据库的管理，并提供里对各个表操作的业务方法<br>
 * 作 者: 薛龙<br>
 * 开发日期：2013-8-7 下午13:44:33
 * 单 位：武汉天喻信息 研发中心
 * 修改日期：<br>
 * 修改人：<br>
 * 修改说明：<br>
 * *********************************************************<br>
 */
public class TyDcDatabase {
    // log tag
    protected static final String TAG = "TyDcDatabase";
    public static final String DATABASE_FILE = "TyDcPosDatabase.db";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    /**
     * 该常量字段
     */
    private static int DATABASE_VERSION = 3;

    private static TyDcDatabase mInstance = null;
    private static DbUtils mDbUtils;

    private static Context mContext;

    private TyDcDatabase() {
        // Singleton only, use getInstance()
    }

    public static synchronized TyDcDatabase getInstance(Context context) {
        mContext = context;
        if (mInstance == null) {
            mInstance = new TyDcDatabase();
            DbUtils.DaoConfig config = new DbUtils.DaoConfig(mContext);

            config.setDbName(DATABASE_FILE);
            config.setDbVersion(DATABASE_VERSION);
            config.setDbUpgradeListener(new DbUtils.DbUpgradeListener() {
                @Override
                public void onUpgrade(DbUtils dbUtils, int oldVer, int newVer) {
                    upgradeDatabase(dbUtils, oldVer, newVer);
                }
            });
            mDbUtils = DbUtils.create(config);
            mDbUtils.configAllowTransaction(true);
            mDbUtils.configDebug(true);
            createDefTables();
        }
        return mInstance;
    }

    private static void createDefTables(){
        try {
            mDbUtils.createTableIfNotExist(Operator.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private static void upgradeDatabase(DbUtils dbUtils, int oldVer, int newVer) {
        Log.e(TAG, "数据库版本不一致，进入升级数据库流程");
        Log.e(TAG, "当前数据库版本：" + oldVer + "  ----需升级到：" + newVer);
        try {
            // TODO: 2021/8/3
        } catch (Exception e) {

        }
    }

    public boolean updateOperator(Operator operator) {
        if (mDbUtils == null) {
            return false;
        }
        try {
            mDbUtils.saveOrUpdate(operator);
            return true;
        } catch (DbException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Operator> getOperators() {
        if (mDbUtils == null) {
            return new ArrayList<>();
        }
        try {
            return mDbUtils.findAll(Operator.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public boolean deleteAll(List<Operator> list) {
        if (mDbUtils == null) {
            return false;
        }
        try {
            mDbUtils.deleteAll(list);
            return true;
        } catch (DbException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(Operator operator) {
        if (mDbUtils == null) {
            return false;
        }
        try {
            mDbUtils.delete(operator);
            return true;
        } catch (DbException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean ifExist(String vcuid) {
        if (mDbUtils == null) {
            return false;
        }
        try {
            return mDbUtils.findById(Operator.class, vcuid) != null;
        } catch (DbException e) {
            e.printStackTrace();
            return false;
        }
    }

    public long countTotalRecNum() {
        if (mDbUtils == null) {
            return 0;
        }
        try {
            return mDbUtils.count(Operator.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //保存记录
    public void saveRecord(Operator operator) {
        if (mDbUtils == null) {
            return;
        }
        try {
            mDbUtils.save(operator);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

}
