package it.unipi.di.sam.goshopping;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class GeofenceBR extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent gEvent = GeofencingEvent.fromIntent(intent);
        if(gEvent == null) return;
        if(gEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(gEvent.getErrorCode());
            Log.e("GeofenceBR", errorMessage);
            return;
        }
        // Get the transition type
        int gTransition = gEvent.getGeofenceTransition();

        // Test that the reported transition was of interest
        if(gTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
            gTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            List<Geofence> triggeringGeofences = gEvent.getTriggeringGeofences();
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
        } else // Log the error
            Log.e("Geofence", "Unknown geofence transition"); // Needed?
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
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
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "enter";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "exit";
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "dwell";
            default:
                return "unknown_transition";
        }
    }


    private int getIntFromReqId(String reqId) {
        switch(reqId) {
            case "campus":
                return 1;
            case "Coop":
                return 2;
            case "Carrefour":
                return 3;
            case "Maurys":
                return 4;
            case "Google":
                return 5;
            case "Campus":
                return 6;
            case "StatuaNormale":
                return 7;
            default:
                return 0;

        }
    }




}
