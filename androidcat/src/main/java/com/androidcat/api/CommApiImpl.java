package com.androidcat.api;

import android.app.Application;
import android.content.Context;

import com.androidcat.biz.bean.CardRecord;
import com.androidcat.biz.bean.WhiteList;
import com.androidcat.biz.consts.GConfig;
import com.androidcat.biz.database.JlPosDatabase;
import com.androidcat.biz.manager.CardRecordsManager;
import com.androidcat.biz.manager.CommManager;
import com.androidcat.biz.manager.WhiteListManager;
import com.androidcat.catlibs.log.LogFileTool;
import com.androidcat.catlibs.log.LogUtil;
import com.androidcat.biz.consts.PublicConsts;
import com.androidcat.catlibs.persistance.SharePreferenceUtil;
import com.androidcat.catlibs.persistance.SpKey;
import com.androidcat.catlibs.uncaught.ErrorReporter;
import com.androidcat.catlibs.utils.Utils;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.List;

/**
 * Created by androidcat on 2019/8/26.
 */

public class CommApiImpl implements CommApi{
    private static CommApiImpl sdkImpl;
    private static Context mContext;

    public static CommApiImpl getInstance(Context context){
        mContext=context;
        if(sdkImpl==null){
            sdkImpl=new CommApiImpl();
        }
        return sdkImpl;
    }

    @Override
    public void initApi(Context context) {
        //配置文件必须首先初始化，顺序不能错
        SharePreferenceUtil.init(context);
        //加载配置次之
        loadConfig();
        //日志初始化再次之
        LogFileTool.init(context);
        //接下来顺序无关紧要
        JlPosDatabase.getInstance(context);
        initImageLoader(context);
        // Activate the ErrorReporter
        ErrorReporter.getInstance().init((Application) context);
        PublicConsts.IMEI= Utils.getDeviceId(context);
        PublicConsts.DEVICE_NAME=Utils.getDeviceName(context);
    }

    @Override
    public int uploadCrash(String crash, CallBack callBack) {
        return new CommManager(mContext,callBack).uploadCrash(crash);
    }

    @Override
    public int getServerTime(CallBack callBack) {
        return new CommManager(mContext,callBack).getServerTime();
    }

    @Override
    public int checkUpdate(String username,String baseVersion, CallBack callBack) {
        return new CommManager(mContext,callBack).checkUpdate(username,baseVersion);
    }

    @Override
    public int getWhiteList(String time, CallBack callBack) {
        return new WhiteListManager(mContext,callBack).getWhiteList(time);
    }

    @Override
    public int getAdList(CallBack callBack) {
        return new CommManager(mContext,callBack).getAdList();
    }

    @Override
    public int uploadCardRecords(CallBack callBack) {
        return new CardRecordsManager(mContext,callBack).uploadRecords();
    }

    @Override
    public int uploadCardRecords(List<CardRecord> records,CallBack callBack) {
        return new CardRecordsManager(mContext,callBack).uploadRecords(records);
    }

    @Override
    public int uploadCardRecord(CardRecord record, CallBack callBack) {
        return new CardRecordsManager(mContext,callBack).uploadRecord(record);
    }

    @Override
    public int rollbackCardRecord(CardRecord record, CallBack callBack) {
        return new CardRecordsManager(mContext,callBack).rollbackRecord(record);
    }

    @Override
    public int registerJpush(String jpushId,CallBack callBack) {
        return new CommManager(mContext,callBack).bindJpush(jpushId);
    }

    @Override
    public List<CardRecord> getTodayCardRecord(String consumeType,int pageNo,int pageSize) {
        return JlPosDatabase.getInstance(mContext).getTodayRecords(consumeType,pageNo,pageSize);
    }

    @Override
    public int queryDeviceId(CallBack callBack) {
        return new CommManager(mContext,callBack).queryDeviceId();
    }

    @Override
    public int bindDeviceName(String deviceName,CallBack callBack) {
        return new CommManager(mContext,callBack).bindDeviceName(deviceName);
    }

    @Override
    public int setCanteenSite(String site, CallBack callBack) {
        return new CommManager(mContext,callBack).setCanteenSite(site);
    }

    @Override
    public int getShortcutInfo(CallBack callBack) {
        return new CommManager(mContext,callBack).getShortcutInfo();
    }

    @Override
    public int uploadLog(CallBack callBack) {
        return new CommManager(mContext,callBack).uploadLog();
    }

    @Override
    public int uploadDatabase(CallBack callBack) {
        return new CommManager(mContext,callBack).uploadDatabase();
    }

    @Override
    public long countWhiteList() {
        return JlPosDatabase.getInstance(mContext).countWhiteList();
    }

    @Override
    public long countTotalRecNum() {
        return JlPosDatabase.getInstance(mContext).countTotalRecNum();
    }

    @Override
    public long countCurJiciRecNum() {
        return JlPosDatabase.getInstance(mContext).countTodayJiciRecords();
    }

    @Override
    public long countCurJifeiRecNum() {
        return JlPosDatabase.getInstance(mContext).countTodayJifeiRecords();
    }

    @Override
    public long countUnUploadedRecNum() {
        return JlPosDatabase.getInstance(mContext).countUnUploadedRecNum();
    }

    @Override
    public long countToBeDeletedRecNum() {
        return JlPosDatabase.getInstance(mContext).countToBeDeletedRecNum();
    }

    @Override
    public long countRolledBackRecNum() {
        return JlPosDatabase.getInstance(mContext).countRolledBackRecNum();
    }

    @Override
    public void cleanRecords() {
        JlPosDatabase.getInstance(mContext).deleteYesterdayRecords();
    }

    @Override
    public double getTotalConsumedAmountOfStaff(String staffNo) {
        return JlPosDatabase.getInstance(mContext).getTotalConsumedAmountOfStaff(staffNo);
    }

    @Override
    public WhiteList getWhiteListByCardNo(String cardNo) {
        return JlPosDatabase.getInstance(mContext).getWhiteListByCardNo(cardNo);
    }

    @Override
    public long countFreeMealByMealType(String jobNo, String mealType) {
        return JlPosDatabase.getInstance(mContext).countFreeMealByMealType(jobNo, mealType);
    }

    @Override
    public long countFreeMeal(String jobNo) {
        return JlPosDatabase.getInstance(mContext).countFreeMeal(jobNo);
    }

    @Override
    public String getPasswordByCardNo(String cardNo) {
        return JlPosDatabase.getInstance(mContext).getPasswordByCardNo(cardNo);
    }

    @Override
    public void saveRecord(CardRecord record) {
        JlPosDatabase.getInstance(mContext).saveRecord(record);
    }

    @Override
    public long countRecordsByMealTypeAndConsumeType(String mealType, String consumeType) {
        return JlPosDatabase.getInstance(mContext).countRecordsByMealTypeAndConsumeType(mealType,consumeType);
    }

    @Override
    public long countCurJiciRecordsByStaffNo() {
        return JlPosDatabase.getInstance(mContext).countCurJiciRecordsByStaffNo();
    }

    @Override
    public long countTodayJiciRecords() {
        return JlPosDatabase.getInstance(mContext).countTodayJiciRecords();
    }

    @Override
    public void updateRecord(CardRecord cardRecord) {
        JlPosDatabase.getInstance(mContext).updateRecord(cardRecord);
    }

    private static void loadConfig() {
        GConfig config = GConfig.getConfig();
        //配置服务器环境
        config.comm.env = GConfig.ENV_PROD;
        //配置网络协议
        config.comm.protocol = GConfig.HTTPS;

        LogUtil.setDebug(config.comm.debug);
        LogUtil.setLogFile(config.comm.logFile);
        PublicConsts.SERVER_URL = GConfig.getDomain();
        PublicConsts.IMG_URL = GConfig.getImgUrl();
        PublicConsts.PIN = config.comm.pin;
        PublicConsts.IS_CHECK_TIME = SharePreferenceUtil.getBoolean(SpKey.KEY_IS_CHECK_TIME,true);
    }

    private void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you
        // may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)// 设置线程的优先级
                .memoryCacheSize(128)
                .denyCacheImageMultipleSizesInMemory()// 当同一个Uri获取不同大小的图片，缓存到内存时，只缓存一个。默认会缓存多个不同的大小的相同图片
                .discCacheFileNameGenerator(new Md5FileNameGenerator())// 设置缓存文件的名字
                .discCacheFileCount(100)// 缓存文件的最大个数
                .tasksProcessingOrder(QueueProcessingType.LIFO)// 设置图片下载和显示的工作队列排序
                .build();

        // Initialize ImageLoader with configuration
        ImageLoader.getInstance().init(config);
    }
}
