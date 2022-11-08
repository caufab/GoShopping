package it.unipi.di.sam.goshopping;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import java.util.Set;

import it.unipi.di.sam.goshopping.R;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";

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
        fragment.setTargetFragment(caller, 0);
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
        }
    }

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


    public static class GeofencingFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.geofencing_preferences, rootKey);
            Preference newSearch = findPreference("new_search");
            Intent intent = new Intent(getContext(), PlaceSearch.class);
            newSearch.setIntent(intent);
            // TODO: on activity result .... if something was done ⟶ update list of active store


            Cursor cursor = MainActivity.db.getGeofences();
            if(cursor.getCount()>=0) {
                PreferenceCategory activePlaces = (PreferenceCategory) findPreference("active_places");

                while(cursor.moveToNext()) {
                    Preference place = new Preference(getContext());
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("_ID"));
                    place.setTitle(name);
                    place.setSummary("Some other description of place");
                    place.setKey(String.valueOf(id)); // meant to be and int
                    place.setPersistent(false);
                    place.setOnPreferenceClickListener(preference -> { // Dialog to confirm delete
                        AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
                        builder.setTitle("Conferma eliminazione");
                        builder.setMessage("Sei sicuro di voler eliminare "+name);
                        builder.setPositiveButton("Elimina", (dialogInterface, i) -> {
                            preference.setEnabled(false);
                            MainActivity.db.removeGeofence(id);
                            activePlaces.removePreference(preference);
                            dialogInterface.dismiss();
                        }).setNegativeButton("Annulla", (dialogInterface, i) -> dialogInterface.dismiss()).show();
                        return false;
                    });

                    activePlaces.addPreference(place);
                }
            }




        }
    }
}