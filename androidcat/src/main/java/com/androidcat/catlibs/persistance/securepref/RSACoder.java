package com.androidcat.catlibs.persistance.securepref;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;

import com.androidcat.catlibs.log.LogUtil;
import com.androidcat.catlibs.utils.CommonMethods;
import com.androidcat.catlibs.utils.DesTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

/**
 * Created by androidcat on 2019/1/15.
 */

public class RSACoder {
    private static final String TAG = "RSACoder";

    public static final String KEY_ALIAS = "tyCardKeeper";
    private static final String RSA_MODE =  "RSA/ECB/PKCS1Padding";
    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String ENCRYPTED_KEY = "twhh";
    private static final String ENCRYPTED_KEY2 = "sebl";
    private static final String SHARED_PREFENCE_NAME = "xfile";

    private static RSACoder INSTANCE;
    private static Object LOCK = new Object();
    private KeyStore keyStore;
    private Context context;

    private RSACoder(Context context){
        this.context = context;
        init(context);
    }

    private void init(Context context) {
        try {
            keyStore = KeyStore.getInstance(AndroidKeyStore);
            keyStore.load(null);
            // Generate the RSA key pairs
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                // Generate a key pair for encryption
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 30);
                KeyPairGeneratorSpec spec = new      KeyPairGeneratorSpec.Builder(context)
                        .setAlias(KEY_ALIAS)
                        .setSubject(new X500Principal("CN=" + KEY_ALIAS))
                        .setSerialNumber(BigInteger.TEN)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, AndroidKeyStore);
                kpg.initialize(spec);
                kpg.generateKeyPair();
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public static RSACoder get(Context context){
        if (INSTANCE == null){
            synchronized (LOCK){
                if (INSTANCE == null){
                    INSTANCE = new RSACoder(context);
                }
            }
        }

        return INSTANCE;
    }


    /**
     * 获取当前应用密钥库中的条目
     *
     * @return
     * */
    public Enumeration<String> getAliases(){
        if (keyStore == null) {
            return null;
        }

        try {
            return keyStore.aliases();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 先判断是否存在该别名
     * */
    public boolean containsAlias(String alias) {
        if (keyStore == null || TextUtils.isEmpty(alias)){
            return false;
        }

        boolean contains = false;
        try{
            contains = keyStore.containsAlias(alias);
        }catch (Exception e){
            e.printStackTrace();
        }
        return contains;
    }

    /**
     * 生成新的密钥
     *
     * @param context
     * */
    public KeyPair generateKey(Context context){
        if (containsAlias(KEY_ALIAS)){
            return null;
        }

        try {
            // Generate a key pair for encryption
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 30);
            KeyPairGeneratorSpec spec = new      KeyPairGeneratorSpec.Builder(context)
                    .setAlias(KEY_ALIAS)
                    .setSubject(new X500Principal("CN=" + KEY_ALIAS))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, AndroidKeyStore);
            kpg.initialize(spec);
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return null;
    }

    public void deleteKey(final String alias){
        try{
            keyStore.deleteEntry(alias);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加密
     *
     * @param secret 要加密的数据
     * */
    public byte[] rsaEncrypt(byte[] secret){
        try {
            //取出密钥
            PublicKey publicKey = keyStore.getCertificate(KEY_ALIAS).getPublicKey();
            // Encrypt the text
            Cipher inputCipher = getCipher();
            inputCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
            cipherOutputStream.write(secret);
            cipherOutputStream.close();

            byte[] vals = outputStream.toByteArray();
            return vals;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 解密
     *
     * @param encrypted 要解密的数据
     * */
    public  byte[]  rsaDecrypt(byte[] encrypted) {
        try {
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, null);
            Cipher output = getCipher();
            output.init(Cipher.DECRYPT_MODE, privateKey);
            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(encrypted), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte)nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for(int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }
            return bytes;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Cipher getCipher() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // below android m
                return Cipher.getInstance(RSA_MODE, "AndroidOpenSSL"); // error in android 6: InvalidKeyException: Need RSA private or public key
            }
            else { // android m and above
                return Cipher.getInstance(RSA_MODE, "AndroidKeyStoreBCWorkaround"); // error in android 5: NoSuchProviderException: Provider not available: AndroidKeyStoreBCWorkaround
            }
        } catch(Exception exception) {
            throw new RuntimeException("getCipher: Failed to get an instance of Cipher", exception);
        }
    }

    /**
     * 对数据进行签名
     *
     * @param data
     * @param alias
     * */
    public byte[] sign(byte[] data, String alias){
        try{
            //取出密钥
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            Signature s = Signature.getInstance("SHA1withRSA");
            s.initSign(privateKeyEntry.getPrivateKey());
            s.update(data);
            return s.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 验证数据签名
     *
     * @param data 原始数据
     * @param signatureData 签署的数据
     * @param alias
     * */
    public boolean verify (byte[] data, byte[] signatureData, String alias){
        try{
            //取出密钥
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);

            Signature s = Signature.getInstance("SHA1withRSA");
            s.initVerify(privateKeyEntry.getCertificate());
            s.update(data);
            return s.verify(signatureData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return false;
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
            byte[] encryptedKey = rsaEncrypt(confoundKey);
            enryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString(ENCRYPTED_KEY, enryptedKeyB64);
            edit.commit();
            LogUtil.d(TAG,"----random key has been generated successfully----");
        }
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
        byte[] confoundKey = rsaDecrypt(encryptedKey);
        byte[] key = DesTools.unconfound(confoundKey,Keeper.FACTOR);
        LogUtil.e(TAG,"rsaDecrypt costs:"+(System.currentTimeMillis() - now)+"ms");
        String rk = CommonMethods.bytesToHex(key);
        //LogUtil.d(TAG,"retrieved key:"+rk);
        return key;
    }

    public void uk(byte[] key){
        long now = System.currentTimeMillis();

        byte[] encryptedKey = rsaEncrypt(key);
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFENCE_NAME, Context.MODE_PRIVATE);

        String enryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
        String oldKey = pref.getString(ENCRYPTED_KEY, null);

        SharedPreferences.Editor edit = pref.edit();
        edit.putString(ENCRYPTED_KEY, enryptedKeyB64);
        edit.putString(ENCRYPTED_KEY2, oldKey);
        edit.commit();

        LogUtil.e(TAG,"uk costs:"+(System.currentTimeMillis() - now)+"ms");
    }

}
