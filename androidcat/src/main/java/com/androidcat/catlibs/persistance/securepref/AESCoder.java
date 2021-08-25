package com.androidcat.catlibs.persistance.securepref;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.androidcat.catlibs.log.LogUtil;
import com.androidcat.catlibs.utils.CommonMethods;
import com.androidcat.catlibs.utils.DesTools;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import androidx.annotation.RequiresApi;

/**
 * Created by androidcat on 2019/1/15.
 */

public class AESCoder {
    private static final String TAG = "AESCoder";

    public static final String KEY_ALIAS = "tyCardKeeper2";
    private static final String AES_MODE =  "AES/GCM/NoPadding";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String ENCRYPTED_KEY = "twhh";
    private static final String ENCRYPTED_KEY2 = "sebl";
    private static final String ENCRYPTED_IV = "jwhhsebw";
    private static final String SHARED_PREFENCE_NAME = "xfile2";

    private static AESCoder INSTANCE;
    private static Object LOCK = new Object();
    private KeyStore keyStore;
    private Context context;
    private SecretKey secretKey;
    private byte[] iv = null;

    @TargetApi(Build.VERSION_CODES.M)
    private AESCoder(Context context){
        this.context = context;
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void init() {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);

            if (!keyStore.containsAlias(KEY_ALIAS)){
                final KeyGenerator keyGenerator = KeyGenerator
                        .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
                keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build());
                secretKey = keyGenerator.generateKey();
            }
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AESCoder get(Context context){
        if (INSTANCE == null){
            synchronized (LOCK){
                if (INSTANCE == null){
                    INSTANCE = new AESCoder(context);
                }
            }
        }

        return INSTANCE;
    }

    private SecretKey getSecretKey() throws NoSuchAlgorithmException,
            UnrecoverableEntryException, KeyStoreException {
        return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
    }

    /**
     * 加密
     *
     * @param secret 要加密的数据
     * */
    public byte[] aesEncrypt(byte[] secret){
        try {
            final Cipher cipher = Cipher.getInstance(AES_MODE);
            if (secretKey == null){
                secretKey = getSecretKey();
            }
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            iv = cipher.getIV();
            saveIv();

            return cipher.doFinal(secret);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 解密
     *
     * @param encrypted 要解密的数据
     * */
    public  byte[]  aesDecrypt(byte[] encrypted) {
        try {
            final Cipher cipher = Cipher.getInstance(AES_MODE);
            getIv();
            final GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            if (secretKey == null){
                secretKey = getSecretKey();
            }
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            return cipher.doFinal(encrypted);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveIv(){
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFENCE_NAME, Context.MODE_PRIVATE);
        String ivStr = CommonMethods.bytesToHex(iv);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(ENCRYPTED_IV, ivStr);
        edit.commit();
    }

    private void getIv(){
        if (iv == null){
            SharedPreferences pref = context.getSharedPreferences(SHARED_PREFENCE_NAME, Context.MODE_PRIVATE);
            String ivStr = pref.getString(ENCRYPTED_IV,"");
            iv = CommonMethods.str2bytes(ivStr);
        }
    }

    public void gk(){
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFENCE_NAME, Context.MODE_PRIVATE);
        String enryptedKeyB64 = pref.getString(ENCRYPTED_KEY, null);
        if (enryptedKeyB64 == null) {
            byte[] key = new byte[48];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(key);
            //LogUtil.d(TAG,"generated key:"+CommonMethods.bytesToHex(key));
            byte[] confoundKey = DesTools.confound(key,Keeper.FACTOR);
            byte[] encryptedKey = aesEncrypt(confoundKey);
            enryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString(ENCRYPTED_KEY, enryptedKeyB64);
            edit.commit();
            LogUtil.d(TAG,"----random key has been generated successfully----");
        }
    }

    public void uk(byte[] key){
        long now = System.currentTimeMillis();

        byte[] encryptedKey = aesEncrypt(key);
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFENCE_NAME, Context.MODE_PRIVATE);

        String enryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
        String oldKey = pref.getString(ENCRYPTED_KEY, null);

        SharedPreferences.Editor edit = pref.edit();
        edit.putString(ENCRYPTED_KEY, enryptedKeyB64);
        edit.putString(ENCRYPTED_KEY2, oldKey);
        edit.commit();

        LogUtil.e(TAG,"uk costs:"+(System.currentTimeMillis() - now)+"ms");
    }

    public byte[] rk(){
        LogUtil.d(TAG,"retrieve random key from file");
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFENCE_NAME, Context.MODE_PRIVATE);
        String enryptedKeyB64 = pref.getString(ENCRYPTED_KEY, null);
        if (enryptedKeyB64 == null){
            LogUtil.d(TAG,"random key is null,go to gk");
            gk();
            return rk();
        }
        byte[] encryptedKey = Base64.decode(enryptedKeyB64, Base64.DEFAULT);

        long now = System.currentTimeMillis();
        byte[] confoundKey = aesDecrypt(encryptedKey);
        byte[] key = DesTools.unconfound(confoundKey,Keeper.FACTOR);
        LogUtil.e(TAG,"aesDecrypt costs:"+(System.currentTimeMillis() - now)+"ms");
        String rk = CommonMethods.bytesToHex(key);
        //LogUtil.d(TAG,"retrieved key:"+rk);
        return key;
    }

}
