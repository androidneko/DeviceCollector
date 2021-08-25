package com.androidcat.catlibs.persistance.securepref;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.androidcat.catlibs.log.LogUtil;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The Class SecurePreferences.
 */
public class SecurePreferences implements SharedPreferences {

    /**
     * The Constant TAG.
     */
    private static final String TAG = SecurePreferences.class.getName();
    /**
     * The s logging enabled.
     */
    private static boolean sLoggingEnabled = false;
    /**
     * The shared preferences.
     */
    private SharedPreferences sharedPreferences;
    /**
     * The keys.
     */
    private AesCbcWithIntegrity.SecretKeys keys;
    /**
     * The shared pref filename.
     */
    private String sharedPrefFilename;


    /**
     * Instantiates a new secure preferences.
     *
     * @param context            the context
     * @param password           the password
     * @param sharedPrefFilename the shared pref filename
     */
    public SecurePreferences(Context context, final String password, final String sharedPrefFilename) {
        this(context, null, password, sharedPrefFilename);
    }


    /**
     * Instantiates a new secure preferences.
     *
     * @param context            the context
     * @param secretKey          the secret key
     * @param password           the password
     * @param sharedPrefFilename the shared pref filename
     */
    private SecurePreferences(Context context, final AesCbcWithIntegrity.SecretKeys secretKey, final String password, final String sharedPrefFilename) {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferenceFile(context, sharedPrefFilename);
        }

        //
        if (secretKey != null) {
            keys = secretKey;
        } else if (TextUtils.isEmpty(password)) {
            // Initialize or create encryption key
            try {
                final String key = SecurePreferences.generateAesKeyName(context);

                String keyAsString = sharedPreferences.getString(key, null);
                if (keyAsString == null) {
                    keys = AesCbcWithIntegrity.generateKey();
                    //saving new key
                    boolean committed = sharedPreferences.edit().putString(key, keys.toString()).commit();
                    if (!committed) {
                        Log.w(TAG, "Key not committed to prefs");
                    }
                } else {
                    keys = AesCbcWithIntegrity.keys(keyAsString);
                }

                if (keys == null) {
                    throw new GeneralSecurityException("Problem generating Key");
                }

            } catch (GeneralSecurityException e) {
                if (sLoggingEnabled) {
                    LogUtil.e(TAG, "Error init:" + e.getMessage());
                }
                throw new IllegalStateException(e);
            }
        } else {
            //use the password to generate the key
            try {
                //long now = System.currentTimeMillis();
                keys = AesCbcWithIntegrity.generateKeyFromPassword(password, getDeviceSerialNumber(context));
                //keys = AesCbcWithIntegrity.generateKeyFromPassword(password);
                //LogUtil.e(TAG,"generateKeyFromPassword costs:"+(System.currentTimeMillis() - now)+"ms");

                if (keys == null) {
                    throw new GeneralSecurityException("Problem generating Key From Password");
                }
            } catch (GeneralSecurityException e) {
                if (sLoggingEnabled) {
                    LogUtil.e(TAG, "Error init using user password:" + e.getMessage());
                }
                throw new IllegalStateException(e);
            }
        }

    }

    public void updateKeys(byte[] password){
        //use the password to generate the key
        try {
            long now = System.currentTimeMillis();
            keys = AesCbcWithIntegrity.generateKeyFromPassword(password);
            LogUtil.e(TAG,"generateKeyFromPassword costs:"+(System.currentTimeMillis() - now)+"ms");

            if (keys == null) {
                throw new GeneralSecurityException("Problem generating Key From Password");
            }
        } catch (GeneralSecurityException e) {
            if (sLoggingEnabled) {
                LogUtil.e(TAG, "Error init using user password:" + e.getMessage());
            }
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generate aes key name.
     *
     * @param context the context
     * @return the string
     * @throws GeneralSecurityException the general security exception
     */
    private static String generateAesKeyName(Context context) throws GeneralSecurityException {
        final String password = context.getPackageName();
        final byte[] salt = getDeviceSerialNumber(context).getBytes();
        AesCbcWithIntegrity.SecretKeys generatedKeyName = AesCbcWithIntegrity.generateKeyFromPassword(password, salt);
        if (generatedKeyName == null) {
            throw new GeneralSecurityException("Key not generated");
        }

        return hashPrefKey(generatedKeyName.toString());
    }

    /**
     * Gets the device serial number.
     *
     * @param context the context
     * @return the device serial number
     */
    private static String getDeviceSerialNumber(Context context) {
        try {
            String deviceSerial = (String) Build.class.getField("SERIAL").get(
                    null);
            if (TextUtils.isEmpty(deviceSerial) || deviceSerial.length() % 2 != 0) {
                return Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            } else {
                return deviceSerial;
            }
        } catch (Exception ignored) {
            // Fall back  to Android_ID
            return Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }
    }

    /**
     * Hash pref key.
     *
     * @param prefKey the pref key
     * @return the string
     */
    public static String hashPrefKey(String prefKey) {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = prefKey.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);

            return Base64.encodeToString(digest.digest(), AesCbcWithIntegrity.BASE64_FLAGS);

        } catch (NoSuchAlgorithmException e) {
            if (sLoggingEnabled) {
                Log.w(TAG, "Problem generating hash", e);
            }
        } catch (UnsupportedEncodingException e) {
            if (sLoggingEnabled) {
                Log.w(TAG, "Problem generating hash", e);
            }
        }
        return null;
    }

    /**
     * Gets the shared preference file.
     *
     * @param context      the context
     * @param prefFilename the pref filename
     * @return the shared preference file
     */
    private SharedPreferences getSharedPreferenceFile(Context context, String prefFilename) {
        this.sharedPrefFilename = prefFilename;

        if (TextUtils.isEmpty(prefFilename)) {
            return PreferenceManager
                    .getDefaultSharedPreferences(context);
        } else {
            return context.getSharedPreferences(prefFilename, Context.MODE_PRIVATE);
        }
    }

    /**
     * Encrypt.
     *
     * @param cleartext the cleartext
     * @return the string
     */
    public String encrypt(String cleartext) {
        if (TextUtils.isEmpty(cleartext)) {
            return cleartext;
        }
        try {
            return AesCbcWithIntegrity.encrypt(cleartext, keys).toString();
        } catch (GeneralSecurityException e) {
            if (sLoggingEnabled) {
                Log.w(TAG, "encrypt", e);
            }
            return null;
        } catch (UnsupportedEncodingException e) {
            if (sLoggingEnabled) {
                Log.w(TAG, "encrypt", e);
            }
        }
        return null;
    }

    /**
     * Encrypt.
     *
     * @param cleartext the cleartext
     * @return the string
     */
    public String encrypt(byte[] cleartext) {
        if (cleartext == null || cleartext.length == 0) {
            return "";
        }
        try {
            return AesCbcWithIntegrity.encrypt(cleartext, keys).toString();
        } catch (GeneralSecurityException e) {
            if (sLoggingEnabled) {
                Log.w(TAG, "encrypt", e);
            }
            return null;
        }
    }

    /**
     * Decrypt.
     *
     * @param ciphertext the ciphertext
     * @return the string
     */
    private String decrypt(final String ciphertext) {
        if (TextUtils.isEmpty(ciphertext)) {
            return ciphertext;
        }
        try {
            AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(ciphertext);

            return AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);
        } catch (GeneralSecurityException e) {
            if (sLoggingEnabled) {
                Log.w(TAG, "decrypt", e);
            }
        } catch (UnsupportedEncodingException e) {
            if (sLoggingEnabled) {
                Log.w(TAG, "decrypt", e);
            }
        }
        return null;
    }

    /**
     * Decrypt.
     *
     * @param ciphertext the ciphertext
     * @return the string
     */
    private byte[] decryptBytes(final String ciphertext) {
        if (TextUtils.isEmpty(ciphertext)) {
            return new byte[0];
        }
        try {
            AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(ciphertext);

            return AesCbcWithIntegrity.decrypt(cipherTextIvMac, keys);
        } catch (GeneralSecurityException e) {
            if (sLoggingEnabled) {
                Log.w(TAG, "decrypt", e);
            }
        }
        return new byte[0];
    }

    /* (non-Javadoc)
     * @see android.content.SharedPreferences#contains(java.lang.String)
     */
    @Override
    public boolean contains(String key) {
        return sharedPreferences.contains(SecurePreferences.hashPrefKey(key));
    }


    /* (non-Javadoc)
     * @see android.content.SharedPreferences#edit()
     */
    @Override
    public Editor edit() {
        return new Editor();
    }

    /* (non-Javadoc)
     * @see android.content.SharedPreferences#getAll()
     */
    @Override
    public Map<String, String> getAll() {
        //wont be null as per http://androidxref.com/5.1.0_r1/xref/frameworks/base/core/java/android/app/SharedPreferencesImpl.java
        final Map<String, ?> encryptedMap = sharedPreferences.getAll();
        final Map<String, String> decryptedMap = new HashMap<String, String>(
                encryptedMap.size());
        for (Entry<String, ?> entry : encryptedMap.entrySet()) {
            try {
                Object cipherText = entry.getValue();
                //don't include the key
                if (cipherText != null && !cipherText.equals(keys.toString())) {
                    //the prefs should all be strings
                    decryptedMap.put(entry.getKey(),
                            decrypt(cipherText.toString()));
                }
            } catch (Exception e) {
                if (sLoggingEnabled) {
                    Log.w(TAG, "error during getAll", e);
                }
                // Ignore issues that unencrypted values and use instead raw cipher text string
                decryptedMap.put(entry.getKey(),
                        entry.getValue().toString());
            }
        }
        return decryptedMap;
    }

    /* (non-Javadoc)
     * @see android.content.SharedPreferences#getBoolean(java.lang.String, boolean)
     */
    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        final String encryptedValue = sharedPreferences.getString(
                SecurePreferences.hashPrefKey(key), null);
        if (encryptedValue == null) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(decrypt(encryptedValue));
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see android.content.SharedPreferences#getLong(java.lang.String, long)
     */
    @Override
    public long getLong(String key, long defaultValue) {
        final String encryptedValue = sharedPreferences.getString(
                SecurePreferences.hashPrefKey(key), null);
        if (encryptedValue == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(decrypt(encryptedValue));
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    public long getLong(String key, long defaultValue,boolean isEnc) {
        if (isEnc){
            return getLong(key,defaultValue);
        }else {
            return sharedPreferences.getLong(key, defaultValue);
        }
    }

    /* (non-Javadoc)
     * @see android.content.SharedPreferences#getString(java.lang.String, java.lang.String)
     */
    @Override
    public String getString(String key, String defaultValue) {
        final String encryptedValue = sharedPreferences.getString(
                SecurePreferences.hashPrefKey(key), null);
        return (encryptedValue != null) ? decrypt(encryptedValue) : defaultValue;
    }

    public byte[] getByteArray(String key) {
        final String encryptedValue = sharedPreferences.getString(
                SecurePreferences.hashPrefKey(key), null);
        return (encryptedValue != null) ? decryptBytes(encryptedValue) : new byte[0];
    }

    /* (non-Javadoc)
     * @see android.content.SharedPreferences#getStringSet(java.lang.String, java.util.Set)
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Set<String> getStringSet(String key, Set<String> defaultValues) {
        final Set<String> encryptedSet = sharedPreferences.getStringSet(
                SecurePreferences.hashPrefKey(key), null);
        if (encryptedSet == null) {
            return defaultValues;
        }
        final Set<String> decryptedSet = new HashSet<String>(
                encryptedSet.size());
        for (String encryptedValue : encryptedSet) {
            decryptedSet.add(decrypt(encryptedValue));
        }
        return decryptedSet;
    }

    /* (non-Javadoc)
     * @see android.content.SharedPreferences#registerOnSharedPreferenceChangeListener(android.content.SharedPreferences.OnSharedPreferenceChangeListener)
     */
    @Override
    public void registerOnSharedPreferenceChangeListener(
            final OnSharedPreferenceChangeListener listener) {
        sharedPreferences
                .registerOnSharedPreferenceChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see android.content.SharedPreferences#unregisterOnSharedPreferenceChangeListener(android.content.SharedPreferences.OnSharedPreferenceChangeListener)
     */
    @Override
    public void unregisterOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see android.content.SharedPreferences#getFloat(java.lang.String, float)
     */
    @Override
    public float getFloat(String key, float defaultValue) {
        final String encryptedValue = sharedPreferences.getString(
                SecurePreferences.hashPrefKey(key), null);
        if (encryptedValue == null) {
            return defaultValue;
        }
        try {
            String decryptData = decrypt(encryptedValue);
            if (decryptData == null){
                return 0.0f;
            }
            return Float.parseFloat(decryptData);
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see android.content.SharedPreferences#getInt(java.lang.String, int)
     */
    @Override
    public int getInt(String key, int defaultValue) {
        final String encryptedValue = sharedPreferences.getString(
                SecurePreferences.hashPrefKey(key), null);
        if (encryptedValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(decrypt(encryptedValue));
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    public int getInt(String key, int defaultValue,boolean isEnc) {
        if (isEnc){
            return getInt(key, defaultValue);
        }else {
            return sharedPreferences.getInt(SecurePreferences.hashPrefKey(key), defaultValue);
        }
    }

    /**
     * The Class Editor.
     */
    public class Editor implements SharedPreferences.Editor {

        /**
         * The m editor.
         */
        private SharedPreferences.Editor mEditor;

        /**
         * Constructor.
         */
        private Editor() {
            mEditor = sharedPreferences.edit();
        }

        /* (non-Javadoc)
         * @see android.content.SharedPreferences.Editor#putString(java.lang.String, java.lang.String)
         */
        @Override
        public SharedPreferences.Editor putString(String key, String value) {
            mEditor.putString(SecurePreferences.hashPrefKey(key),
                    encrypt(value));
            return this;
        }


        /**
         * Put unencrypted string.
         *
         * @param key   the key
         * @param value the value
         * @return the shared preferences. editor
         */
        public SharedPreferences.Editor putUnencryptedString(String key,
                                                             String value) {
            mEditor.putString(SecurePreferences.hashPrefKey(key), value);
            return this;
        }

        /* (non-Javadoc)
         * @see android.content.SharedPreferences.Editor#putStringSet(java.lang.String, java.util.Set)
         */
        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public SharedPreferences.Editor putStringSet(String key,
                                                     Set<String> values) {
            final Set<String> encryptedValues = new HashSet<String>(
                    values.size());
            for (String value : values) {
                encryptedValues.add(encrypt(value));
            }
            mEditor.putStringSet(SecurePreferences.hashPrefKey(key),
                    encryptedValues);
            return this;
        }

        /* (non-Javadoc)
         * @see android.content.SharedPreferences.Editor#putInt(java.lang.String, int)
         */
        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            mEditor.putString(SecurePreferences.hashPrefKey(key),
                    encrypt(Integer.toString(value)));
            return this;
        }

        public SharedPreferences.Editor putInt(String key, int value,boolean isEnc) {
            if (isEnc){
                mEditor.putString(SecurePreferences.hashPrefKey(key),encrypt(Integer.toString(value)));
            }else {
                mEditor.putInt(SecurePreferences.hashPrefKey(key),value);
            }
            return this;
        }

        /* (non-Javadoc)
         * @see android.content.SharedPreferences.Editor#putLong(java.lang.String, long)
         */
        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            mEditor.putString(SecurePreferences.hashPrefKey(key),
                    encrypt(Long.toString(value)));
            return this;
        }

        /* (non-Javadoc)
         * @see android.content.SharedPreferences.Editor#putFloat(java.lang.String, float)
         */
        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            mEditor.putString(SecurePreferences.hashPrefKey(key),
                    encrypt(Float.toString(value)));
            return this;
        }

        /* (non-Javadoc)
         * @see android.content.SharedPreferences.Editor#putBoolean(java.lang.String, boolean)
         */
        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            mEditor.putString(SecurePreferences.hashPrefKey(key),
                    encrypt(Boolean.toString(value)));
            return this;
        }

        /* (non-Javadoc)
         * @see android.content.SharedPreferences.Editor#remove(java.lang.String)
         */
        @Override
        public SharedPreferences.Editor remove(String key) {
            mEditor.remove(SecurePreferences.hashPrefKey(key));
            return this;
        }

        /* (non-Javadoc)
         * @see android.content.SharedPreferences.Editor#clear()
         */
        @Override
        public SharedPreferences.Editor clear() {
            mEditor.clear();
            return this;
        }

        /* (non-Javadoc)
         * @see android.content.SharedPreferences.Editor#commit()
         */
        @Override
        public boolean commit() {
            return mEditor.commit();
        }

        /* (non-Javadoc)
         * @see android.content.SharedPreferences.Editor#apply()
         */
        @Override
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        public void apply() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                mEditor.apply();
            } else {
                commit();
            }
        }


        public SharedPreferences.Editor putByteArray(String key, byte[] value) {
            mEditor.putString(SecurePreferences.hashPrefKey(key),
                    encrypt(value));
            return this;
        }

        public SharedPreferences.Editor putLong(String key, long value,boolean isEnc) {
            if (isEnc){
                mEditor.putString(SecurePreferences.hashPrefKey(key),
                        encrypt(Long.toString(value)));
            }else {
                mEditor.putLong(key,value);
            }
            return this;
        }
    }


}
