package it.unipi.di.sam.goshopping;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.PermissionChecker;

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
    @SuppressWarnings("MissingPermission")
    public static void sendNotification(Context context, int notificationId, String title, String smallText, String bigText, PendingIntent pendingIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_dashboard_black_24dp)
                .setContentTitle(title)
                .setContentText(smallText)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText)); // TODO: parameter if needed
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        builder.setAutoCancel(true);
        notificationManager.notify(notificationId, builder.build());
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.cancel(notificationId);
    }


    public static void showToast(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }
}
