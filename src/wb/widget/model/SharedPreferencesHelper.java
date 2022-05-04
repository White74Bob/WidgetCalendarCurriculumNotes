package wb.widget.model;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class SharedPreferencesHelper {
    private final SharedPreferences mSharedPreferences;

    public SharedPreferencesHelper(Context context, String preferencesName) {
        mSharedPreferences = context.getSharedPreferences(preferencesName,
                Context.MODE_PRIVATE);
    }

    public void registerListener(final OnSharedPreferenceChangeListener listener) {
        if (listener == null) return;
        mSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterListener(final OnSharedPreferenceChangeListener listener) {
        if (listener == null) return;
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * 存储
     */
    public void put(String key, Object object) {
        if (object == null) return;

        Editor editor = mSharedPreferences.edit();
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        editor.apply();
    }

    public Object get(String key, Object defaultObject) {
        if (defaultObject == null) {
            return mSharedPreferences.getString(key, null);
        }
        if (defaultObject instanceof String) {
            return mSharedPreferences.getString(key, (String) defaultObject);
        }
        if (defaultObject instanceof Integer) {
            return mSharedPreferences.getInt(key, (Integer) defaultObject);
        }
        if (defaultObject instanceof Boolean) {
            return mSharedPreferences.getBoolean(key, (Boolean) defaultObject);
        }
        if (defaultObject instanceof Float) {
            return mSharedPreferences.getFloat(key, (Float) defaultObject);
        }
        if (defaultObject instanceof Long) {
            return mSharedPreferences.getLong(key, (Long) defaultObject);
        }
        return mSharedPreferences.getString(key, null);
    }

    /**
     * 移除keys.
     */
    public void remove(final String... keys) {
        if (keys == null || keys.length <= 0) return;
        Editor editor = mSharedPreferences.edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.apply();
    }
    
    /**
     * 移除keys.
     */
    public void remove(final ArrayList<String> keys) {
        if (keys == null || keys.size() <= 0) return;
        Editor editor = mSharedPreferences.edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.apply();
    }

    /**
     * 移除那些以某些字符串开头的keys.
     */
    public void removeLike(final String[] keysLike, final String... keys) {
        if (keys == null || keys.length <= 0) return;
        Editor editor = mSharedPreferences.edit();
        for (String key : keys) {
            if (keysLike == null || keysLike.length <= 0) {
                editor.remove(key);
            } else {
                for (String keyLike : keysLike) {
                    if (key.startsWith(keyLike)) {
                        editor.remove(key);
                        break;
                    }
                }
            }
        }
        editor.apply();
    }

    /**
     * 清除所有数据
     */
    public void clear() {
        Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * 查询某个key是否存在
     */
    public boolean contains(String key) {
        return mSharedPreferences.contains(key);
    }

    /**
     * 返回所有的键值对
     */
    public Map<String, ?> getAll() {
        return mSharedPreferences.getAll();
    }
}
