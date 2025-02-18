package Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle the notification here
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.sendNotification("Reminder", "Daily Reminder to reflect on your Mental Movies!");

        // Reschedule the alarms
        NotificationScheduler.scheduleNotifications(context);
    }
}