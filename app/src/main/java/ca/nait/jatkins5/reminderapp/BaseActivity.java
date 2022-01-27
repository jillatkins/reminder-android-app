package ca.nait.jatkins5.reminderapp;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity
{
    static Intent myIntent;
    static AlarmManager alarmManager;
    static PendingIntent pendingIntent;
    static NotificationManager nfManager;
    static DBManager manager;
    static SQLiteDatabase database;
    static ArrayList<Reminders> remindersArrayList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
}
