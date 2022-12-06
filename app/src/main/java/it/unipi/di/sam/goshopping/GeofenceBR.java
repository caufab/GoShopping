package it.unipi.di.sam.goshopping;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBR extends BroadcastReceiver {

    private boolean found = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(!sharedPreferences.getBoolean("geofencing_switch", false)) { // user disabled geofencing
            return;
        }
        GeofencingEvent gEvent = GeofencingEvent.fromIntent(intent);
        if(gEvent == null) return;
        if(gEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(gEvent.getErrorCode());
            Log.e("GeofenceBR", errorMessage);
            return;
        }

        DbAccess db = new DbAccess(context);
        Cursor cursor = db.getGeofenceCursor();


        // Get the transition type
        int gTransition = gEvent.getGeofenceTransition();

        // Test that the reported transition was of interest
        if(gTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
            gTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            List<Geofence> triggeringGeofences = gEvent.getTriggeringGeofences();
            if(triggeringGeofences == null) return;
            for(Geofence geofence : triggeringGeofences) {
                cursor.moveToFirst();
                do {
                    if(geofence.getRequestId().equals(cursor.getString(cursor.getColumnIndexOrThrow("place_id")))) {
                        found = true;
                        if(gTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                            Cursor shoppingListCursor = db.getTopItems(Constants.NOTIFICATION_MAX_ITEMS);
                            if(shoppingListCursor.getCount() != 0) {
                                StringBuilder itemListStr = new StringBuilder();
                                int i = 1;
                                while(shoppingListCursor.moveToNext() && i<=5) {
                                    itemListStr.append(shoppingListCursor.getString(shoppingListCursor.getColumnIndexOrThrow("item"))).append("\n");
                                    i++;
                                }
                                itemListStr.append("...");
                                Intent notificationIntent = new Intent(context, MainActivity.class);
                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                                stackBuilder.addNextIntentWithParentStack(notificationIntent);
                                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
                                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                Utils.sendNotification(context,
                                    cursor.getInt(cursor.getColumnIndexOrThrow("_ID")),
                                    context.getString(R.string.notification_title),
                                    context.getString(R.string.notification_sub_title),
                                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                                    itemListStr.toString(),
                                    pendingIntent);
                            }
                        }
                        else Utils.cancelNotification(context, cursor.getInt(cursor.getColumnIndexOrThrow("_ID")));
                    }
                } while(cursor.moveToNext());

                if(!found) {
                    Log.e("GeofenceBR_Error", "Received a geofence transition but it was not in the database");
                }
            }
        }
    }
}
