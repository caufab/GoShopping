package it.unipi.di.sam.goshopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.ViewAnimator;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class PlaceSearch extends AppCompatActivity {

    private Location currentLocation = null;
    private LocationManager locationManager;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final String TAG = PlaceSearch.class.getSimpleName();
    private Handler handler = new Handler();
    private PlacePredictionAdapter adapter = new PlacePredictionAdapter();


    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent = null;

    // Places API support structures
    List<Place.Field> fields;
    FetchPlaceRequest fetchPlaceRequest;
    Task<FetchPlaceResponse> responseTask;



    private ViewAnimator viewAnimator;
    private static ProgressBar progressBar;
    static MenuItem SearchMenuItem;


    final String apiKey = BuildConfig.API_KEY;
    static int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_search);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) { actionBar.setDisplayHomeAsUpEnabled(true); }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Setup Places Client
        if (!Places.isInitialized())
            Places.initialize(getApplicationContext(), apiKey);

        if(geofencingClient == null)
            geofencingClient = LocationServices.getGeofencingClient(this);

        progressBar = findViewById(R.id.progress_bar);
        viewAnimator = findViewById(R.id.view_animator);
        placesClient = Places.createClient(this);
        progressBar.setIndeterminate(true);
        initRecyclerView();
    }

    // Updates geofence count to limit it's increment and stops progressbar
    public static class SetGeofenceCount implements Runnable {
        public SetGeofenceCount(int geofenceCount) { count = geofenceCount; }
        @Override
        public void run() {
            progressBar.setIndeterminate(false);
            SearchMenuItem.setEnabled(true);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!checkPermissions())
            requestPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.place_search_menu, menu);
        SearchMenuItem = menu.findItem(R.id.search);
        SearchMenuItem.setEnabled(false);
        MainActivity.db.getGeofenceCount();
        SearchView searchView = (SearchView) SearchMenuItem.getActionView();
        initSearchView(searchView);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.search) {
            sessionToken = AutocompleteSessionToken.newInstance();
            return false;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    private void initSearchView(SearchView searchView) {
        searchView.setQueryHint(getString(R.string.search_place_hint));
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                progressBar.setIndeterminate(true);

                // Cancel any previous place prediction request
                handler.removeCallbacksAndMessages(null);

                // Start a new place prediction request in 300ms
                handler.postDelayed( () -> {
                    getPlacePredictions(newText);
                    }, 300);
                return false;
            }
        });
    }


    private void initRecyclerView() {
        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
        adapter.setPlaceClickListener(place -> {

            fields = new ArrayList<>();
            fields.add(Place.Field.LAT_LNG); // extracting only LAT_LNG info because other info are included in "place" parameter
            fetchPlaceRequest = FetchPlaceRequest.newInstance(place.getPlaceId(),fields);
            responseTask = placesClient.fetchPlace(fetchPlaceRequest);
            responseTask.addOnSuccessListener(fetchPlaceResponse -> {
                LatLng latLng = fetchPlaceResponse.getPlace().getLatLng();
                if(latLng == null) {
                    Utils.showToast(this,getString(R.string.error_occurred_retry));
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if(count < Constants.Geofences.MAX_GEOFENCES) {
                    builder.setTitle(R.string.add_place_dialog_title);
                    builder.setMessage(place.getPrimaryText(null)+"\n("+place.getSecondaryText(null)+")");
                    builder.setPositiveButton(R.string.add, (dialog, which) -> {
                        addGeofence(place, latLng);
                        count++;
                        dialog.dismiss();
                    }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
                } else {
                    builder.setTitle(R.string.place_limit_reached_dialog_title);
                    builder.setMessage(R.string.place_limit_reached_dialog_message);
                    builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss() );
                }

                builder.show();
            }).addOnFailureListener(exception -> {
                Utils.showToast(this,R.string.error_occurred_retry);
                Log.e("PlaceSearch_Error", "Fetch place error: "+exception.getMessage());
            });
        });
    }

    @SuppressWarnings("MissingPermission") // FIXME: remove when i place if(fine location is granted)
    private void getPlacePredictions(String query) {

        // At this point if users has already granted location permission he will see the distance from
        // each places, otherwise he will see only places name and address
        if(checkPermissions()) {
            // Get last know location, then listen updates. Uses network provider or fused provider in S+ Android version
            String locationProvider = LocationManager.NETWORK_PROVIDER;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                locationProvider = LocationManager.FUSED_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            locationManager.requestLocationUpdates(locationProvider, 0, 0, location -> { currentLocation = location; });
        }

        LatLng origin;
        if(currentLocation != null) origin = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        else origin = null;

        // Create a new programmatic Place Autocomplete request in Places SDK for Android
        final FindAutocompletePredictionsRequest newRequest = FindAutocompletePredictionsRequest
            .builder()
            .setSessionToken(sessionToken)
            .setQuery(query)
            .setCountries("IT")  // check Locale?
            .setOrigin(origin)
            .build();

        // autocomplete prediction request
        placesClient.findAutocompletePredictions(newRequest)
            .addOnSuccessListener(response -> {
                List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                adapter.setPredictions(predictions);
                progressBar.setIndeterminate(false);
                viewAnimator.setDisplayedChild(predictions.isEmpty() ? 0 : 1);
            }).addOnFailureListener(exception -> {
                if(exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e("PlaceSearch_Error", "Place not found: "+apiException.getStatusCode());
                }
            });
    }

    @SuppressWarnings("MissingPermission")
    private void addGeofence(AutocompletePrediction place, LatLng geofenceLatLng) {
        Geofence geofence = buildGeofence(place.getPlaceId(),geofenceLatLng);
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER) // FIXME: this initial trigger must be removed
                .addGeofence(geofence);
        GeofencingRequest geofencingRequest = builder.build();
        if(!checkPermissions())
            requestPermissions();
        else { // user has permission -> add place to geofence client and db
            MainActivity.db.insertGeofence(
                    place.getPlaceId(),
                    String.valueOf(place.getPrimaryText(null)),
                    String.valueOf(place.getSecondaryText(null)),
                    geofenceLatLng.latitude, geofenceLatLng.longitude);
            geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                    .addOnSuccessListener(unused -> {
                        Log.d("Geofences", "Success on adding geofences");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Geofences_Error", "Failed adding geofences");
                        e.printStackTrace();
                    });
        }
    }

    // FLAG_MUTABLE sets a warning as it requires API level 31, yet it works fine in Nexus 5 (API 27)
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it
        if (geofencePendingIntent != null) return geofencePendingIntent;
        Intent intent = new Intent(this, GeofenceBR.class);
        return geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // fills the geofence list with all places in places hashmap
    private Geofence buildGeofence(String placeId, LatLng geofenceLatLng) {
        return new Geofence.Builder()
                .setRequestId(placeId)
                .setCircularRegion(geofenceLatLng.latitude, geofenceLatLng.longitude, Constants.Geofences.RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(Constants.Geofences.LOITERING_DELAY)
                .setNotificationResponsiveness(Constants.Geofences.NOTIFICATION_RESPONSIVENESS)
                .build();
    }


    // Checks if permission has been granted
    private boolean checkPermissions() {
        return (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    // ask for permission only if user denied it before but did not check "Don't ask again" checkbox
    private void requestPermissions() {
        Snackbar.make(this, findViewById(android.R.id.content), getString(R.string.place_search_permission_request), Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.allow), view -> {
                ActivityCompat.requestPermissions(PlaceSearch.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSIONS_REQUEST_CODE);
            }).show();
    }

    // After getting FINE_LOCATION permission sends users to settings to grant BACKGROUND_LOCATION permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSIONS_REQUEST_CODE && Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {
            if(checkPermissions()) {
                if(!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            REQUEST_PERMISSIONS_REQUEST_CODE);
                }
            }
        }
    }


}




