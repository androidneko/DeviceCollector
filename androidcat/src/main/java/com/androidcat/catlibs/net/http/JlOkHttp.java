package com.androidcat.catlibs.net.http;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.androidcat.catlibs.log.LogUtil;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * 自己封装Okhttp
 * Created by xl on 17/5/26.
 */
public class JlOkHttp {
    private static final String TAG = "JlOkHttp_Logger";
    private static final int TIME_OUT = 10;
    private final static MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Context context;
    private OkHttpClient httpClient;
    private OkHttpClient httpsClient;
    private Handler callbackHandler;
    private static JlOkHttp instance;

    private JlOkHttp() {
        callbackHandler = new Handler(Looper.getMainLooper());
        initClient();
    }

    public void initClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient().newBuilder()
                    .connectionPool(new ConnectionPool(10, 5l, TimeUnit.SECONDS))
                    .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .build();
        }

        if (httpsClient == null) {
            httpsClient = new OkHttpClient().newBuilder()
                    .connectionPool(new ConnectionPool(10, 5l, TimeUnit.SECONDS))
                    .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .sslSocketFactory(getCertificates1(), new IX509TrustManager())
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .build();
        }
    }

    public void resetHttpsClient() {
        httpsClient = null;
        httpsClient = new OkHttpClient().newBuilder()
                .connectionPool(new ConnectionPool(10, 5l, TimeUnit.SECONDS))
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .sslSocketFactory(getCertificates1(), new MyX509TrustManager())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .build();
    }

    /**
     * 获取句柄
     *
     * @return
     */
    public static JlOkHttp getInstance() {
        if (instance == null) {
            instance = new JlOkHttp();
        }
        return instance;
    }

    public void post(final String url, final JSONObject obj, final HttpReqListener listener) {
        //post builder 参数
        String sJson = new Gson().toJson(obj);
        LogUtil.e(TAG, "req url:" + url + "---data:" + sJson);
        if (listener != null) {
            listener.onStart(url);
        }

        post(null, url, sJson, new RawResponseHandler() {
            @Override
            public void onSuccess(int statusCode, String response) {

                LogUtil.e(TAG, "response url:" + url + "---data:" + response);
                if (listener != null) {
                    listener.onResponse(response);
                }
            }

            @Override
            public void onFailure(int statusCode, Exception e) {
                String err = e.getMessage();
                if (err != null) {
                    LogUtil.e(TAG, "请求失败:----ex:" + e.getMessage());
                }
                if (listener != null) {
                    listener.onFailure(e);
                }
            }
        });
    }

    public void post(final String url, final String param, final HttpReqListener listener) {
        //post builder 参数
        LogUtil.e(TAG, "req url:" + url/* + "---data:" + param*/);
        if (listener != null) {
            listener.onStart(url);
        }

        post(null, url, param, new RawResponseHandler() {
            @Override
            public void onSuccess(int statusCode, String response) {

                LogUtil.e(TAG, "response url:" + url /*+ "---data:" + response*/);
                if (listener != null) {
                    listener.onResponse(response);
                }
            }

            @Override
            public void onFailure(int statusCode, Exception e) {
                String err = e.getMessage();
                if (err != null) {
                    LogUtil.e(TAG, "请求失败:----ex:" + e.getMessage());
                }
                if (listener != null) {
                    listener.onFailure(e);
                }
            }
        });
    }

    /**
     * post 请求
     *
     * @param url             url
     * @param params          参数
     * @param responseHandler 回调
     */
    public void post(final String url, final Map<String, String> params, final IResponseHandler responseHandler) {
        post(null, url, params, responseHandler);
    }

    /**
     * post 请求
     *
     * @param context         发起请求的context
     * @param url             url
     * @param params          参数
     * @param responseHandler 回调
     */
    public void post(Context context, final String url, final Map<String, String> params, final IResponseHandler responseHandler) {
        //post builder 参数
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }

        Request request;

        //发起request
        if (context == null) {
            request = new Request.Builder()
                    .url(url)
                    .post(builder.build())
                    .build();
        } else {
            request = new Request.Builder()
                    .url(url)
                    .post(builder.build())
                    .tag(context)
                    .build();
        }

        if (url.startsWith("https")) {
            httpsClient.newCall(request).enqueue(new MyCallback(callbackHandler, responseHandler));
            return;
        }
        httpClient.newCall(request).enqueue(new MyCallback(callbackHandler, responseHandler));
    }

    /**
     * post 请求
     *
     * @param context         发起请求的context
     * @param url             url
     * @param sJson           参数
     * @param responseHandler 回调
     */
    public void post(Context context, final String url, final String sJson, final IResponseHandler responseHandler) {
        Request request;
        RequestBody body = RequestBody.create(JSON, sJson);
        //发起request
        if (context == null) {
            request = new Request.Builder()
                    .url(url)
                    .addHeader("ENCODEMETHOD", "des")
                    .addHeader("SIGNMETHOD", "sign")
                    .addHeader("APPID", "8a88a8ee65ea37a40165f11ed69a0000")
                    .post(body)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(url)
                    .addHeader("ENCODEMETHOD", "des")
                    .addHeader("SIGNMETHOD", "sign")
                    .addHeader("APPID", "8a88a8ee65ea37a40165f11ed69a0000")
                    .post(body)
                    .tag(context)
                    .build();
        }
        if (url.startsWith("https")) {
            httpsClient.newCall(request).enqueue(new MyCallback(callbackHandler, responseHandler));
            return;
        }
        httpClient.newCall(request).enqueue(new MyCallback(callbackHandler, responseHandler));
    }

    /**
     * get 请求
     *
     * @param url             url
     * @param params          参数
     * @param responseHandler 回调
     */
    public void get(final String url, final Map<String, String> params, final IResponseHandler responseHandler) {
        get(null, url, params, responseHandler);
    }

    /**
     * get 请求
     *
     * @param context         发起请求的context
     * @param url             url
     * @param params          参数
     * @param responseHandler 回调
     */
    public void get(Context context, final String url, final Map<String, String> params, final IResponseHandler responseHandler) {
        //拼接url
        String get_url = url;
        if (params != null && params.size() > 0) {
            int i = 0;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (i++ == 0) {
                    get_url = get_url + "?" + entry.getKey() + "=" + entry.getValue();
                } else {
                    get_url = get_url + "&" + entry.getKey() + "=" + entry.getValue();
                }
            }
        }

        Request request;

        //发起request
        if (context == null) {
            request = new Request.Builder()
                    .url(url)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(url)
                    .tag(context)
                    .build();
        }

        httpClient.newCall(request).enqueue(new MyCallback(callbackHandler, responseHandler));
    }


    /**
     * 取消当前context的所有请求
     *
     * @param context
     */
    public void cancel(Context context) {
        if (context == null) {
            return;
        }
        if (httpClient != null) {
            for (Call call : httpClient.dispatcher().queuedCalls()) {
                if (call.request().tag() != null) {
                    if (call.request().tag().equals(context))
                        call.cancel();
                }
            }
            for (Call call : httpClient.dispatcher().runningCalls()) {
                if (call.request().tag() != null) {
                    if (call.request().tag().equals(context))
                        call.cancel();
                }
            }
        }
    }


    //callback
    private class MyCallback implements Callback {

        private Handler mHandler;
        private IResponseHandler mResponseHandler;

        public MyCallback(Handler handler, IResponseHandler responseHandler) {
            mHandler = handler;
            mResponseHandler = responseHandler;
        }

        @Override
        public void onFailure(Call call, final IOException e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mResponseHandler.onFailure(0, e);
                }
            });
        }

        @Override
        public void onResponse(Call call, final Response response) throws IOException {
            if (response.isSuccessful()) {
                final String response_body = response.body().string();

                if (mResponseHandler instanceof JsonResponseHandler) {       //json回调
                    try {
                        final JSONObject jsonBody = new JSONObject(response_body);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ((JsonResponseHandler) mResponseHandler).onSuccess(response.code(), jsonBody);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (mResponseHandler instanceof GsonResponseHandler) {    //gson回调
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Gson gson = new Gson();
                                ((GsonResponseHandler) mResponseHandler).onSuccess(response.code(),
                                        gson.fromJson(response_body, ((GsonResponseHandler) mResponseHandler).getType()));
                            } catch (final Exception e) {
                                LogUtil.e(TAG, "onResponse fail parse gson, body=" + response_body + "\n" + "ex:" + e.getMessage());
                                mResponseHandler.onFailure(response.code(), e);
                            }

                        }
                    });
                } else if (mResponseHandler instanceof RawResponseHandler) {     //raw字符串回调
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((RawResponseHandler) mResponseHandler).onSuccess(response.code(), response_body);
                        }
                    });
                }
            } else {
                LogUtil.e(TAG, "onResponse fail :" + response.message());

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mResponseHandler.onFailure(0, new Exception(response.message()));
                    }
                });
            }
        }
    }


    public static SSLSocketFactory getCertificates(InputStream... certificates) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e) {
                }
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");

            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            trustManagerFactory.init(keyStore);
            sslContext.init(
                    null,
                    trustManagerFactory.getTrustManagers(),
                    new SecureRandom());
            return sslContext.getSocketFactory();
            //return new NoSSLv3SocketFactory(sslContext.getSocketFactory());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
	
    //增加https证书检验和比对，比对客户端和服务端公钥是否一致
    public static SSLSocketFactory getCertificates1() {
        try{
            SSLContext sslContext = SSLContext.getInstance("TLS");
            //TrustManager[] trustManagers = {new MyX509TrustManager()};
            TrustManager[] trustManagers = {new IX509TrustManager()};
            sslContext.init(null, trustManagers, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static class MyX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            for (X509Certificate cert : chain) {
                try {
                    // Make sure that it hasn't expired.
                    cert.checkValidity();
                    // Verify the certificate's public key chain.
                    cert.verify(cert.getPublicKey());
                } catch (CertificateException e) {
                    //此异常是证书已经过期异常，在手机调到证书生效时间之后会捕捉到此异常
                    LogUtil.w(TAG, "checkClientTrusted ex:" + e.getLocalizedMessage());
                    if (e instanceof CertificateExpiredException
                            || e instanceof CertificateNotYetValidException){
                       return;
                    }
                    else {
                        throw e;
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (chain == null) {
                throw new CertificateException("checkServerTrusted: X509Certificate array is null");
            }
            if (chain.length < 1) {
                throw new CertificateException("checkServerTrusted: X509Certificate is empty");
            }
            if (!(null != authType && authType.equals("ECDHE_RSA"))) {
                throw new CertificateException("checkServerTrusted: AuthType is not ECDHE_RSA");
            }

            //检查所有证书
            try {
                TrustManagerFactory factory = TrustManagerFactory.getInstance("X509");
                factory.init((KeyStore) null);
                for (TrustManager trustManager : factory.getTrustManagers()) {
                    ((X509TrustManager) trustManager).checkServerTrusted(chain, authType);
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (CertificateException e){
                //此异常是证书已经过期异常，在手机调到证书生效时间之后会捕捉到此异常
                LogUtil.w(TAG, "checkClientTrusted ex:" + e.getLocalizedMessage());
                if (e instanceof CertificateExpiredException
                        || e instanceof CertificateNotYetValidException ){
                    return;
                }
                else {
                    throw e;
                }
            }

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream certificateIs = new Buffer().writeUtf8(JlSlmCer.SLM_CER).inputStream();
            X509Certificate clientCertificate = (X509Certificate) certificateFactory.generateCertificate(certificateIs);
            try{
                if (certificateIs != null){
                    certificateIs.close();
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            X509Certificate certificate = chain[0];
            PublicKey clientPublicKey = clientCertificate.getPublicKey();
            PublicKey serverPublicKey = certificate.getPublicKey();
            String serverEncoded = new BigInteger(1, serverPublicKey.getEncoded()).toString(16);
            String clientEncoded = new BigInteger(1, clientPublicKey.getEncoded()).toString(16);
            if (!clientEncoded.equals(serverEncoded)) {
                android.util.Log.d(TAG, "public key is diffrent");
                throw new CertificateException("server's PublicKey is not equals to client's PublicKey");
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class IX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            for (X509Certificate cert : chain) {
                try {
                    // Make sure that it hasn't expired.
                    cert.checkValidity();
                    // Verify the certificate's public key chain.
                    cert.verify(cert.getPublicKey());
                } catch (CertificateException e) {
                    //此异常是证书已经过期异常，在手机调到证书生效时间之后会捕捉到此异常
                    LogUtil.w(TAG, "checkClientTrusted ex:" + e.getLocalizedMessage());
                    if (e instanceof CertificateExpiredException
                            || e instanceof CertificateNotYetValidException){
                        return;
                    }
                    else {
                        throw e;
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (chain == null) {
                throw new CertificateException("checkServerTrusted: X509Certificate array is null");
            }
            if (chain.length < 1) {
                throw new CertificateException("checkServerTrusted: X509Certificate is empty");
            }
            if (!(null != authType && authType.equals("ECDHE_RSA"))) {
                throw new CertificateException("checkServerTrusted: AuthType is not ECDHE_RSA");
            }

            for (X509Certificate cert : chain) {
                try {
                    // Make sure that it hasn't expired.
                    cert.checkValidity();
                } catch (CertificateExpiredException e) {
                    //此异常是证书已经过期异常，在手机调到证书生效时间之后会捕捉到此异常
                    LogUtil.w(TAG, "checkServerTrusted: CertificateExpiredException:" + e.getLocalizedMessage());
                } catch (CertificateNotYetValidException e) {
                    //此异常是证书未生效异常，在手机调到证书生效时间之前会捕捉到此异常
                    LogUtil.w(TAG, "checkServerTrusted: CertificateNotYetValidException:" + e.getLocalizedMessage());
                }
                try {
                    // Verify the certificate's public key chain.
                    cert.verify(cert.getPublicKey());
                } catch (CertificateExpiredException e) {
                    //此异常是证书已经过期异常，在手机调到证书生效时间之后会捕捉到此异常
                    LogUtil.w(TAG, "checkServerTrusted: CertificateExpiredException:" + e.getLocalizedMessage());
                } catch (CertificateNotYetValidException e) {
                    //此异常是证书未生效异常，在手机调到证书生效时间之前会捕捉到此异常
                    LogUtil.w(TAG, "checkServerTrusted: CertificateNotYetValidException:" + e.getLocalizedMessage());
                } catch (CertificateException ex) {
                    //其他异常正常报错
                    LogUtil.w(TAG, "Throw checkClientTrusted: " + ex.getLocalizedMessage());
                    throw ex;
                } catch (NoSuchAlgorithmException e) {
                    LogUtil.w(TAG, "checkServerTrusted: NoSuchAlgorithmException:" + e.getLocalizedMessage());
                } catch (InvalidKeyException e) {
                    LogUtil.w(TAG, "checkServerTrusted: InvalidKeyException:" + e.getLocalizedMessage());
                    throw new CertificateException(e.getMessage());
                } catch (NoSuchProviderException e) {
                    LogUtil.w(TAG, "checkServerTrusted: NoSuchProviderException:" + e.getLocalizedMessage());
                } catch (SignatureException e) {
                    LogUtil.w(TAG, "checkServerTrusted: SignatureException:" + e.getLocalizedMessage());
                }
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
