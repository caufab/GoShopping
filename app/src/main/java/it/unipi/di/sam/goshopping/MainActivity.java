package it.unipi.di.sam.goshopping;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.unipi.di.sam.goshopping.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static DbAccess db;

    private ActivityMainBinding binding;

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;


    private HashMap<String, LatLng> places;
    private long placesRadius = 30;

    // Tracks whether the user requested to add or remove geofences, or to do neither.
    private enum PendingGeofenceTask { ADD, REMOVE, NONE }
     // Provides access to the Geofencing API.
    private GeofencingClient mGeofencingClient;
    // The list of geofences used in this sample.
    private ArrayList<Geofence> mGeofenceList;
    //Used when requesting to add or remove geofences.
    private PendingIntent mGeofencePendingIntent;

    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        db = new DbAccess(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button startGeo = findViewById(R.id.start_geo);
        Button stopGeo = findViewById(R.id.stop_geo);


        startGeo.setOnClickListener(v -> {
            addGeofences();
        });

        stopGeo.setOnClickListener(v -> {
            stopGeofencing();
        });


        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();
        places = new HashMap<>();
        // Googleplex.
        places.put("GOOGLE", new LatLng(37.422611,-122.0840577));
        places.put("COOP", new LatLng(43.71395693,10.420868));
           places.put("Maur", new LatLng(43.71611915,10.423249));
        places.put("Carr", new LatLng(43.72589813350207,10.418184486348556));

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;
        populateGeofenceList();
        mGeofencingClient = LocationServices.getGeofencingClient(this);



/*
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_shoppinglist, R.id.navigation_ficardlist, R.id.navigation_stats)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

*/
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions())
            requestPermissions();
        else
            performPendingGeofenceTask();
    }


    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    // FIXME: on pixel device it's not asking for background position!
    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }



    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        // debug purpose only: use only first element of geofence list
        builder.addGeofence(mGeofenceList.get(0));
        return builder.build();
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it
        if( mGeofencePendingIntent != null) {
            Log.d("Geofencing", "mGeofencePendingIntent not null (reuse)");
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBR.class);
        // using FLAG_UPDATE_CURRENT to get the same pending intent back when calling addGeofences() and removeGeofences()
        // impostare l'OR con un if (SDK>=android S ... ecc), non qui
        mGeofencePendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, aVoid -> { // Geofences added
                    Log.d("geofences", "Success on adding geofences");
                })
                .addOnFailureListener(this, e -> { // Failed to add geofences
                    Log.e("geofences", "Failed adding geofences. Exception message: "+e.getMessage()+ " | cause: "+e.getCause());
                    e.printStackTrace();
                });
    }

    private void stopGeofencing() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(this, unused -> {
                    // Geofences removed
                    Log.d("Geofences", "Geofences removed successfully");
                })
                .addOnFailureListener(this, e -> {
                    // Failed to remove geofences
                    Log.e("Geofences", "Failed to remove geofences");
                });
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    private void performPendingGeofenceTask() {
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            addGeofences();
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            stopGeofencing();
        }
    }


    // fills the geofence list with all places in places hashmap
    private void populateGeofenceList() {
        for(Map.Entry<String, LatLng> entry : places.entrySet()) {
            Log.d("Geofencing", "putting: "+entry.getKey()+" | Lat: "+entry.getValue().latitude+" | Long: "+entry.getValue().longitude);
            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence (a string to identify this geofence)
                    .setRequestId(entry.getKey())
                    // Set circular region of this geofence
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            placesRadius
                    )
                    // Set the expiration duration of the geofence. This geofence gets automatically removed after this period of time.
                    .setExpirationDuration(1000 * 60 * 60) // 1 hour
                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    // Create the geofence
                    .build());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu1:
                // do something
                return true;
            case R.id.menu2:
                // do something
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(mainTextStringId),
                        Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }






}