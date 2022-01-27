package ca.nait.jatkins5.reminderapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DBManager extends SQLiteOpenHelper
{
    static final String TAG = "DBManager";
    static final String DB_NAME = "Reminders.db";
    static final int DB_VERSION = 3;
    static final String TABLE_REMINDERS = "reminders";
    static final String C_ID = BaseColumns._ID;
    static final String C_REMINDER_MSG = "REMINDER_MESSAGE";
    static final String C_REMINDER_DATE = "REMINDER_DATE";

    public DBManager(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(TAG, "in DBManager Constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        String sql = "create table " + TABLE_REMINDERS + " (" + C_ID + " integer primary key AUTOINCREMENT, "
                                                            + C_REMINDER_MSG + " text, " + C_REMINDER_DATE + " text)";
        database.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
        database.execSQL("drop table if exists " + TABLE_REMINDERS);
        Log.d(TAG, "in onUpgrade()");
        onCreate(database);
    }
}
