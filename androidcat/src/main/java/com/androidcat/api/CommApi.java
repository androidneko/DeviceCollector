package com.androidcat.api;

import android.content.Context;

import com.androidcat.biz.bean.CardRecord;
import com.androidcat.biz.bean.WhiteList;
import com.androidcat.catlibs.net.http.response.BaseResponse;

import java.util.List;

/**
 * Created by androidcat on 2019/8/26.
 */

public interface CommApi {

    void initApi(Context context);

    /**
     * 上传crash
     * @param crash crash info
     * @param callBack
     * @return
     */
    int uploadCrash(String crash,CallBack callBack);

    /**
     * 获取白名单
     * @param callBack
     * @return
     */
    int getServerTime(CallBack callBack);

    /**
     * 检查更新
     * @param userName
     * @param baseVersion
     * @param callBack
     * @return
     */
    int checkUpdate(String userName,String baseVersion,CallBack callBack);

    /**
     * 获取白名单
     * @param time 上次最后一条数据时间
     * @param callBack
     * @return
     */
    int getWhiteList(String time,CallBack callBack);

    /**
     * 获取广告列表
     * @param callBack
     * @return
     */
    int getAdList(CallBack callBack);

    /**
     * 批量上传刷卡记录
     * @param callBack
     * @return
     */
    int uploadCardRecords(CallBack callBack);

    /**
     * 批量上传刷卡记录
     * @param callBack
     * @return
     */
    int uploadCardRecords(List<CardRecord> records,CallBack callBack);

    /**
     * 上传刷卡记录
     * @param record 记录
     * @param callBack
     * @return
     */
    int uploadCardRecord(CardRecord record,CallBack callBack);

    /**
     * 上传撤销刷卡记录
     * @param record 记录
     * @param callBack
     * @return
     */
    int rollbackCardRecord(CardRecord record,CallBack callBack);

    /**
     * 注册Jpush
     * @param jpushId
     * @param callBack
     * @return
     */
    int registerJpush(String jpushId,CallBack callBack);

    /**
     * @param pageNo 页码
     * @param pageSize 每页记录数
     * 获取当天刷卡记录,分页查询
     * @return List<CardRecord>
     */
    List<CardRecord> getTodayCardRecord(String consumeType,int pageNo,int pageSize);

    /**
     * 查询设备信息
     * @param callBack
     * @return
     */
    int queryDeviceId(CallBack callBack);

    /**
     * 绑定设备名称
     * @param callBack
     * @return
     */
    int bindDeviceName(String deviceName,CallBack callBack);

    /**
     * 绑定餐厅
     * @param callBack
     * @return
     */
    int setCanteenSite(String site, CallBack callBack);

    /**
     * 获取快捷消费列表
     * @param callBack
     * @return
     */
    int getShortcutInfo(CallBack callBack);

    /**
     * 上传日志
     * @param callBack
     * @return
     */
    int uploadLog(CallBack callBack);

    /**
     * 上传database
     * @param callBack
     * @return
     */
    int uploadDatabase(CallBack callBack);

    ////////////////////////////////////////////数据库接口开始/////////////////////////////////////////////
    //获取白名单个数
    long countWhiteList();

    //获取当天刷卡记录数
    long countTotalRecNum();

    //获取当天计次刷卡记录数
    long countCurJiciRecNum();

    //获取当天计费刷卡记录数
    long countCurJifeiRecNum();

    //获取未上传刷卡记录数
    long countUnUploadedRecNum();

    //获取待删除刷卡记录数
    long countToBeDeletedRecNum();

    //获取已撤销未上传记录数
    long countRolledBackRecNum();

    //清理历史待删除数据
    void cleanRecords();

    //查询某员工当天消费金额
    double getTotalConsumedAmountOfStaff(String staffNo);

    //通过卡号获取白名单信息
    WhiteList getWhiteListByCardNo(String cardNo);

    //通过工号和餐别查询计次次数
    long countFreeMealByMealType(String jobNo,String mealType);

    //通过工号查询计次次数
    long countFreeMeal(String jobNo);

    //通过卡号查询白名单密码
    String getPasswordByCardNo(String cardNo);

    void saveRecord(CardRecord record);

    long countRecordsByMealTypeAndConsumeType(String mealType,String consumeType);

    long countCurJiciRecordsByStaffNo();

    long countTodayJiciRecords();

    void updateRecord(CardRecord cardRecord);

    ////////////////////////////////////////////数据库接口结束/////////////////////////////////////////////

    /**
     * 回调接口
     */
    interface CallBack{
        /**
         * 成功回调
         * @param action 业务类型checkUpdate
         * @param entity 返回数据
         */
        void onSuccess(int action,BaseResponse entity);

        /**
         * 失败回调
         * @param action 业务类型
         * @param code 错误码
         * @param error 错误描述
         */
        void onFail(int action,String error,String code);
    }
}
