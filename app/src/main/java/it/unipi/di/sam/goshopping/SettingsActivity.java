package it.unipi.di.sam.goshopping;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;


public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

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
            setTitle(savedInstanceState.getCharSequence(getString(R.string.settings)));
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(getString(R.string.settings), getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) { return true; }
        return super.onSupportNavigateUp();
    }


    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, Preference pref) {
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
            if(themePref == null) return;
            themePref.setOnPreferenceChangeListener((preference, newValue) -> {
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

            Preference infoPreference = findPreference("info_preference");
            assert infoPreference != null;
            infoPreference.setOnPreferenceClickListener(preference -> {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(infoPreference.getContext());
                builder.setTitle(getString(R.string.app_name)+" v"+BuildConfig.VERSION_CODE);
                builder.setMessage(R.string.about_dialog_message);
                builder.setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss());
                builder.setIcon(R.mipmap.app_icon_v1);
                builder.show();
                return false;
            });

        }
    }


    public static class GeofencingFragment extends PreferenceFragmentCompat {

        private static GeofencingClient mGeofencingClient;
        private Preference geofencingSwitch;
        private Preference permissionNA;
        private SharedPreferences sharedPreferences;

        PreferenceCategory activePlaces;
        static Cursor cursor;
        static int count;

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.geofencing_preferences, rootKey);

            if(mGeofencingClient == null)
                mGeofencingClient = LocationServices.getGeofencingClient(requireContext());

            Preference newSearch = findPreference("new_search");
            if(newSearch!=null) {
                newSearch.setOnPreferenceClickListener(preference -> {
                    startActivity(new Intent(getContext(), PlaceSearch.class));
                    return false;
                });
            }

            geofencingSwitch = findPreference("geofencing_switch");
            permissionNA = findPreference("permission_na");
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

            permissionNA.setOnPreferenceClickListener(preference -> {
                if(!hasFinePosPermission() || !hasBgPosPermission()) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
                } else if(!hasNotifPermission())
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_PERMISSIONS_REQUEST_CODE);
                return false;
            });

            activePlaces = findPreference("active_places");

        }

        private boolean hasBgPosPermission() {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
                return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
            else return true;
        }
        private boolean hasFinePosPermission() {
            return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        private boolean hasNotifPermission() {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU)
                return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
            else return true;
        }


        public static class ShowPlaces implements Runnable{
            private final Preference newSearch;
            private final PreferenceCategory activePlaces;
            public ShowPlaces(PreferenceCategory preferenceCategory, Preference newSearch, Cursor placesCursor) {
                this.activePlaces = preferenceCategory;
                this.newSearch = newSearch;
                cursor = placesCursor;
            }
            @Override
            public void run() {
                activePlaces.removeAll();
                count = cursor.getCount();
                activePlaces.setTitle(activePlaces.getContext().getString(R.string.active_places)+" ("+count+")");
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
                        builder.setTitle(R.string.place_remove_confirm_dialog_title);
                        builder.setMessage(builder.getContext().getString(R.string.place_remove_confirm_dialog_message_intro)+" "+name);
                        builder.setPositiveButton(R.string.remove, (dialogInterface, i) -> {
                            preference.setEnabled(false);
                            AppMain.getDb().removeGeofence(placeId);
                            removeGeofenceId(placeId);
                            activePlaces.removePreference(preference);
                            count--;
                            activePlaces.setTitle(activePlaces.getContext().getString(R.string.active_places)+" ("+count+")");
                            if(!newSearch.isEnabled()) newSearch.setEnabled(true); // re-enable new search button
                            dialogInterface.dismiss();
                        }).setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss()).show();
                        return false;
                    });
                    activePlaces.addPreference(place);
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();

            AppMain.getDb().getGeofences(findPreference("active_places"), findPreference("new_search"));

            if(!hasFinePosPermission() || !hasBgPosPermission() || !hasNotifPermission()) {
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
                    .addOnSuccessListener(unused -> Log.d("Geofencing", "Success on removing geofence"))
                    .addOnFailureListener(e -> Log.d("Geofencing_Error", "Failed to removing geofence: "+e.getMessage()));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if(grantResults.length > 0) {
                if(grantResults[0] == PackageManager.PERMISSION_DENIED ) {
                    String message;
                    if(permissions[0].equals(Manifest.permission.POST_NOTIFICATIONS))
                        message = getString(R.string.allow_notification_from_settings_message);
                    else if(permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION))
                        message = getString(R.string.allow_background_position_from_settings_message);
                    else message = getString(R.string.permissions_must_be_allowed_message);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.request_permission_title);
                    builder.setMessage(message);
                    builder.setPositiveButton(R.string.settings, (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        dialog.dismiss();
                    }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).show();

                } else if(grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(R.string.request_permission_title);
                        builder.setMessage(R.string.next_click_allow_always_permission_message);
                        builder.setPositiveButton("Ok", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
                            dialog.dismiss();
                        }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).show();
                    }
                }
            }


        }
    }

}