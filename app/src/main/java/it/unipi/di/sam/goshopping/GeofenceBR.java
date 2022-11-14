package it.unipi.di.sam.goshopping;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class GeofenceBR extends BroadcastReceiver {

    private static DbAccess db;
    private static Cursor cursor;
    private boolean found = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(!sharedPreferences.getBoolean("geofencing_switch", false)) {// user disabled geofencing
            Log.e("logging", "shared pref false");
            return;
        }
        GeofencingEvent gEvent = GeofencingEvent.fromIntent(intent);
        if(gEvent == null) return;
        if(gEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(gEvent.getErrorCode());
            Log.e("GeofenceBR", errorMessage);
            return;
        }

        db = new DbAccess(context);
        cursor = db.getGeofences();


        // Get the transition type
        int gTransition = gEvent.getGeofenceTransition();

        // Test that the reported transition was of interest
        if(gTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
            gTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            List<Geofence> triggeringGeofences = gEvent.getTriggeringGeofences();
            for(Geofence geofence : triggeringGeofences) {
                cursor.moveToFirst();
                do {
                    if(geofence.getRequestId().equals(cursor.getString(cursor.getColumnIndexOrThrow("place_id")))) {
                        found = true;
                        if(gTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

                            Cursor shoppingListCursor = db.query("");
                            if(shoppingListCursor.getCount() != 0) {
                                Log.e("logging", "count: "+shoppingListCursor.getCount());
                                String bigText = "";
                                int i = 1;
                                while(shoppingListCursor.moveToNext() && i<=5) {
                                    bigText += shoppingListCursor.getString(shoppingListCursor.getColumnIndexOrThrow("item")) + "\n";
                                    i++;
                                }
                                bigText += "...";
                                Log.e("logging", "bigText: "+bigText);
                                // TODO: make pending intent to launch app
                                Utils.sendNotification(context,
                                        cursor.getInt(cursor.getColumnIndexOrThrow("_ID")),
                                        "Sei presso " + cursor.getString(cursor.getColumnIndexOrThrow("name")) + " ?",
                                        "Ecco i primi 5 elementi della tua lista della spesa:",
                                        bigText,
                                        null);
                            }
                        }
                        else Utils.cancelNotification(context, cursor.getInt(cursor.getColumnIndexOrThrow("_ID")));
                    }
                } while(cursor.moveToNext());

                if(!found) {
                    Log.e("GeofenceBR", "Received a geofence transition but it was not in the database");
                }

            }
            /*
            // Get the transition details as a String
            String geofenceTransitionDetails = getGeofenceTransitionDetails(gTransition,triggeringGeofences);
            Log.i("Geofence", geofenceTransitionDetails); // DEBUG
            ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
            for(Geofence geofence : triggeringGeofences)
                triggeringGeofencesIdsList.add(geofence.getRequestId());
            String triggeringGeofenceIdsString = TextUtils.join(",", triggeringGeofencesIdsList);

            if(getTransitionString(gTransition).equals("exit"))
                Utils.cancelNotification(context, getIntFromReqId(triggeringGeofenceIdsString));
            else if(getTransitionString(gTransition).equals("dwell"))
                Utils.sendNotification(context, getIntFromReqId(triggeringGeofenceIdsString)+10, "Dwell", "Sei da 5 secondi in " + triggeringGeofenceIdsString, null);
            */

        } else // Log the error
            Log.e("Geofence", "Unknown geofence transition"); // Needed?



    }

   /*
    private String getGeofenceTransitionDetails(int geofenceTransition, List<Geofence> triggeringGeofences) {
        String geofenceTransitionString = getTransitionString(geofenceTransition);
        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for(Geofence geofence : triggeringGeofences) 
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        String triggeringGeofenceIdsString = TextUtils.join(",", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofenceIdsString;

    }

    private String getTransitionString(int transitionType) {
        switch(transitionType) {
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "exit";
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "dwell";
            default:
                return "unknown_transition";
        }
    }
*/






}
