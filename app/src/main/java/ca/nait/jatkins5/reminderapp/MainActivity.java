package ca.nait.jatkins5.reminderapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static ca.nait.jatkins5.reminderapp.DBManager.C_REMINDER_MSG;
import static ca.nait.jatkins5.reminderapp.DBManager.TABLE_REMINDERS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private Dialog dialog;
    ListView lvReminders;
    Cursor cursor;
    CursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BaseActivity.manager = new DBManager(this);

        lvReminders = (ListView) findViewById(R.id.listview_reminders);

        //instantiate and setOnClickListener to FAB
        FloatingActionButton addReminderButton = findViewById(R.id.button_add_reminder);
        addReminderButton.setOnClickListener(this);

        refreshList();

        lvReminders.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Cursor cursor = (Cursor)adapter.getItem(position);
                int reminderID = cursor.getInt(0);
                String message = cursor.getString(1);
                String dateTime = cursor.getString(2);
                EditReminder(reminderID, message, dateTime);
            }
        });

    }

    @Override
    protected void onResume()
    {
        cursor = refreshList();
        adapter = new CursorAdapter(this, cursor);
        lvReminders.setAdapter(adapter);
        super.onResume();
    }

    @Override
    public void onClick(View button)
    {
        switch(button.getId())
        {
            case R.id.button_add_reminder:
            {
                //Toast.makeText(this, "Add new reminder", Toast.LENGTH_SHORT).show();
                addReminder();
                break;
            }
        }
    }

    public void addReminder()
    {
        // Create a new Dialog (pop up) and set the layout/view of the dialog
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.floating_popup);

        // Create and instantiate local variables
        final TextView tvDate = dialog.findViewById(R.id.tv_date);
        Button selectBtn, addBtn;
        selectBtn = dialog.findViewById(R.id.button_selectDate);
        addBtn = dialog.findViewById(R.id.button_add_new);
        final EditText etMessage = dialog.findViewById(R.id.et_message);

        final Calendar newCalendar = Calendar.getInstance();

        // This opens a nested dialog - first the calendar dialog will appear, and then the time dialog
        // Select Date button listener
        selectBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Open DatePickerDialog
                DatePickerDialog dialog = new DatePickerDialog(MainActivity.this,
                                                                new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth)
                    {
                        final Calendar newDate = Calendar.getInstance();
                        Calendar newTime = Calendar.getInstance();
                        TimePickerDialog time = new TimePickerDialog(MainActivity.this,
                                                                        new TimePickerDialog.OnTimeSetListener()
                        {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                            {
                                // Set the DATE *AND* TIME
                                newDate.set(year,month,dayOfMonth,hourOfDay,minute, 0);
                                Calendar tem = Calendar.getInstance();

                                // check if the set time is greater than the current time
                                if(newDate.getTimeInMillis() - tem.getTimeInMillis() > 0)
                                {
                                    tvDate.setText(newDate.getTime().toString());
                                }
                                else
                                {
                                    Toast.makeText(MainActivity.this,"Invalid time",Toast.LENGTH_SHORT).show();
                                }
                            }
                            // get and set the time
                        },newTime.get(Calendar.HOUR_OF_DAY),newTime.get(Calendar.MINUTE), true);
                        // displays the time picker dialog
                        time.show();
                    }
                    // get and set the date
                },newCalendar.get(Calendar.YEAR),newCalendar.get(Calendar.MONTH),newCalendar.get(Calendar.DAY_OF_MONTH));

                // disallow past dates to be picked
                dialog.getDatePicker().setMinDate(System.currentTimeMillis());

                // display the dialog
                dialog.show();
            }
        });

        // ADD (save) BUTTON
        addBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Added some validation to ensure that a proper date/time/message is input before adding the Reminder
                String theMessage = etMessage.getText().toString();

                if (tvDate.getText().toString() == "" || tvDate.getText().toString() == "Date And Time")
                {
                    Toast.makeText(MainActivity.this, "Please enter a date and time.", Toast.LENGTH_LONG).show();
                }

                else if(theMessage.equals(""))
                {
                    Toast.makeText(MainActivity.this, "Please enter a reminder message.", Toast.LENGTH_LONG).show();
                }

                else
                {
                    // get the database and create a new Reminders object
                    BaseActivity.database = BaseActivity.manager.getWritableDatabase();
                    Reminders reminder = new Reminders();
                    reminder.setMessage(etMessage.getText().toString().trim());
                    Date remindTime = new Date(tvDate.getText().toString().trim());
                    reminder.setRemindDate(remindTime);

                    ContentValues values = new ContentValues();
                    values.put(DBManager.C_REMINDER_MSG, reminder.message);
                    values.put(DBManager.C_REMINDER_DATE, String.valueOf(reminder.remindDate));

                    try
                    {
                        // Insert into database & repopulate the listview
                        BaseActivity.database.insertOrThrow(TABLE_REMINDERS, null, values);
                        cursor.requery();
                    }
                    catch (SQLException e)
                    {
                        Toast.makeText(MainActivity.this, "ERROR: " + e, Toast.LENGTH_LONG).show();
                    }
                    // get a new instance of the calendar
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(remindTime);

                    // set the intent
                    // Intent: abstract description of an operation to be performed. Will be used with a Broadcast Receiver
                    Intent intent = new Intent(MainActivity.this, AlarmNotifier.class);
                    // copying the elements to be sent to the Notification (broadcast)
                    intent.putExtra("Message", reminder.getMessage());
                    intent.putExtra("RemindDate", reminder.getReminderDate().toString());
                    intent.putExtra("id", reminder.getID());
                    //Create a Pending Intent, FLAG_UPDATE_CURRENT keeps the intent and updates it when the extras change
                    // The returned object of a Pending Intent (in this case, getBroadcast) can be handed to other applications so they
                    // can perform the action described at a later time
                    PendingIntent intent1 = PendingIntent.getBroadcast(MainActivity.this, reminder.getID(),
                                                                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    // Set a new AlarmManager variable. The AlarmManager class provides access to the system alarm services.
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    // this sets the alarm time and will wake up the device upon that time and launch the intent
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent1);

                    Toast.makeText(MainActivity.this, "Reminder Added!", Toast.LENGTH_SHORT).show();
                    // close the dialog
                    dialog.dismiss();
                }
            }
        });
        // show the dialog
        dialog.show();
    }

    public void EditReminder(final int reminderID, String message, String dateTime)
    {
        // THIS IS SIMILAR TO THE AddReminder() METHOD, WITH SOME CHANGES

        //Toast.makeText(MainActivity.this,"EditReminder!", Toast.LENGTH_SHORT).show();

        // Create a new Dialog (pop up) and set the layout/view
        dialog = new Dialog(MainActivity.this);
        // New layout that includes different buttons
        dialog.setContentView(R.layout.floating_edit_popup);

        // Create and instantiate local variables
        final TextView tvDate = dialog.findViewById(R.id.tv_date);
        Button selectBtn, updateBtn, deleteBtn;
        selectBtn = dialog.findViewById(R.id.button_selectDate);
        updateBtn = dialog.findViewById(R.id.button_update);
        deleteBtn = dialog.findViewById(R.id.button_delete);
        final EditText etMessage = dialog.findViewById(R.id.et_message);

        final Calendar newCalendar = Calendar.getInstance();

        // Since there is a set date/time and message already, display those in the pop-up
        tvDate.setText(dateTime);
        etMessage.setText(message);

        // Select Date button listener
        selectBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Open DatePickerDialog
                DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth)
                    {
                        final Calendar newDate = Calendar.getInstance();
                        Calendar newTime = Calendar.getInstance();
                        TimePickerDialog time = new TimePickerDialog(MainActivity.this,
                                                                        new TimePickerDialog.OnTimeSetListener()
                        {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                            {
                                newDate.set(year,month,dayOfMonth,hourOfDay,minute, 0);
                                Calendar tem = Calendar.getInstance();

                                if(newDate.getTimeInMillis() - tem.getTimeInMillis() > 0)
                                {
                                    tvDate.setText(newDate.getTime().toString());
                                }
                                else
                                {
                                    Toast.makeText(MainActivity.this,"Invalid time",Toast.LENGTH_SHORT).show();
                                }
                            }
                        },newTime.get(Calendar.HOUR_OF_DAY),newTime.get(Calendar.MINUTE), true);
                        time.show();
                    }
                },newCalendar.get(Calendar.YEAR),newCalendar.get(Calendar.MONTH),newCalendar.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMinDate(System.currentTimeMillis());
                dialog.show();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BaseActivity.database = BaseActivity.manager.getWritableDatabase();
                Reminders reminder = new Reminders();
                reminder.setMessage(etMessage.getText().toString().trim());
                Date remindTime = new Date(tvDate.getText().toString().trim());
                reminder.setRemindDate(remindTime);

                ContentValues values = new ContentValues();
                values.put(DBManager.C_ID, reminderID);
                values.put(DBManager.C_REMINDER_MSG, reminder.message);
                values.put(DBManager.C_REMINDER_DATE, String.valueOf(reminder.remindDate));

                try
                {
                    // REPLACE the current database record with an updated version.
                    // The database will find the record based on the primary key (C_ID)
                    BaseActivity.database.replace(TABLE_REMINDERS, null, values);
                    cursor.requery();
                }
                catch(SQLException e)
                {
                    Toast.makeText(MainActivity.this, "ERROR: " + e, Toast.LENGTH_LONG).show();
                }


                Calendar calendar = Calendar.getInstance();
                calendar.setTime(remindTime);

                Intent intent = new Intent(MainActivity.this,AlarmNotifier.class);
                intent.putExtra("Message",reminder.getMessage());
                intent.putExtra("RemindDate",reminder.getReminderDate().toString());
                intent.putExtra("id",reminder.getID());
                PendingIntent intent1 = PendingIntent.getBroadcast(MainActivity.this,
                                                                    reminder.getID(),intent,PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),intent1);

                Toast.makeText(MainActivity.this,"Reminder Updated!",Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BaseActivity.database = BaseActivity.manager.getWritableDatabase();

                try
                {
                    BaseActivity.database.delete(TABLE_REMINDERS, DBManager.C_ID + "=" + reminderID, null);
                    cursor.requery();
                }
                catch(SQLException e)
                {
                    Toast.makeText(MainActivity.this, "ERROR: " + e, Toast.LENGTH_LONG).show();
                }

                Toast.makeText(MainActivity.this,"Reminder Deleted!",Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public Cursor refreshList()
    {
        BaseActivity.remindersArrayList = new ArrayList<Reminders>();
        BaseActivity.database = BaseActivity.manager.getReadableDatabase();

        Cursor cursor = BaseActivity.database.query(TABLE_REMINDERS, null, null,
                                            null, null, null, null);
        startManagingCursor(cursor);
        String tempMsg, tempDate, output;
        int id;

        while(cursor.moveToNext())
        {
            id = cursor.getInt(cursor.getColumnIndex(DBManager.C_ID));
            tempMsg = cursor.getString(cursor.getColumnIndex(DBManager.C_REMINDER_MSG));
            tempDate = cursor.getString(cursor.getColumnIndex(DBManager.C_REMINDER_DATE));

            Reminders item = new Reminders(id, tempMsg, tempDate);
            BaseActivity.remindersArrayList.add(item);
        }

        return cursor;
    }

}