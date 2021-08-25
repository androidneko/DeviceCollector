package com.androidcat.biz.manager;

import android.content.Context;
import android.content.Intent;

import com.androidcat.catlibs.net.http.NetService;
import com.androidcat.biz.consts.PublicConsts;
import com.androidcat.catlibs.persistance.SharePreferenceUtil;

public class HttpManager {

    private static HttpManager m_mgHttp = null;
    private static NetService m_svNet = null;
    private static Context mContext;

    public HttpManager() {
        m_svNet = new NetService(PublicConsts.SERVER_URL, PublicConsts.IMEI, PublicConsts.VERSION_NAME,new NetService.TOKEN_ERROR_CALLBACK(){
            @Override
            public void error(String action, String error) {
                if ("/users/logout".equals(action)){
                    //manually logout,do nothing...
                    return;
                }
                Intent intent=new Intent();
                intent.setAction(PublicConsts.ACTION_TOKEN_ERROR);
                intent.setPackage(mContext.getPackageName());
                intent.putExtra("msg",error);
                mContext.sendBroadcast(intent);

            }
        });
    }

    public static NetService getInstance(Context context) {
        mContext=context;
        if (null == m_mgHttp) {
            m_mgHttp = new HttpManager();
        }

        String token = SharePreferenceUtil.getString(PublicConsts.TOKEN);
        m_svNet.setToken(token);
        return m_svNet;
    }


}
