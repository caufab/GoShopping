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
    //    if(intent == null) Log.e("Geofencing", "onReceive parameter 'intent' is null");
    //    else Log.e("Geofencing", "onReceive parameter 'intent' is NOT null");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if(geofencingEvent == null) Log.e("Geofencing", "geofencingEvent is null");
        else Log.d("Geofencing", "geofencing NOT null!!!");
        if(geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e("GeofenceBR", errorMessage);
            return;
        }
        // Get the transition type
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest
        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Get the geofences that were triggered. A single event can trigger multiple geofences
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            // Get the transition details as a String
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,triggeringGeofences);
            // Send notification and log the transition details
            //   sendNotification(geofenceTransitionDetails);
            Log.i("Geofence", geofenceTransitionDetails);
        } else {
            // Log the error
            Log.e("Geofence", "there was an error in transition");
        //    Log.e("Geofence", getString(R.string.geofence_transition_invalid_type,geofenceTransition));
        }

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
            default:
                return "unknown_transition";
        }
    }







}
