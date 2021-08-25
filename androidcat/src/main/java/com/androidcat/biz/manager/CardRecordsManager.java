package com.androidcat.biz.manager;

import android.content.Context;

import com.androidcat.api.CommApi;
import com.androidcat.biz.bean.CardRecord;
import com.androidcat.biz.database.JlPosDatabase;
import com.androidcat.catlibs.net.http.HttpCallback;
import com.androidcat.biz.consts.PublicConsts;
import com.androidcat.biz.consts.SDKConsts;
import com.androidcat.catlibs.net.http.request.UploadRequest;
import com.androidcat.catlibs.net.http.response.BaseResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by androidcat on 2019/8/28.
 */

public class CardRecordsManager extends BaseManager{

    public CardRecordsManager(Context context, CommApi.CallBack callBack) {
        super(context, callBack);
    }

    public int uploadRecords(){
        JlPosDatabase database = JlPosDatabase.getInstance(context);
        final List<CardRecord> records = database.getTobeUploadedRecords();
        return uploadRecords(records);
    }

    public int uploadRecord(CardRecord cardRecord){
        List<CardRecord> records = new ArrayList<>();
        if (cardRecord.id == 0){
            JlPosDatabase database = JlPosDatabase.getInstance(context);
            CardRecord added = database.getCardRecord(cardRecord);
            if (added != null){
                records.add(added);
            }
        }else {
            records.add(cardRecord);
        }
        return uploadRecords(records);
    }

    public int rollbackRecord(CardRecord cardRecord){
        List<CardRecord> records = new ArrayList<>();
        records.add(cardRecord);
        return rollbackRecords(records);
    }

    public int uploadRecords(final List<CardRecord> records){
        if (records == null || records.size() == 0){
            onSucceeded(SDKConsts.TYPE_UPLOAD_CONSUME, new BaseResponse("SUCCESS","暂无记录"));
            return SDKConsts.SUCCESS;
        }
        UploadRequest request = new UploadRequest();
        request.deviceId = PublicConsts.IMEI;
        request.swipeCardList = records;

        httpManager.uploadRecords(request, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                List<CardRecord> rolledBackList = new ArrayList<>();
                List<CardRecord> uploadedList = new ArrayList<>();
                for (CardRecord record:records){
                    if (record.state == CardRecord.ROLLED_BACK){
                        rolledBackList.add(record);
                        continue;
                    }
                    record.state = CardRecord.TO_BE_DELETED;
                    uploadedList.add(record);
                }
                JlPosDatabase.getInstance(context).deleteAll(rolledBackList);
                JlPosDatabase.getInstance(context).updateAll(uploadedList);
                onSucceeded(SDKConsts.TYPE_UPLOAD_CONSUME, new BaseResponse());
            }

            @Override
            public void onFail(String error, String code) {
                if(error != null && error.contains("重复上传")){
                    for (CardRecord record:records){
                        record.state = CardRecord.TO_BE_DELETED;
                    }
                }
                onFailed(SDKConsts.TYPE_UPLOAD_CONSUME, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }

    public int rollbackRecords(final List<CardRecord> records){
        if (records == null || records.size() == 0){
            onSucceeded(SDKConsts.TYPE_UPLOAD_CONSUME, new BaseResponse("SUCCESS","暂无记录"));
            return SDKConsts.SUCCESS;
        }
        UploadRequest request = new UploadRequest();
        request.deviceId = PublicConsts.IMEI;
        request.swipeCardList = records;

        httpManager.uploadRecords(request, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                for (CardRecord record:records){
                    JlPosDatabase.getInstance(context).delete(record);
                }
                onSucceeded(SDKConsts.TYPE_UPLOAD_CONSUME, new BaseResponse());
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_UPLOAD_CONSUME, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }
}
