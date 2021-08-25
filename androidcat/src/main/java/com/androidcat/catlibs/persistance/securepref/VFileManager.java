package com.androidcat.catlibs.persistance.securepref;

import android.content.Context;
import android.content.SharedPreferences;

import com.androidcat.catlibs.log.LogUtil;
import com.androidcat.catlibs.utils.CommonMethods;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by androidcat on 2018/8/20.
 */

public class VFileManager {

    private static final String TAG = "VFileManager";
    private static final String VF_HEADER = "vfile_";

    private SecurePreferences prefs;
    private Context context;
    private String appId;
    private byte[] key;

    static Map<String, VFileManager> managers = new HashMap<>();

    private VFileManager(Context context, String appletId) {

        this.context = context;
        this.appId = appletId;
        getKey();
        ensurePrefs();
    }

    public static VFileManager getInstance(Context context, String appId) {
        VFileManager manager = managers.get(appId);
        if (manager == null) {
            synchronized (VFileManager.class) {
                if (manager == null) {
                    manager = new VFileManager(context, appId);
                    managers.put(appId, manager);
                }
            }
        }
        return manager;
    }

    private void ensurePrefs() {
        if (prefs == null) {
            prefs = new SecurePreferences(context, CommonMethods.bytesToHex(this.key), VF_HEADER + appId);
        }
    }

    public void setValueMap(Map<String, Object> data) {
        if (data == null || data.size() == 0) {
            return;
        }
        ensurePrefs();
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> keys = data.keySet();
        for (String key : keys) {
            Object value = data.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Integer) {
                ((SecurePreferences.Editor) editor).putInt(key, (int) value, false);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (boolean) value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (float) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (long) value);
            } else if (value instanceof byte[]) {
                ((SecurePreferences.Editor) editor).putByteArray(key, (byte[]) value);
            } else {
                LogUtil.e(TAG, "Exception: unknown value type");
            }
        }
        editor.apply();
    }

    public void setByteArray(String key, byte[] value) {
        ensurePrefs();
        SecurePreferences.Editor editor = prefs.edit();
        editor.putByteArray(key, value);
        editor.apply();
    }

    public byte[] getByteArray(String key) {
        ensurePrefs();
        return prefs.getByteArray(key);
    }

    /**
     * Sets the preference value.
     *
     * @param key   the key
     * @param value the value
     */
    public boolean setValue(String key, String value) {
        ensurePrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * Sets the preference value.
     *
     * @param key   the key
     * @param value the value
     */
    public void setValueAsync(String key, String value) {
        ensurePrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void destroySelf() {
        ensurePrefs();
        prefs.edit().clear().commit();
    }

    /**
     * Gets the preference value.
     *
     * @param key the key
     * @return the preference value
     */
    public String getValue(String key) {
        ensurePrefs();
        return prefs.getString(key, "");
    }

    public String getValue(String key, String def) {

        ensurePrefs();
        return prefs.getString(key, def);
    }

    public void setValue(String key, boolean value) {
        ensurePrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBooleanValue(String key, boolean def) {
        ensurePrefs();
        return prefs.getBoolean(key, def);
    }

    public void setValue(String key, int value, boolean isEnc) {
        ensurePrefs();
        SecurePreferences.Editor editor = prefs.edit();
        editor.putInt(key, value, isEnc);
        editor.apply();
    }

    public boolean setValue(String key, int value) {
        ensurePrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public void setValueAsync(String key, int value) {
        ensurePrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getIntValue(String key, int def) {
        ensurePrefs();
        return prefs.getInt(key, def);
    }

    public int getIntValue(String key, int def, boolean isEnc) {
        ensurePrefs();
        return prefs.getInt(key, def, isEnc);
    }

    public void setValue(String key, float value) {
        ensurePrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public float getFloatValue(String key, float def) {
        ensurePrefs();
        return prefs.getFloat(key, def);
    }

    public void setValue(String key, double value) {
        ensurePrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(key, (float) value);
        editor.commit();
    }

    public double getDoubleValue(String key, float def) {
        ensurePrefs();
        return prefs.getFloat(key, def);
    }

    public void setValue(String key, long value) {
        ensurePrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public void setLongValue(String key, long value) {
        ensurePrefs();
        SecurePreferences.Editor editor = prefs.edit();
        editor.putLong(key, value, false);
        editor.commit();
    }

    public long getLongValue(String key, long def) {
        ensurePrefs();
        return prefs.getLong(key, def);
    }

    public long getLongValue(String key, long def, boolean isEnc) {
        if (isEnc) {
            return getLongValue(key, def);
        } else {
            ensurePrefs();
            return prefs.getLong(key, def, false);
        }
    }

    public void removeValue(String key) {
        ensurePrefs();
        prefs.edit().remove(key).commit();
    }


    public void removeObject(Class<?> clazz) {
        ensurePrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(clazz.getCanonicalName());
        editor.commit();
    }

    public void removeObject(Object obj) {
        ensurePrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(obj.getClass().getCanonicalName());
        editor.commit();
    }

    public byte[] getKey() {
        if (this.key == null) {
            this.key = Keeper.getKeeper(context).rk();
        }
        return this.key;
    }

    public void updateKey() {
        if (prefs != null) {
            byte[] keyBytes = new byte[48];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(keyBytes);
            this.key = keyBytes;
            prefs.updateKeys(keyBytes);
            Keeper.getKeeper(context).uk(keyBytes);
        }
    }
}
