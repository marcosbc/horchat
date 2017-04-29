package com.horchat.horchat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import com.horchat.horchat.activity.MainActivity;
import com.horchat.horchat.model.Account;
import com.horchat.horchat.model.Settings;

public class DatabaseHelper extends SQLiteOpenHelper {
    /* Database configuration */
    private static final String DB_NAME         = "horchat";
    private static final int DB_VERSION         = 1;
    /* Table names */
    private static final String TABLE_ACCOUNTS  = "accounts";
    private static final String TABLE_SETTINGS  = "settings";
    /* Strings for creating and dropping databases */
    private static final String SQL_CREATE_ACCOUNTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ACCOUNTS + " ("   +
            "    _id        INTEGER PRIMARY KEY AUTOINCREMENT,"     +
            "    username   TEXT,"                                  +
            "    nickname   TEXT,"                                  +
            "    password   TEXT,"                                  +
            "    realname   TEXT,"                                  +
            "    host       TEXT,"                                  +
            "    port       INTEGER"                                +
            ")";
    private static final String SQL_DROP_ACCOUNTS =
            "DROP TABLE IF EXISTS " + TABLE_ACCOUNTS;
    private static final String SQL_CREATE_SETTINGS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SETTINGS + " ("   +
            "    key        TEXT PRIMARY KEY,"                      +
            "    value      TEXT"                                   +
            ")";
    private static final String SQL_DROP_SETTINGS =
            "DROP TABLE IF EXISTS " + TABLE_SETTINGS;
    /* Table keys */
    public static final String ID_CURRENT_ACCOUNT = "currentAccountId";
    /* Class attributes */
    private Settings settings;

    /* Database helper */
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        settings = getSettings();
    }

    /* Database instantiation method */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ACCOUNTS);
        db.execSQL(SQL_CREATE_SETTINGS);
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
    private Settings getSettings() {
        SQLiteDatabase db = getReadableDatabase();
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
            row.put("key", key);
            row.put("value", value);
            returnCode = db.insertWithOnConflict(TABLE_SETTINGS, null, row, SQLiteDatabase.CONFLICT_REPLACE);
            settings.setValue(key, value);
            db.close();
        }
        return returnCode;
    }
    public void clearSetting(String key) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SETTINGS, "key = '" + key + "'", null);
        settings.clearValue(key);
        db.close();
    }
    public Account getCurrentAccount() {
        Account account = null;
        // Get the current active account
        long currentAccount = Long.valueOf(getIntegerSetting(ID_CURRENT_ACCOUNT));
        // Check if the key is defined, if not, the user was not logged in
        if (currentAccount > 0) {
            account = getAccount(currentAccount);
        }
        return account;
    }
    private Account getAccount(long id) {
        Account account = null;
        String tuples[] = {"_id", "username", "nickname", "password", "realname", "host", "port"};
        SQLiteDatabase db = getReadableDatabase();
        if (db != null) {
            if (id > 0) {
                // Search for the current session
                Cursor cursor = db.query(TABLE_ACCOUNTS, tuples, "_id = '" + id + "'", null, null, null, null, null);
                account = new Account(cursor);
                cursor.close();
            }
            db.close();
        }
        return account;
    }
    public Account createAccount(Bundle info) {
        Account account = null;
        // Get parameters
        String host = info.getString(MainActivity.SERVER_HOST);
        String port = info.getString(MainActivity.SERVER_PORT);
        String username = info.getString(MainActivity.SERVER_USERNAME);
        String nickname = info.getString(MainActivity.USER_NICKNAME);
        String password = info.getString(MainActivity.USER_PASSWORD);
        String realname = info.getString(MainActivity.USER_REALNAME);
        // Check if activities were performed
        if (host != null && username != null) {
            // Populate the row to insert
            ContentValues row = new ContentValues();
            row.put("host", host);
            row.put("port", port);
            row.put("username", username);
            row.put("nickname", nickname);
            row.put("password", password);
            row.put("realname", realname);
            // Initialize the database connection
            SQLiteDatabase db = getWritableDatabase();
            if (db != null) {
                long userId = db.insert(TABLE_ACCOUNTS, null, row);
                account = getAccount(userId);
                db.close();
            }
        }
        return account;
    }

    /* Called upon logout */
    public void logout() {
        if (this.getSetting(ID_CURRENT_ACCOUNT) != null) {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_SETTINGS, "key = '" + ID_CURRENT_ACCOUNT + "'", null);
            db.close();
        }
    }
}
