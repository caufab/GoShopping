package it.unipi.di.sam.goshopping;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.DialogPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import it.unipi.di.sam.goshopping.R;

public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {


    private static final String TITLE_TAG = "Impostazioni";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                setTitle(R.string.title_activity_settings);
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) { actionBar.setDisplayHomeAsUpEnabled(true); }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) { return true; }
        return super.onSupportNavigateUp();
    }


    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }


    public static class HeaderFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey);

            ListPreference themePref = findPreference("theme_preference");
            themePref.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.e("logging", "new value: "+newValue.toString());

                switch (newValue.toString()) {
                    case "light":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case "dark":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    case "systems":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                }

                return true;
            });

        }
    }

    /*
    public static class MessagesFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.messages_preferences, rootKey);
        }
    }

    public static class SyncFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.sync_preferences, rootKey);
        }
    }
    */

    // FIXME: try to extract this class into a separate file
    public static class GeofencingFragment extends PreferenceFragmentCompat {


        private static GeofencingClient mGeofencingClient;
        private Preference geofencingSwitch;
        private static Preference newSearch;
        private Preference permissionNA;
        private SharedPreferences sharedPreferences;

        static PreferenceCategory activePlaces;
        static Cursor cursor;
        static int count;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.geofencing_preferences, rootKey);

            if(mGeofencingClient == null)
                mGeofencingClient = LocationServices.getGeofencingClient(getContext());

            newSearch = findPreference("new_search");
            newSearch.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), PlaceSearch.class));
                return false;
            });

            geofencingSwitch = findPreference("geofencing_switch");
            permissionNA = findPreference("permission_na");
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            permissionNA.setOnPreferenceClickListener(preference -> {
                if(!checkFinePosPermission() || !checkBgPosPermission()) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
                } else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU && !checkNotifPermission())
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_PERMISSIONS_REQUEST_CODE);
                return false;
            });

            activePlaces = (PreferenceCategory) findPreference("active_places");

        }

        private boolean checkBgPosPermission() {
            boolean b = true;
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
                b = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
            return b;
        }
        private boolean checkFinePosPermission() {
            return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        private boolean checkNotifPermission() {
            return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }


        public static class ShowPlaces implements Runnable{
            public ShowPlaces(Cursor placesCursor) { cursor = placesCursor; }
            @Override
            public void run() {
                activePlaces.removeAll();
                count = cursor.getCount();
                activePlaces.setTitle("Luoghi attivi ("+count+")");
                newSearch.setEnabled(count < Constants.Geofences.MAX_GEOFENCES); // enable new search button only if active geofences are < 10
                while(cursor.moveToNext()) {
                    Preference place = new Preference(activePlaces.getContext());
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String address = cursor.getString((cursor.getColumnIndexOrThrow("address")));
                    String placeId = cursor.getString(cursor.getColumnIndexOrThrow("place_id"));
                    place.setTitle(name);
                    place.setSummary(address);
                    place.setIconSpaceReserved(false);
                    place.setKey(String.valueOf(placeId)); // meant to be and int
                    place.setPersistent(false);
                    place.setOnPreferenceClickListener(preference -> { // Dialog to confirm delete
                        AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
                        builder.setTitle("Conferma eliminazione");
                        builder.setMessage("Sei sicuro di voler eliminare "+name);
                        builder.setPositiveButton("Elimina", (dialogInterface, i) -> {
                            preference.setEnabled(false);
                            MainActivity.db.removeGeofence(placeId);
                            removeGeofenceId(placeId);
                            activePlaces.removePreference(preference);
                            count--;
                            activePlaces.setTitle("Luoghi attivi ("+count+")");
                            if(!newSearch.isEnabled()) newSearch.setEnabled(true); // re-enable new search button
                            dialogInterface.dismiss();
                        }).setNegativeButton("Annulla", (dialogInterface, i) -> dialogInterface.dismiss()).show();
                        return false;
                    });
                    activePlaces.addPreference(place);
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (MainActivity.db == null) {
                startActivity(new Intent(getContext(), MainActivity.class));
                return;
            }
            MainActivity.db.getGeofences();

            if(!checkFinePosPermission() || !checkBgPosPermission() || !checkNotifPermission()) {
                sharedPreferences.edit().putBoolean("geofencing_switch", false).apply();
                geofencingSwitch.setDefaultValue(false);
                geofencingSwitch.setEnabled(false);
                permissionNA.setVisible(true);
            } else {
                permissionNA.setVisible(false);
                geofencingSwitch.setEnabled(true);
            }

        }


        private static void removeGeofenceId(String geofenceId) {
            List<String> geofenceIdList = new ArrayList<>();
            geofenceIdList.add(geofenceId);
            mGeofencingClient.removeGeofences(geofenceIdList)
                    .addOnSuccessListener(unused -> { Log.d("Geofencing", "Success on removing geofence"); })
                    .addOnFailureListener(e -> { Log.d("Geofencing", "Failed to removing geofence"); e.printStackTrace(); } );
        }



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if(grantResults.length > 0) {
                if(grantResults[0] == PackageManager.PERMISSION_DENIED ) {
                    String message = "";
                    if(permissions[0].equals(Manifest.permission.POST_NOTIFICATIONS))
                        message = "Vai su impostazioni e abilita la ricezione delle notifiche";
                    else if(permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION))
                        message = "Vai su impostazioni, clicca su Autorizzazioni -> Posizione e scegli l'opzione 'Consenti sempre'";
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Autorizzazione richiesta");
                    builder.setMessage(message);
                    builder.setPositiveButton("Impostazioni", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        dialog.dismiss();
                    }).setNegativeButton("Annulla", (dialog, which) -> {
                        dialog.dismiss();
                    }).show();

                } else if(grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                //        !(permissions[0].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) || permissions[0].equals(Manifest.permission.POST_NOTIFICATIONS))) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Autorizzazione richiesta");
                        builder.setMessage("Nella schermata successiva clicca su 'Consenti sempre'");
                        builder.setPositiveButton("Ok", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
                            dialog.dismiss();
                        }).setNegativeButton("Annulla", (dialog, which) -> {
                            dialog.dismiss();
                        }).show();
                    }
                }
            }


        }
    }






}