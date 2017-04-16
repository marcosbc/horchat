package com.horchat.horchat;

import android.database.Cursor;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Settings {
    private Map<String, String> map;
    public Settings(Cursor cursor) {
        map = new HashMap<String, String>();
        if (cursor != null) {
            // Loop the cursor
            while (cursor.moveToNext()) {
                if (cursor.getColumnCount() > 0) {
                    setValue(cursor.getString(0), cursor.getString(1));
                }
            }
        }
    }
    public String getValue(String key) {
        return map.get(key);
    }
    public void setValue(String key, String value) {
        map.put(key, value);
    }
    public void clearValue(String key) {
        map.remove(key);
    }
}
