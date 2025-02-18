package Notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;

public class NotificationScheduler {

    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_FIRST_NOTIFICATION_TIME = "first_notification_time";
    private static final String KEY_SECOND_NOTIFICATION_TIME = "second_notification_time";

    public static void scheduleNotifications(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            // Handle the error (e.g., log or show a message)
            return;
        }

        // Cancel any existing alarms to avoid duplication
        cancelNotifications(context);

        // Intent for the notification
        Intent intent = new Intent(context, NotificationReceiver.class);

        // Unique request codes for each alarm
        int requestCode12PM = 1;
        int requestCode6PM = 2;

        // PendingIntents for 12 PM and 6 PM
        PendingIntent pendingIntent12PM = PendingIntent.getBroadcast(
                context,
                requestCode12PM,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent pendingIntent6PM = PendingIntent.getBroadcast(
                context,
                requestCode6PM,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Get saved notification times from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String firstTime = prefs.getString(KEY_FIRST_NOTIFICATION_TIME, "12:00"); // Default to 12 PM
        String secondTime = prefs.getString(KEY_SECOND_NOTIFICATION_TIME, "18:00"); // Default to 6 PM

        // Parse the saved times into hours and minutes
        String[] firstTimeParts = firstTime.split(":");
        int firstHour = Integer.parseInt(firstTimeParts[0]);
        int firstMinute = Integer.parseInt(firstTimeParts[1]);

        String[] secondTimeParts = secondTime.split(":");
        int secondHour = Integer.parseInt(secondTimeParts[0]);
        int secondMinute = Integer.parseInt(secondTimeParts[1]);

        // Set time for the first notification
        Calendar calendarFirst = Calendar.getInstance();
        calendarFirst.set(Calendar.HOUR_OF_DAY, firstHour);
        calendarFirst.set(Calendar.MINUTE, firstMinute);
        calendarFirst.set(Calendar.SECOND, 0);

        // Set time for the second notification
        Calendar calendarSecond = Calendar.getInstance();
        calendarSecond.set(Calendar.HOUR_OF_DAY, secondHour);
        calendarSecond.set(Calendar.MINUTE, secondMinute);
        calendarSecond.set(Calendar.SECOND, 0);

        // If the time has already passed today, set it for the next day
        if (Calendar.getInstance().after(calendarFirst)) {
            calendarFirst.add(Calendar.DAY_OF_YEAR, 1);
        }
        if (Calendar.getInstance().after(calendarSecond)) {
            calendarSecond.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Schedule repeating alarms
        long interval = AlarmManager.INTERVAL_DAY; // Repeat daily

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Use setExactAndAllowWhileIdle for Android 6.0+
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarFirst.getTimeInMillis(), pendingIntent12PM);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarSecond.getTimeInMillis(), pendingIntent6PM);
        } else {
            // Use setRepeating for older versions
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarFirst.getTimeInMillis(), interval, pendingIntent12PM);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarSecond.getTimeInMillis(), interval, pendingIntent6PM);
        }
    }

    public static void cancelNotifications(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            // Handle the error (e.g., log or show a message)
            return;
        }

        // Cancel existing alarms
        Intent intent = new Intent(context, NotificationReceiver.class);

        int requestCode12PM = 1;
        int requestCode6PM = 2;

        PendingIntent pendingIntent12PM = PendingIntent.getBroadcast(
                context,
                requestCode12PM,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent pendingIntent6PM = PendingIntent.getBroadcast(
                context,
                requestCode6PM,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent12PM);
        alarmManager.cancel(pendingIntent6PM);
    }
}