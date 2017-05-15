package com.horchat.horchat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import com.horchat.horchat.activity.MainActivity;
import com.horchat.horchat.model.Account;
import com.horchat.horchat.model.Conversation;
import com.horchat.horchat.model.Server;
import com.horchat.horchat.model.Session;
import com.horchat.horchat.model.Settings;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String ID = "horchat";
    /* Database configuration */
    private static final String DB_NAME              = "horchat";
    private static final int DB_VERSION              = 2;
    /* Table names */
    private static final String TABLE_SESSIONS       = "sessions";
    private static final String TABLE_SETTINGS       = "settings";
    private static final String TABLE_CONVERSATIONS  = "conversations";
    /* Strings for creating and dropping databases */
    private static final String SQL_CREATE_SESSIONS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SESSIONS + " ("      +
            "    username   TEXT    NOT NULL,"                         +
            "    realName   TEXT    NOT NULL,"                         +
            "    nickname   TEXT    NOT NULL,"                         +
            "    host       TEXT    NOT NULL,"                         +
            "    port       INTEGER NOT NULL,"                         +
            "    password   TEXT,"                                     +
            "    PRIMARY KEY (username, host)"                         +
            ")";
    private static final String SQL_DROP_SESSIONS =
            "DROP TABLE IF EXISTS " + TABLE_SESSIONS;
    private static final String SQL_CREATE_SETTINGS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SETTINGS + " ("      +
            "    sid        INTEGER NOT NULL,"                         +
            "    key        TEXT,"                                     +
            "    value      TEXT,"                                     +
            "    PRIMARY KEY (sid, key)"                               +
            ")";
    private static final String SQL_DROP_SETTINGS =
            "DROP TABLE IF EXISTS " + TABLE_SETTINGS;

    private static final String SQL_CREATE_CONVERSATIONS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_CONVERSATIONS + " (" +
            "    sid        INTEGER NOT NULL,"                         +
            "    name       TEXT,"                                     +
            "    data       TEXT,"                                     +
            "    type       INTEGER NOT NULL,"                         +
            "    PRIMARY KEY (sid, name, type)"                        +
            ")";
    private static final String SQL_DROP_CONVERSATIONS =
            "DROP TABLE IF EXISTS " + TABLE_CONVERSATIONS;
    /* Table keys */
    public static final String ID_CURRENT_SESSION = "currentSessionId";
    public static final String LAST_CONVERSATION_NAME = "lastConversationName";
    /* Class attributes */
    private Settings mSettings;
    private long mSessionId;

    /* Database helper */
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mSessionId = getCurrentSessionId();
        mSettings = getSettings();
    }

    /* Database instantiation method */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SESSIONS);
        db.execSQL(SQL_CREATE_SETTINGS);
        db.execSQL(SQL_CREATE_CONVERSATIONS);
    }

    /* Database upgrade method */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /* TODO: Implement proper upgrade method */
        db.execSQL(SQL_DROP_SESSIONS);
        db.execSQL(SQL_DROP_SETTINGS);
        db.execSQL(SQL_DROP_CONVERSATIONS);
        onCreate(db);
    }

    /* Helper methods */
    private long getCurrentSessionId() {
        long sessionId = 0;
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = { "value" };
        String whereClause = "key = ?";
        String[] whereArgs = { ID_CURRENT_SESSION };
        Cursor cursor = db.query(TABLE_SETTINGS, columns, whereClause, whereArgs, null, null, null,
                null);
        if (cursor.moveToNext() && cursor.getColumnCount() >= 1) {
            try {
                sessionId = Integer.parseInt(cursor.getString(0));
            } catch(Exception e) {
                // Nothing to be done
            }
        }
        cursor.close();
        db.close();
        return sessionId;
    }
    private Settings getSettings() {
        SQLiteDatabase db = getReadableDatabase();
        String tuples[] = {"key", "value"};
        String whereClause = "sid = ?";
        String[] whereArgs = { String.valueOf(mSessionId) };
        Cursor cursor = db.query(TABLE_SETTINGS, tuples, whereClause, whereArgs, null, null, null,
                null);
        Settings settings = new Settings(cursor);
        cursor.close();
        db.close();
        return settings;
    }
    public String getSetting(String key) {
        String value = null;
        if (mSettings != null) {
            value = mSettings.getValue(key);
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
            long sessionId = mSessionId;
            if (key.equals(ID_CURRENT_SESSION)) {
                /* Force it to be unique for all sessions */
                sessionId = 0;
            }
            row.put("sid", sessionId);
            row.put("key", key);
            row.put("value", value);
            returnCode = db.insertWithOnConflict(TABLE_SETTINGS, null, row,
                    SQLiteDatabase.CONFLICT_REPLACE);
            mSettings.setValue(key, value);
            db.close();
        }
        return returnCode;
    }
    public void clearSetting(String key) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SETTINGS, "key = '" + key + "'", null);
        mSettings.clearValue(key);
        db.close();
    }
    public Session getCurrentSession() {
        Session session = null;
        // Check if the key is defined, if not, the user was not logged in
        if (mSessionId > 0) {
            Log.d(ID, "Found current session");
            session = getSession(mSessionId);
        } else {
            Log.d(ID, "Could not find any active session");
        }
        return session;
    }
    private Session getSession(long id) {
        Session session = null;
        String tuples[] = {"rowid", "username", "realName", "nickname", "host", "port", "password"};
        SQLiteDatabase db = getReadableDatabase();
        Log.d(ID, "checking db");
        if (db != null) {
            Log.d(ID, "db is not null");
            if (id > 0) {
                Log.d(ID, "Creating Session object");
                // Search for the current session
                Cursor cursor = db.query(TABLE_SESSIONS, tuples, "rowid = '" + id + "'",
                        null, null, null, null, null);
                session = new Session(cursor);
                cursor.close();
            }
            db.close();
        }
        return session;
    }
    public Session createSession(Bundle info) {
        Session session = null;
        // Get parameters
        Account account = (Account) info.getSerializable(MainActivity.ACCOUNT);
        Server server = (Server) info.getSerializable(MainActivity.SERVER);
        // Check if activities were performed
        if (account != null && server != null) {
            // Populate the row to insert
            ContentValues row = new ContentValues();
            row.put("username", account.getUsername());
            row.put("realName", account.getRealName());
            row.put("nickname", account.getNickname());
            row.put("host",     server.getHost());
            row.put("port",     server.getPort());
            row.put("password", server.getPassword());
            // Initialize the database connection
            SQLiteDatabase db = getWritableDatabase();
            if (db != null) {
                Log.d(ID, "Inserting session to db");
                long userId = db.insert(TABLE_SESSIONS, null, row);
                Log.d(ID, "value of id: " + userId);
                session = getSession(userId);
                db.close();
            }
        } else {
            Log.d(ID, "Null value detected in " +
                    ((account == null && server != null) ? "'account'" : "") +
                    ((server == null && account != null) ? "'server'" : "") +
                    ((account == null && server == null) ? "both 'account' and 'server'" : ""));
        }
        if (session != null) {
            Log.d(ID, "Setting current session");
            // Save in settings
            setSetting(ID_CURRENT_SESSION, String.valueOf(session.getId()));
        } else {
            Log.d(ID, "Object 'session' is null");
        }
        return session;
    }

    /* Called upon logout */
    public void logout() {
        if (this.getSetting(ID_CURRENT_SESSION) != null) {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_SETTINGS, "key = '" + ID_CURRENT_SESSION + "'", null);
            db.close();
        }
    }

    /* New conversation */
    public void newConversation(Conversation conversation, long sessionId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues row = new ContentValues();
        row.put("sid", sessionId);
        row.put("name", conversation.getName());
        // TODO: Add data to conversation
        row.put("data", (String) null);
        row.put("type", conversation.getType());
        db.insert(TABLE_CONVERSATIONS, null, row);
    }

    /* Get conversations */
    public List<Conversation> getConversations(long sessionId) {
        List<Conversation> conversations = new ArrayList<Conversation>();
        String tuples[] = {"name", "data", "type"};
        String whereClause = "sid = ?";
        String whereArgs[] = { String.valueOf(sessionId) };
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONVERSATIONS, tuples, whereClause, whereArgs, null, null,
                null, null);
        if (cursor != null) {
            while (cursor.moveToNext() && cursor.getColumnCount() >= 3) {
                conversations.add(new Conversation(cursor.getString(0), cursor.getInt(2)));
            }
        }
        cursor.close();
        return conversations;
    }
}
