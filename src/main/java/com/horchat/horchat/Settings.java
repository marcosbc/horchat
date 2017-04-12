package com.horchat.horchat;

import android.database.Cursor;

import java.util.HashMap;
import java.util.Map;

public class Settings {
    private Map<String, String> map;
    public Settings(Cursor cursor) {
        map = new HashMap<String, String>();
        // Loop the cursor
        cursor.moveToFirst();
        do {
            if (cursor.getColumnCount() > 0) {
                setValue(cursor.getString(0), cursor.getString(1));
            }
        } while(cursor.moveToNext());
    }
    public String getValue(String key) {
        return map.get(key);
    }
    public void setValue(String key, String value) {
        map.put(key, value);
    }
}
