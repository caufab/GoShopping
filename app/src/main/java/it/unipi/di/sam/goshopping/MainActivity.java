package it.unipi.di.sam.goshopping;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unipi.di.sam.goshopping.databinding.ActivityMainBinding;
import it.unipi.di.sam.goshopping.ui.cardlist.NewCardActivity;

public class MainActivity extends AppCompatActivity {

    public static DbAccess db;

    private ActivityMainBinding binding;

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;


    private HashMap<String, LatLng> places;

    // Tracks whether the user requested to add or remove geofences, or to do neither.
    private enum PendingGeofenceTask {ADD, REMOVE, NONE}


    private GeofencingClient mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        db = new DbAccess(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Utils.createNotificationChannel(this);

        Button startGeo = findViewById(R.id.start_geo);
        Button stopGeo = findViewById(R.id.stop_geo);





        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();
        places = new HashMap<>();
        // Googleplex.
        places.put("Google", new LatLng(37.422611, -122.0840577));
        places.put("Coop", new LatLng(43.71395693, 10.420868));
        places.put("Maurys", new LatLng(43.71611915, 10.423249));
        places.put("Carrefour", new LatLng(43.72589813350207, 10.418184486348556));
        places.put("Campus", new LatLng(43.720165476849175, 10.398976315694702));
        places.put("StatuaNormale", new LatLng(43.71955703507006, 10.400220191167943));

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;
        populateGeofenceList(places);
        mGeofencingClient = LocationServices.getGeofencingClient(this);

        startGeo.setOnClickListener(v -> {
            addGeofences(mGeofenceList);
        });

        stopGeo.setOnClickListener(v -> {
            removeAllGeofences();
        });


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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // TODO: check if needed
                requestPermissions();
            }
    }


    private boolean checkPermissions() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    // TODO: Need to ask for background permission "always on" with link to go
    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            showSnackbar(R.string.permission_rationale, android.R.string.ok, view -> { // Request permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
            });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy sets the permission in
            // a given state or the user denied the permission previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
            // Check if it has been granded with  ActivityCompat.OnRequestPermissionsResultCallback / .onRequestPermissionsResult(int, String[], int[]
            // Check .requestPermission documentation
        }
    }

/*
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
*/

    // Works fine in Nexus 5
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it
        if (mGeofencePendingIntent != null) return mGeofencePendingIntent;
        Intent intent = new Intent(this, GeofenceBR.class);
        return mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

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
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
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