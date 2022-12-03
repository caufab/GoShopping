package it.unipi.di.sam.goshopping;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Utils {

    public static void createNotificationChannel(Context context) {
        // TODO: if context != null
        CharSequence name = context.getString(R.string.notification_channel_name); // TODO: put in string.xml
        String description = context.getString(R.string.notification_channel_description); // TODO: put in string.xml
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(context.getString(R.string.notification_channel_id), name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Sends a new notification to the Notification Manager with a title, text, and the
     * pending intent to launch when users click on it
     */
    @SuppressWarnings("MissingPermission")
    public static void sendNotification(Context context, int notificationId, String title, String smallText, String bigText, PendingIntent pendingIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
                .setSmallIcon(R.mipmap.app_icon_v1)
                .setContentTitle(title)
                .setContentText(smallText)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        builder.setAutoCancel(true);
        notificationManager.notify(notificationId, builder.build());
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.cancel(notificationId);
    }


    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    public static void showToast(Context context, int messageResource) {
        Toast.makeText(context, messageResource, Toast.LENGTH_LONG).show();
    }
}
