package it.unipi.di.sam.goshopping;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.internal.ApiKey;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.strictmode.FragmentStrictMode;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unipi.di.sam.goshopping.databinding.ActivityMainBinding;
import it.unipi.di.sam.goshopping.ui.cardlist.NewCardActivity;

public class MainActivity extends AppCompatActivity {

    public static DbAccess db;

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        db = new DbAccess(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Utils.createNotificationChannel(this);

        /*
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean b = sharedPreferences.getBoolean("geofencing_switch", false);
        if(b && !checkPermissions()) {
            sharedPreferences.edit().putBoolean("geofencing_switch", false).apply();
            // This only happens if user enabled geofencing and then the app lost necessary permission
            Snackbar.make(this, binding.getRoot(), "Non disponi dei permessi necessari per il geofencing. Vai nelle impostazioni", Snackbar.LENGTH_INDEFINITE)
                .setAction("Vai", v -> {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                }).show();

        }

        */

//      FIXME: remove form comments: core element for fragment navigation

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_shoppinglist, R.id.navigation_ficardlist, R.id.navigation_stats)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);


    }

    private boolean checkPermissions() {
        boolean b = true;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
            b = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return b && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    // FIXME: to be removed (not needed on MainActivity)
    @Override
    public void onStart() {
        super.onStart();

    }


/*
    // TODO: Need to ask for background permission "always on" with link to go
    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            showSnackbar(R.string.permission_rationale, android.R.string.ok, view -> { // Request permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
            });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy sets the permission in
            // a given state or the user denied the permission previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
            // Check if it has been granded with  ActivityCompat.OnRequestPermissionsResultCallback / .onRequestPermissionsResult(int, String[], int[]
            // Check .requestPermission documentation
        }
    }

    /*
    // TODO: use this to request permission on android 30+
    // TODO: background position permission must be requested right after user confirmed

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if(checkPermissions()) {
                if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
                }
            }
        }
    }
*/
    /*
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
*/

    /*
    // Works fine in Nexus 5
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it
        if (mGeofencePendingIntent != null) return mGeofencePendingIntent;
        Intent intent = new Intent(this, GeofenceBR.class);
        return mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
    */

     /*
    @SuppressWarnings("MissingPermission")
    private void addGeofences(List<Geofence> geofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList);
        GeofencingRequest geofencingRequest = builder.build();
        mGeofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                .addOnSuccessListener(this, unused -> { Log.d("geofences", "Success on adding geofences"); })
                .addOnFailureListener(this, e -> { Log.e("geofences", "Failed adding geofences"); e.printStackTrace(); });
    }

    private void removeGeofence(List<String> geofenceList) {
        mGeofencingClient.removeGeofences(geofenceList)
            .addOnSuccessListener(unused -> { Log.d("Geofencing", "Success on removing geofence"); })
            .addOnFailureListener(this, e -> { Log.d("Geofencing", "Failed to removing geofence"); e.printStackTrace(); } );
    }

    private void removeAllGeofences() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
            .addOnSuccessListener(this, unused -> { Log.d("Geofencing", "All geofences removed successfully"); })
            .addOnFailureListener(this, e -> { Log.e("Geofencing", "Failed to remove all geofences at once"); e.printStackTrace(); });
    }
*/

    /*
    // fills the geofence list with all places in places hashmap
    private void populateGeofenceList(Map<String, LatLng> places) {
        // TODO: first check if we need to populate geofences and/or db
        for(Map.Entry<String, LatLng> entry : places.entrySet()) {
            mGeofenceList.add(new Geofence.Builder()
                .setRequestId(entry.getKey())
                .setCircularRegion( entry.getValue().latitude, entry.getValue().longitude, Constants.Geofences.RADIUS )
                .setExpirationDuration(Constants.Geofences.EXPIRATION_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL) // TODO: remove enter
                .setLoiteringDelay(Constants.Geofences.LOITERING_DELAY)
                .build());
        }
    }
    */




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}