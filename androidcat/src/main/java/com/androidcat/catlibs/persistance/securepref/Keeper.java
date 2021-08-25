package com.androidcat.catlibs.persistance.securepref;

import android.content.Context;
import android.os.Build;

/**
 * Created by androidcat on 2019/3/28.
 */

public class Keeper {

    public static final int FACTOR = 2;

    private AESCoder aesCoder;
    private RSACoder rsaCoder;

    private static Keeper keeper;

    private Keeper(Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            rsaCoder = RSACoder.get(context);
        }else {
            aesCoder = AESCoder.get(context);
        }
    }

    public static Keeper getKeeper(Context context){
        if (keeper == null){
            keeper = new Keeper(context);
        }
        return keeper;
    }

    public byte[] rk(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return rsaCoder.rk();
        }else {
            return aesCoder.rk();
        }
    }

    public void uk(byte[] key){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            rsaCoder.uk(key);
        }else {
            aesCoder.uk(key);
        }
    }


    /**
     * 加密
     *
     * @param secret 要加密的数据
     * */
    public byte[] aesEncrypt(byte[] secret){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return rsaCoder.rsaEncrypt(secret);
        }else {
            return aesCoder.aesEncrypt(secret);
        }
    }

    /**
     * 解密
     *
     * @param encrypted 要解密的数据
     * */
    public  byte[]  aesDecrypt(byte[] encrypted) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return rsaCoder.rsaDecrypt(encrypted);
        }else {
            return aesCoder.aesDecrypt(encrypted);
        }
    }
}
