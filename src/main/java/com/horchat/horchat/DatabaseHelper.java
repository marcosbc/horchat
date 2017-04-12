package com.horchat.horchat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseHelper extends SQLiteOpenHelper {
    /* Database configuration */
    private static final String DB_NAME         = "horchat";
    private static final int DB_VERSION         = 1;
    /* Table names */
    private static final String TABLE_ACCOUNTS  = "accounts";
    private static final String TABLE_SETTINGS  = "settings";
    /* Strings for creating and dropping databases */
    private static final String SQL_CREATE_ACCOUNTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ACCOUNTS + " ("   +
            "    _id        INTEGER PRIMARY KEY,"                   +
            "    username   TEXT,"                                  +
            "    nickname   TEXT,"                                  +
            "    password   TEXT,"                                  +
            "    realname   TEXT,"                                  +
            "    host       TEXT,"                                  +
            "    port       INTEGER"                                +
            ")";
    private static final String SQL_DROP_ACCOUNTS =
            "DROP DATABASE IF EXISTS " + TABLE_ACCOUNTS;
    private static final String SQL_CREATE_SETTINGS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SETTINGS + " ("   +
            "    key        TEXT PRIMARY KEY,"                      +
            "    value      TEXT"                                   +
            ")";
    private static final String SQL_DROP_SETTINGS =
            "DROP DATABASE IF EXISTS " + TABLE_SETTINGS;
    /* Table keys */
    public static final String KEY_CURRENT_ACCOUNT = "currentAccountId";
    /* Class attributes */
    private Settings settings;
    /* Database helper */
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    /* Database instantiation method */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ACCOUNTS);
        db.execSQL(SQL_CREATE_SETTINGS);
        settings = getSettings(db);
    }
    /* Database upgrade method */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /* TODO: Implement proper upgrade method */
        db.execSQL(SQL_DROP_ACCOUNTS);
        db.execSQL(SQL_DROP_SETTINGS);
        onCreate(db);
    }
    /* Helper methods */
    private Settings getSettings(SQLiteDatabase db) {
        String tuples[] = {"key", "value"};
        Cursor cursor = db.query(TABLE_SETTINGS, tuples, null, null, null, null, null, null);
        Settings settings = new Settings(cursor);
        cursor.close();
        return settings;
    }
    public String getSetting(String key) {
        String value = null;
        if (settings != null) {
            value = settings.getValue(key);
        }
        return value;
    }
    public int getIntegerSetting(String key) {
        int value = -1;
        String stringValue = getSetting(key);
        if (stringValue != null) {
            value = Integer.parseInt(stringValue);
        }
        return value;
    }
    public long setSetting(String key, String value) {
        SQLiteDatabase db = getWritableDatabase();
        long returnCode = 0;
        if (db != null) {
            ContentValues row = new ContentValues();
            row.put(key, value);
            returnCode = db.insertWithOnConflict(TABLE_SETTINGS, null, row, SQLiteDatabase.CONFLICT_REPLACE);
            settings.setValue(key, value);
        }
        db.close();
        return returnCode;
    }
    public Account getCurrentAccount() {
        SQLiteDatabase db = getReadableDatabase();
        Account account = null;
        if (db != null) {
            String tuples[] = { "username", "nickname", "password", "realname", "host", "port" };
            // Get the current active account
            int currentAccount = getIntegerSetting(KEY_CURRENT_ACCOUNT);
            // Check if the key is defined, if not, the user was not logged in
            if (currentAccount > 0) {
                // Search for the current session
                Cursor cursor = db.query(TABLE_ACCOUNTS, tuples, "_id = '" + currentAccount + "'", null, null, null, null, null);
                account = new Account(currentAccount, cursor);
                cursor.close();
            }
        }
        db.close();
        return account;
    }
}
