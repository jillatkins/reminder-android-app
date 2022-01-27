package ca.nait.jatkins5.reminderapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Date;

import static ca.nait.jatkins5.reminderapp.BaseActivity.manager;
import static ca.nait.jatkins5.reminderapp.BaseActivity.remindersArrayList;

public class AlarmNotifier extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        BaseActivity.database = manager.getWritableDatabase();
        // create new Reminder object, this will store the Reminder Message and Date/Time for the Alarm
        Reminders reminder = new Reminders();
        reminder.setMessage(intent.getStringExtra("Message"));
        reminder.setRemindDate(new Date(intent.getStringExtra("RemindDate")));
        reminder.setID(intent.getIntExtra("id", 0));

        // Set the alarm sound - this will play the user's default notification sound
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        // Set an intent for the Main Activity, FLAG_ACTIVITY_CLEAR_TOP: if the activity being launched is already running in the current task,
        // instead of launching a new instance of that activity, it will close the other activities on top of it and the Intent will be delivered
        // to the old activity as "new intent"
        Intent intent1 = new Intent(context,MainActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // A task is a collection of activities that users interact with
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intent1);

        // Create a new PendingIntent and instantiate it
        // the request code is used to retrieve the same pending intent instance later on
        PendingIntent intent2 = taskStackBuilder.getPendingIntent(1,PendingIntent.FLAG_UPDATE_CURRENT);

        // NotificationCompat: helper for accessing Notification features
        // Builder: Builder class for NotificationCompat... Easier control over flags and helps construct notification layouts
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Set the notification channel - NotificationManager.IMPORTANCE_HIGH is a "higher notification importance". Shows everywhere, makes noise and peeks.
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            channel = new NotificationChannel("my_channel_01","hello", NotificationManager.IMPORTANCE_HIGH);
        }

        // Build the notification, set the Title, the Message, the Alarm sound that was set, the app icon, the pending intent, and the channel id.
        Notification notification = builder.setContentTitle("Reminder")
                .setContentText(intent.getStringExtra("Message")).setAutoCancel(true)
                .setSound(alarmSound).setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(intent2)
                .setChannelId("my_channel_01")
                .build();

        // get the notification service to launch the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            notificationManager.createNotificationChannel(channel);
        }

        // launch the notification
        notificationManager.notify(1, notification);

    }
}
