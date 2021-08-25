package com.androidcat.biz.database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.androidcat.biz.bean.CardRecord;
import com.androidcat.biz.bean.WhiteList;
import com.androidcat.biz.consts.GConfig;
import com.androidcat.catlibs.log.LogUtil;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.DbModelSelector;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.db.table.DbModel;
import com.lidroid.xutils.exception.DbException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


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
public class JlPosDatabase {
    // log tag
    protected static final String TAG = "JlPosDatabase";
    public static final String DATABASE_FILE = "JlPosDatabase.db";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    /**
     * 该常量字段
     */
    private static int DATABASE_VERSION = 2;

    private static JlPosDatabase mInstance = null;
    private static DbUtils mDbUtils;

    private static Context mContext;

    private JlPosDatabase() {
        // Singleton only, use getInstance()
    }

    public static synchronized JlPosDatabase getInstance(Context context) {
        mContext = context;
        if (mInstance == null) {
            mInstance = new JlPosDatabase();
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
            mDbUtils.configDebug(GConfig.getConfig().comm.debug);
            createDefTables();
        }
        return mInstance;
    }

    private static void createDefTables(){
        try {
            mDbUtils.createTableIfNotExist(CardRecord.class);
            mDbUtils.createTableIfNotExist(WhiteList.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private static void upgradeDatabase(DbUtils dbUtils, int oldVer, int newVer) {
        Log.e(TAG, "数据库版本不一致，进入升级数据库流程");
        Log.e(TAG, "当前数据库版本：" + oldVer + "  ----需升级到：" + newVer);
        try {

            //数据库版本为2时，因表结构发生变化，重新启用数据库升级策略
            if (newVer == 2){
				//本次数据库升级需修改表字段，需先删除就task表
				dbUtils.dropTable(WhiteList.class);
				LogUtil.e(TAG, "----删除旧版WhiteList表，新增modifyTime关联字段----");
                dbUtils.createTableIfNotExist(WhiteList.class);
			}
        } catch (Exception e) {

        }
    }

    public boolean updateCard(CardRecord cardRecord) {
        if (mDbUtils == null) {
            return false;
        }
        try {
            mDbUtils.saveOrUpdate(cardRecord);
            return true;
        } catch (DbException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<CardRecord> getCards() {
        if (mDbUtils == null) {
            return null;
        }
        try {
            return mDbUtils.findAll(CardRecord.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<WhiteList> getWhiteList() {
        if (mDbUtils == null) {
            return null;
        }
        try {
            return mDbUtils.findAll(WhiteList.class);
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
        return null;
    }

    public long countWhiteList() {
        if (mDbUtils == null) {
            return 0;
        }
        try {
            return mDbUtils.count(WhiteList.class);
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
        return 0;
    }

    public boolean deleteAll(List<CardRecord> cardRecords) {
        if (mDbUtils == null) {
            return false;
        }
        try {
            mDbUtils.deleteAll(cardRecords);
            return true;
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
            return false;
        }
    }

    public boolean updateAll(List<CardRecord> cardRecords) {
        if (mDbUtils == null) {
            return false;
        }
        try {
            mDbUtils.updateAll(cardRecords, "state");
            return true;
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
            return false;
        }
    }

    public boolean updateWhiteList(List<WhiteList> whiteLists) {
        if (mDbUtils == null) {
            return false;
        }
        try {
            if (whiteLists != null && whiteLists.size() > 0){
                Collections.sort(whiteLists);
                mDbUtils.replaceAll(whiteLists);
            }
            return true;
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
            return false;
        }
    }

    public void cleanUpWhiteList(){
        if (mDbUtils == null) {
            return ;
        }
        try {
            mDbUtils.delete(WhiteList.class, WhereBuilder.b("addOrDelete", "=", WhiteList.DELETE));
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
    }

    public boolean saveWhiteList(List<WhiteList> whiteLists) {
        if (mDbUtils == null) {
            return false;
        }
        try {
            mDbUtils.saveAll(whiteLists);
            return true;
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
            return false;
        }
    }

    public boolean replaceWhiteList(List<WhiteList> whiteLists) {
        if (mDbUtils == null) {
            return false;
        }
        try {
            mDbUtils.replaceAll(whiteLists);
            return true;
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
            return false;
        }
    }

    public boolean delete(CardRecord cardRecord) {
        if (mDbUtils == null) {
            return false;
        }
        try {
            mDbUtils.delete(cardRecord);
            return true;
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
            return false;
        }
    }

    public boolean deleteAllWhite() {
        if (mDbUtils == null) {
            return false;
        }
        try {
            mDbUtils.deleteAll(WhiteList.class);
            return true;
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
            return false;
        }
    }

    public boolean ifExist(String vcuid) {
        if (mDbUtils == null) {
            return false;
        }
        try {
            return mDbUtils.findById(CardRecord.class, vcuid) != null;
        } catch (DbException e) {
            e.printStackTrace();
            return false;
        }
    }

    //根据卡号获取白名单
    public WhiteList getWhiteListByCardNo(String cardNo) {
        if (mDbUtils == null) {
            return null;
        }
        try {
            return mDbUtils.findById(WhiteList.class, cardNo);
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
            return null;
        }
    }

    //根据卡号从白名单获取员工号
    public String getStaffNoByCardNo(String cardNo) {
        if (mDbUtils == null) {
            return "";
        }
        try {
            WhiteList whiteList = mDbUtils.findById(WhiteList.class, cardNo);
            if (whiteList != null) {
                return whiteList.staffNo;
            }
            return "";
        } catch (DbException e) {
            e.printStackTrace();
            return "";
        }
    }

    public List<String> getCardNosByStaffNo(String staffNo){
        if (mDbUtils == null) {
            return null;
        }
        try {
            List<WhiteList> whiteLists = mDbUtils.findAll(Selector.from(WhiteList.class).where("staffNo","=",staffNo));
            if (whiteLists != null && whiteLists.size() > 0) {
                List<String> cardNos = new ArrayList<>();
                for (WhiteList whiteList : whiteLists){
                    cardNos.add(whiteList.cardNo);
                }
                return cardNos;
            }
            return null;
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
            return null;
        }
    }

    //根据卡号从白名单获取员工密码
    public String getPasswordByCardNo(String cardNo) {
        if (mDbUtils == null) {
            return "";
        }
        try {
            WhiteList whiteList = mDbUtils.findById(WhiteList.class, cardNo);
            if (whiteList != null) {
                return whiteList.password;
            }
            return "";
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
            return "";
        }
    }

    public long countTotalRecNum() {
        if (mDbUtils == null) {
            return 0;
        }
        try {
            return mDbUtils.count(CardRecord.class);
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
        return 0;
    }

    public long countRecordsByMealTypeAndConsumeType(String mealType, String consumeType){
        if (mDbUtils == null) {
            return 0;
        }
        try {
            long zero = getTodayZero();
            long end = zero + (24 * 60 * 60 * 1000);
            return mDbUtils.count(
                    Selector.from(CardRecord.class)
                            .where("transTimeMillions", "BETWEEN", new long[]{zero,end})
                            .and("mealType","=",mealType)
                            .and("consumeType","=",consumeType)
                            .and("state", "<>", CardRecord.ROLLED_BACK));
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
        return 0;
    }

    //获取当日某个员工号下刷卡次数
    public long countFreeMeal(String staffNo) {
        if (mDbUtils == null) {
            return 0;
        }
        long zero = getTodayZero();
        long end = zero + (24 * 60 * 60 * 1000);
        try {
            long count = mDbUtils.count(Selector
                    .from(CardRecord.class)
                    .where("staffNo", "=", staffNo)
                    .and("transTimeMillions", "BETWEEN", new long[]{zero,end})
                    .and("consumeType", "=", CardRecord.CONSUME_TYPE_JICI)
                    .and("state", "!=", CardRecord.ROLLED_BACK));
            return count;
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
            return 0;
        }
    }

    //获取当日某个卡号下某个餐别刷卡次数
    public long countFreeMealByMealType(String staffNo, String mealType) {
        if (mDbUtils == null) {
            return 0;
        }
        try {
            long zero = getTodayZero();
            long end = zero + (24 * 60 * 60 * 1000);
            long count = mDbUtils.count(Selector
                    .from(CardRecord.class)
                    .where("staffNo", "=", staffNo)
                    .and("mealType", "=", mealType)
                    .and("transTimeMillions", "BETWEEN", new long[]{zero,end})
                    .and("state", "<>", CardRecord.ROLLED_BACK));
            return count;
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
            return 0;
        }
    }

    //获取某张卡当日以消费金额综合
    public double getTotalConsumedAmount(String cardNo) {
        if (mDbUtils == null) {
            return 0;
        }
        double total = 0;
        Cursor cursor = null;
        long zero = getTodayZero();
        long end = zero + (24 * 60 * 60 * 1000);
        String sql = "SELECT transMoney from CardRecord " +
                " WHERE cardNo = '" + cardNo +
                "' AND state <> " + CardRecord.ROLLED_BACK +
                " AND transTimeMillions BETWEEN " + zero + " and " + end;
        try {
            cursor = mDbUtils.execQuery(sql);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    double amount = cursor.getDouble(0);
                    total += amount;
                } while (cursor.moveToNext());
            }
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return total;
    }

    //获取某人当日以消费金额综合
    public double getTotalConsumedAmountOfStaff(String staffNo){
        if (mDbUtils == null) {
            return 0;
        }
        double total = 0;
        Cursor cursor = null;
        long zero = getTodayZero();
        long end = zero + (24 * 60 * 60 * 1000);
        List<String> cardNos = getCardNosByStaffNo(staffNo);
        if (cardNos == null || cardNos.size() == 0){
            return 0;
        }
        String where = "";
        if (cardNos != null && cardNos.size() > 0){
            StringBuilder stringBuilder = new StringBuilder();
            for (String cardNo : cardNos){
                stringBuilder.append("'");
                stringBuilder.append(cardNo);
                stringBuilder.append("',");
            }
            where = stringBuilder.toString();
            where = where.substring(0,where.length()-1);
        }

        String sql = "SELECT transMoney from CardRecord" +
                " WHERE cardNo in (" + where + ")"+
                " AND state <> " + CardRecord.ROLLED_BACK +
                " AND transTimeMillions BETWEEN " + zero + " and " + end;
        LogUtil.d(TAG,sql);
        try {
            cursor = mDbUtils.execQuery(sql);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    double amount = cursor.getDouble(0);
                    total += amount;
                } while (cursor.moveToNext());
            }
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return total;
    }

    //获取当日以消费金额综合
    public double getTotalConsumedAmount() {
        if (mDbUtils == null) {
            return 0;
        }
        double total = 0;
        Cursor cursor = null;
        long zero = getTodayZero();
        long end = zero + (24 * 60 * 60 * 1000);
        String sql = "SELECT transMoney from CardRecord " +
                " WHERE transTimeMillions BETWEEN " + zero + " and " + end +
                " AND state <> " + CardRecord.ROLLED_BACK;
        try {
            cursor = mDbUtils.execQuery(sql);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    double amount = cursor.getDouble(0);
                    total += amount;
                } while (cursor.moveToNext());
            }
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return total;
    }

    //删除昨天待上传记录
    public void deleteYesterdayRecords() {
        if (mDbUtils == null) {
            return;
        }
        try {
            LogUtil.d(TAG,"----删除历史已上传了的记录，确保数据库数据干净----");
            long zero = getTodayZero();
            mDbUtils.delete(CardRecord.class,
                    WhereBuilder.b("transTimeMillions", "<", zero)
                    .and("state", "=", CardRecord.TO_BE_DELETED));
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
    }

    //获取待上传记录，只要不是TO_BE_DELETED，都是没上传或上传失败的
    public List<CardRecord> getTobeUploadedRecords() {
        if (mDbUtils == null) {
            return null;
        }
        try {
            return mDbUtils.findAll(Selector
                    .from(CardRecord.class)
                    .where("state", "<>", CardRecord.TO_BE_DELETED));
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateRecord(CardRecord cardRecord) {
        if (mDbUtils == null) {
            return;
        }
        try {
            mDbUtils.update(cardRecord);
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
    }

    //批量删除传入的记录
    public void deleteRecords(List<CardRecord> records) {
        if (mDbUtils == null) {
            return;
        }
        try {
            mDbUtils.deleteAll(records);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    //按卡号获取记录
    public CardRecord getCardRecord(CardRecord record) {
        if (mDbUtils == null) {
            return null;
        }
        try {
            return mDbUtils.findFirst(
                    Selector.from(CardRecord.class)
                            .where("transTimeMillions", "=", record.transTimeMillions)
                            .and("cardNo", "=", record.cardNo)
                            .and("transMoney", "=", record.transMoney)
                            .and("mealType", "=", record.mealType));
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
            return null;
        }
    }

    //分页查询，默认第一页传pageNo=0
    public List<CardRecord> getTodayRecords(String consumeType, int pageNo, int pageSize) {
        if (mDbUtils == null) {
            return null;
        }
        try {
            long zero = getTodayZero();
            long end = zero + (24 * 60 * 60 * 1000);
            List<CardRecord> ret = mDbUtils.findAll(Selector
                    .from(CardRecord.class)
                    .where("transTimeMillions", "BETWEEN", new long[]{zero,end})
                    .and("consumeType", "=", consumeType)
                    .and("state", "<>", CardRecord.ROLLED_BACK)
                    .limit(pageSize)
                    .offset(pageNo*pageSize)
                    .orderBy("transTimeMillions",true));
            return ret;
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
        return null;
    }

    //查询当天计费记录数,即刷卡次数
    public long countTodayJifeiRecords() {
        if (mDbUtils == null) {
            return 0;
        }
        try {
            long zero = getTodayZero();
            long end = zero + (24 * 60 * 60 * 1000);
            return mDbUtils.count(Selector
                    .from(CardRecord.class)
                    .where("transTimeMillions", "BETWEEN", new long[]{zero,end})
                    .and("consumeType", "=", CardRecord.CONSUME_TYPE_JIFEI)
                    .and("state", "<>", CardRecord.ROLLED_BACK));
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
        return 0;
    }

    //查询当天计次记录数,即刷卡次数
    public long countTodayJiciRecords() {
        if (mDbUtils == null) {
            return 0;
        }
        try {
            long zero = getTodayZero();
            long end = zero + (24 * 60 * 60 * 1000);
            return mDbUtils.count(Selector
                    .from(CardRecord.class)
                    .where("transTimeMillions", "BETWEEN", new long[]{zero,end})
                    .and("consumeType", "=", CardRecord.CONSUME_TYPE_JICI)
                    .and("state", "<>", CardRecord.ROLLED_BACK));
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
        return 0;
    }

    //查询未上传刷卡次数
    public long countUnUploadedRecNum() {
        if (mDbUtils == null) {
            return 0;
        }
        try {
            return mDbUtils.count(Selector
                    .from(CardRecord.class)
                    .where("state", "in", new int[]{CardRecord.NEW_ADDED,CardRecord.ROLLED_BACK}));
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
        return 0;
    }

    //查询待删除记录数
    public long countToBeDeletedRecNum() {
        if (mDbUtils == null) {
            return 0;
        }
        try {
            return mDbUtils.count(Selector
                    .from(CardRecord.class)
                    .where("state", "=", CardRecord.TO_BE_DELETED));
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
        return 0;
    }

    //查询已撤销未上传的记录数
    public long countRolledBackRecNum() {
        if (mDbUtils == null) {
            return 0;
        }
        try {
            return mDbUtils.count(Selector
                    .from(CardRecord.class)
                    .where("state", "=", CardRecord.ROLLED_BACK));
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
        return 0;
    }

    //查询当天计次消费人数
    public long countCurJiFeiRecordsByStaffNo() {
        if (mDbUtils == null) {
            return 0;
        }
        try {
            long zero = getTodayZero();
            long end = zero + (24 * 60 * 60 * 1000);
            DbModelSelector dmSelector = Selector
                    .from(CardRecord.class)
                    .where("transTimeMillions", "BETWEEN", new long[]{zero,end})
                    .and("consumeType", "=", CardRecord.CONSUME_TYPE_JIFEI)
                    .and("state", "<>", CardRecord.ROLLED_BACK)
                    .select(new String[]{"count( distinct staffNo ) as count"});
            DbModel dbModel = mDbUtils.findDbModelFirst(dmSelector);
            if (dbModel != null) {
                return dbModel.getLong("count");
            }
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
        return 0;
    }

    //查询当天计次消费人数
    public long countCurJiciRecordsByStaffNo() {
        if (mDbUtils == null) {
            return 0;
        }
        try {
            long zero = getTodayZero();
            long end = zero + (24 * 60 * 60 * 1000);
            DbModelSelector dmSelector = Selector
                    .from(CardRecord.class)
                    .where("transTimeMillions", "BETWEEN", new long[]{zero,end})
                    .and("consumeType", "=", CardRecord.CONSUME_TYPE_JICI)
                    .and("state", "<>", CardRecord.ROLLED_BACK)
                    .select(new String[]{"count( distinct staffNo ) as count"});
            DbModel dbModel = mDbUtils.findDbModelFirst(dmSelector);
            if (dbModel != null) {
                return dbModel.getLong("count");
            }
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
        return 0;
    }

    //保存记录
    public void saveRecord(CardRecord record) {
        if (mDbUtils == null) {
            return;
        }
        try {
            mDbUtils.save(record);
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
    }

    //保存记录
    public void saveRecords(List<CardRecord> records) {
        if (mDbUtils == null) {
            return;
        }
        try {
            mDbUtils.replaceAll(records);
        } catch (DbException e) {
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }
    }

    //删除记录
    public void deleRecord(CardRecord record) {
        if (mDbUtils == null) {
            return;
        }
        try {
            mDbUtils.delete(record);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private long getTodayZero(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND,0);
        long zero =calendar.getTimeInMillis();
        return zero;
    }
}
