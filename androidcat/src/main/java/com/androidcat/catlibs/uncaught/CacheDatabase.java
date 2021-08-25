package com.androidcat.catlibs.uncaught;

import android.content.Context;

import com.androidcat.catlibs.log.LogUtil;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.List;


/**
 *
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
public class CacheDatabase {
	// log tag
	protected static final String TAG = "CacheDatabase";
	public static final String DATABASE_FILE = "CacheDatabase.db";

	/**
	 * 该常量字段
	 */
	private static int DATABASE_VERSION = 3;

	private static CacheDatabase mInstance = null;
	private static DbUtils mDbUtils;

	private static Context mContext;

	private CacheDatabase() {
		// Singleton only, use getInstance()
	}

	public static synchronized CacheDatabase getInstance(Context context) {
		mContext = context;
		if (mInstance == null){
			mInstance = new CacheDatabase();
			try{
				DbUtils.DaoConfig config = new DbUtils.DaoConfig(mContext);
				config.setDbName(DATABASE_FILE);
				config.setDbVersion(DATABASE_VERSION);
				config.setDbUpgradeListener(new DbUtils.DbUpgradeListener() {
					@Override
					public void onUpgrade(DbUtils dbUtils, int oldVer, int newVer) {
						upgradeDatabase(dbUtils,oldVer,newVer);
					}
				});
				mDbUtils = DbUtils.create(config);
				mDbUtils.configAllowTransaction(true);
			}catch (Exception e){
				e.printStackTrace();
				LogUtil.e(TAG,e.getMessage(),e);
			}
		}
		return mInstance;
	}

	private static void upgradeDatabase(DbUtils dbUtils, int oldVer, int newVer) {
		LogUtil.e(TAG, "数据库版本不一致，进入升级数据库流程");
		LogUtil.e(TAG, "当前数据库版本：" + oldVer + "  ----需升级到：" + newVer);
		try{
			dbUtils.createTableIfNotExist(CrashEntity.class);
		}catch (Exception e){

		}
	}

	public boolean saveCrash(CrashEntity crash){
		if (mDbUtils == null){
			return false;
		}
		try {
			mDbUtils.saveOrUpdate(crash);
			return true;
		} catch (DbException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<CrashEntity> getCrashList(){
		if (mDbUtils == null){
			return null;
		}
		try {
			List<CrashEntity> list = mDbUtils.findAll(CrashEntity.class);
			return list;
		} catch (DbException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean hasUnuploadedCrash(){
		if (mDbUtils == null){
			return true;
		}
		try {
			long count = mDbUtils.count(Selector
					.from(CrashEntity.class)
					.where("state","=",0));
			return count > 0;
		} catch (DbException e) {
			e.printStackTrace();
			return true;
		}
	}

	public boolean delete(CrashEntity crash){
		if (mDbUtils == null){
			return true;
		}
		try {
			mDbUtils.delete(crash);
		} catch (DbException e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean deleteCrashList(List<CrashEntity> crashList){
		if (crashList == null || crashList.size() == 0){
			return true;
		}
		for (CrashEntity crash : crashList){
            delete(crash);
        }
		return true;
	}

	public List<CrashEntity> getUnuploadedCrash(){
		if (mDbUtils == null){
			return null;
		}
		try {
			return mDbUtils.findAll(Selector
					.from(CrashEntity.class)
					.where("state","=",0));
		} catch (DbException e) {
			e.printStackTrace();
			return null;
		}
	}

}
