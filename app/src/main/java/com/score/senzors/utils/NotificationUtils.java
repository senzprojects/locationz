package com.score.senzors.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.score.senzors.ui.HomeActivity;
import com.score.senzors.R;

/**
 * Utility class for create and update notifications
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class NotificationUtils {

    // notification Id
    public static final int SERVICE_NOTIFICATION_ID = 1;
    public static final int MESSAGE_NOTIFICATION_ID = 2;

    /**
     * Get notification to create/ update
     * We need to create or update notification in different scenarios
     * @param context context
     * @return notification
     */
    public static Notification getNotification(Context context, int icon, String title, String message) {
        // set up pending intent
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(icon)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent);

        return builder.build();
    }

    /**
     * Create and update notification when query receives from server
     * No we have two notifications regarding Sensor application
     *
     * @param message incoming query
     */
    public static void updateNotification(Context context, String message) {
        Notification notification = getNotification(context, R.drawable.logo_green, context.getString(R.string.new_senz), message);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(MESSAGE_NOTIFICATION_ID, notification);
    }

    /**
     * Cancel notification
     * need to cancel when disconnect from web socket
     */
    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(MESSAGE_NOTIFICATION_ID);
        notificationManager.cancel(SERVICE_NOTIFICATION_ID);
    }

}
