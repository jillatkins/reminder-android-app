package ca.nait.jatkins5.reminderapp;

import java.util.Date;

public class Reminders
{
    public int id;
    String message;
    Date remindDate;

    public Reminders(int id, String message, String remindDate) {}
    public Reminders() {};
    public String getMessage() {return message;}
    public Date getReminderDate() {return remindDate;}
    public int getID() {return id;}
    public void setMessage(String message) {this.message = message;}
    public void setRemindDate(Date remindDate) {this.remindDate = remindDate;}
    public void setID(int id) {this.id = id;}
}
