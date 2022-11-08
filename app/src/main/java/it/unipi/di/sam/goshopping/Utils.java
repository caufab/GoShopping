package it.unipi.di.sam.goshopping;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Utils {

    static String CHANNEL_ID = "Channel_ID"; // TODO: put in string.xml ?

    // create notification channel ( API 26+ ) called from onCreate?
    public static void createNotificationChannel(Context context) {
        // TODO: if context != null
        // notificationId = 1;
        CharSequence name = "ChannelName"; // TODO: put in string.xml
        String description = "ChannelDescription"; // TODO: put in string.xml
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Sends a new notification to the Notification Manager with a title, text, and the
     * pending intent to launch when users click on it
     */
    public static void sendNotification(Context context, int notificationId, String title, String text, PendingIntent pendingIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_dashboard_black_24dp)
                .setContentText(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("LongerText "+text)); // TODO: parameter if needed
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.cancel(notificationId);
    }


}
