package it.unipi.di.sam.goshopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.ViewAnimator;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PlaceSearch extends AppCompatActivity {

    private static final String TAG = PlaceSearch.class.getSimpleName();
    private Handler handler = new Handler();
    private PlacePredictionAdapter adapter = new PlacePredictionAdapter();
//    private Gson gson = new GsonBuilder().registerTypeAdapter(LatLng.class, new LatLongAdapter()).create();

    private RequestQueue queue;
    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;

    private ViewAnimator viewAnimator;
    private ProgressBar progressBar;

    final String apiKey = BuildConfig.API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_search);
    //    setSupportActionBar(findViewById(R.id.toolbar));

        // Setup Places Client
        if (!Places.isInitialized())
            Places.initialize(getApplicationContext(), apiKey);

        // Initialize members
        progressBar = findViewById(R.id.progress_bar);
        viewAnimator = findViewById(R.id.view_animator);
        placesClient = Places.createClient(this);
        queue = Volley.newRequestQueue(this);
        initRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.place_search_menu, menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
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

    private void initSearchView(SearchView searchView) {
        searchView.setQueryHint("Cerca un luogo");
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
        //adapter.setPlaceClickListener(this::geocodePlaceAndDisplay);
        adapter.setPlaceClickListener(place -> {
            Log.d("Places", "place.getPlaceId(): "+place.getPlaceId());
            Log.d("Places", "place.getDistanceMeters(): "+place.getDistanceMeters());
            Log.d("Places", "place.getPlaceTypes(): "+place.getPlaceTypes());
            Log.d("Places", "place.describeContents(): "+place.describeContents());
            Log.d("Places", "place: "+place);

        });
    }


    private void getPlacePredictions(String query) {
        final LocationBias bias = RectangularBounds.newInstance(
            new LatLng(22.458744, 88.208162), // SW lat, lng
            new LatLng(22.730671, 88.524896) // NE lat, lng
        );


        // Create a new programmatic Place Autocomplete request in Places SDK for Android
        final FindAutocompletePredictionsRequest newRequest = FindAutocompletePredictionsRequest
            .builder()
            .setSessionToken(sessionToken)
            .setLocationBias(bias)
            .setTypeFilter(TypeFilter.ESTABLISHMENT)
            .setQuery(query)
            .setCountries("IN")
            .build();

        // Perform autocomplete prediction request
        placesClient.findAutocompletePredictions(newRequest)
            .addOnSuccessListener(response -> {
                List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                adapter.setPredictions(predictions);

                progressBar.setIndeterminate(false);
                viewAnimator.setDisplayedChild(predictions.isEmpty() ? 0 : 1);
            }).addOnFailureListener(exception -> {
                if(exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: "+apiException.getStatusCode());
                }
            });
    }

/*
    private void geocodePlaceAndDisplay(AutocompletePrediction placePrediction) {
        // Construct the request URL

        final String url = "https://maps.googleapis.com/maps/api/geocode/json?place_id=%s&key=%s";
        final String requestURL = String.format(url, placePrediction.getPlaceId(), apiKey);

        // Use the HTTP request URL for Geocoding API to get geographic coordinates for the place
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Inspect the value of "results" and make sure it's not empty
                            JSONArray results = response.getJSONArray("results");
                            if(results.length() == 0) {
                                Log.w(TAG, "No results from geocoding request");
                                return;
                            }
                            GeocodingResult result = gson.fromJson(result.getString(0), GeocodingResult.class);
                            displayDialog(placePrediction, result);
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                }, error -> Log.e(TAG, "Request failed"));

    }

    private void displayDialog(AutocompletePrediction place, GeocodingResult result) {
        new AlertDialog.Builder(this)
                .setTitle(place.getPrimaryText(null))
                .setMessage("Geocoding result:\n" + result.geometry.location)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
*/



}