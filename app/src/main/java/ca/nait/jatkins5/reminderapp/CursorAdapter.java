package ca.nait.jatkins5.reminderapp;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CursorAdapter extends SimpleCursorAdapter
{
    static final String[] columns = {DBManager.C_ID, DBManager.C_REMINDER_MSG, DBManager.C_REMINDER_DATE};
    static final int[] ids = {R.id.tv_reminder_message, R.id.tv_reminder_date};
    static ArrayList<Reminders> remindersList;

    public CursorAdapter(Context context, Cursor cursor)
    {
        super(context, R.layout.reminder_items, cursor, columns, ids);
    }

    @Override
    public void bindView(View row, Context context, Cursor cursor)
    {
        super.bindView(row, context, cursor);
        String strItem = cursor.getString(cursor.getColumnIndex(DBManager.C_REMINDER_MSG));
        String strDate = cursor.getString(cursor.getColumnIndex(DBManager.C_REMINDER_DATE));
        TextView reminderMSG = row.findViewById(R.id.tv_reminder_message);
        TextView reminderDate = row.findViewById(R.id.tv_reminder_date);
        reminderMSG.setText(strItem);
        reminderDate.setText(strDate);
    }
}
