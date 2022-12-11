package it.unipi.di.sam.goshopping;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.ProgressBar;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import it.unipi.di.sam.goshopping.ui.cardlist.CLFragment;
import it.unipi.di.sam.goshopping.ui.shoppinglist.SLFragment;

public class DbAccess extends Activity {

    private static final String DATABASE_NAME = "gs_database.db";
    private static final String SHOPPING_ITEMS_TABLE_NAME = "shopping_items";
    private static final String CARDS_TABLE_NAME = "cards";
    private static final String GEOFENCES_TABLE_NAME = "geofences";
    private static final int DATABASE_VERSION = 2;

    // Database creator
    private static class mSQLiteOH extends SQLiteOpenHelper {

        mSQLiteOH(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }

        public void createTableINE(SQLiteDatabase db, String table_name) {
            Thread T = new Thread(() -> {
                String q;
                switch(table_name) {
                    case SHOPPING_ITEMS_TABLE_NAME:
                        q = "CREATE TABLE IF NOT EXISTS "+SHOPPING_ITEMS_TABLE_NAME+
                                " (_ID INTEGER PRIMARY KEY, item TEXT);";
                        db.execSQL(q);

                        break;
                    case CARDS_TABLE_NAME:
                        q = "CREATE TABLE IF NOT EXISTS "+ CARDS_TABLE_NAME +
                                " (_ID INTEGER PRIMARY KEY, name TEXT, code TEXT, format TEXT, used_times INTEGER, color INTEGER)";
                        db.execSQL(q);
                        break;
                    case GEOFENCES_TABLE_NAME:
                        q = "CREATE TABLE IF NOT EXISTS "+ GEOFENCES_TABLE_NAME +
                                " (_ID INTEGER PRIMARY KEY, place_id TEXT UNIQUE, name TEXT, address TEXT, latitude REAL, longitude REAL, radius REAL, exp_duration INTEGER, loitering_delay INTEGER)";
                        db.execSQL(q);
                        break;
                    default:
                        break;
                }
            });
            T.start();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTableINE(db, SHOPPING_ITEMS_TABLE_NAME);
            createTableINE(db, CARDS_TABLE_NAME);
            createTableINE(db, GEOFENCES_TABLE_NAME);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
    }


    private final mSQLiteOH mOH;
    private final SharedPreferences sharedPreferences;

    public DbAccess(Context context) {
        mOH = new mSQLiteOH(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // making sure all tables still exists
        SQLiteDatabase db = mOH.getWritableDatabase();
        mOH.createTableINE(db, SHOPPING_ITEMS_TABLE_NAME);
        mOH.createTableINE(db, CARDS_TABLE_NAME);
        mOH.createTableINE(db, GEOFENCES_TABLE_NAME);
    }


    // Methods for card list

    public void clQuery() {
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getReadableDatabase();
            Cursor cursor = db.query(CARDS_TABLE_NAME, null, null, null, null, null, getOrderByPreference());
            runOnUiThread(new CLFragment.UpdateCursor(cursor, "set_adapter"));
        });
        T.start();
    }

    public void updateCard(int id, String name, String code, String barcodeFormat, int color, int cardRvPos) {
        Thread T = new Thread(() -> {
            ContentValues val = new ContentValues();
            val.put("name", name);
            val.put("code", code);
            val.put("format", barcodeFormat);
            val.put("color", color);
            SQLiteDatabase db = mOH.getWritableDatabase();
            db.update(CARDS_TABLE_NAME, val, "_ID="+id, null);
            Cursor cursor = db.query(CARDS_TABLE_NAME, null, null, null, null, null, getOrderByPreference());
            runOnUiThread(new CLFragment.UpdateCursor(cursor, "update", cardRvPos));
        });
        T.start();
    }

    public void addCard(String name, String code, String barcodeFormat, int color) {
        Thread T = new Thread(() -> {
            ContentValues val = new ContentValues();
            val.put("name", name);
            val.put("code", code);
            val.put("format", barcodeFormat);
            val.put("used_times", 0);
            val.put("color", color);
            SQLiteDatabase db = mOH.getWritableDatabase();
            db.insert(CARDS_TABLE_NAME, null, val);
            Cursor cursor = db.query(CARDS_TABLE_NAME, null, null, null, null, null, getOrderByPreference());
            runOnUiThread(new CLFragment.UpdateCursor(cursor, "insert"));
        });
        T.start();
    }

    public void incrementCardUsedTimes(int id, int newUsedTimes) {
        Thread T = new Thread(() -> {
            ContentValues val = new ContentValues();
            val.put("used_times", newUsedTimes);
            SQLiteDatabase db = mOH.getWritableDatabase();
            db.update(CARDS_TABLE_NAME, val, "_ID="+id, null);
            Cursor cursor = db.query(CARDS_TABLE_NAME, null, null, null, null, null, getOrderByPreference());
            runOnUiThread(new CLFragment.UpdateCursor(cursor, "increment"));
        });
        T.start();
    }

    public void removeCard(int id, int cardRvPos) {
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getWritableDatabase();
            db.delete(CARDS_TABLE_NAME, "_ID="+id, null);
            Cursor cursor = db.query(CARDS_TABLE_NAME, null, null, null, null, null, getOrderByPreference());
            runOnUiThread(new CLFragment.UpdateCursor(cursor, "remove", cardRvPos));
        });
        T.start();
    }

    // return orderBy statement based on user preference
    private String getOrderByPreference() {
        if(sharedPreferences.getBoolean("order_by_used_times", false))
            return "used_times DESC";
        else return "_ID";
    }


    // Methods for shopping list Items

    public void insertItem (String newItem) {
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getWritableDatabase();
            ContentValues val = new ContentValues();
            val.put("item", newItem);
            db.insert(SHOPPING_ITEMS_TABLE_NAME, null, val);
            Cursor cursor = db.query(SHOPPING_ITEMS_TABLE_NAME, null, null, null, null, null, "_ID");
            runOnUiThread(new SLFragment.UpdateCursor(cursor,"insert"));
        });
        T.start();
    }

    public void getShareableList(Context context) {
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getReadableDatabase();
            Cursor cursor = db.query(SHOPPING_ITEMS_TABLE_NAME, null, null, null, null, null, "_ID");
            StringBuilder strList = new StringBuilder();
            if(cursor.getCount() == 0)
                runOnUiThread(new MainActivity.ShareList(context, null));
            else {
                while (cursor.moveToNext())
                    strList.append(cursor.getString(cursor.getColumnIndexOrThrow("item"))).append("\n");
                runOnUiThread(new MainActivity.ShareList(context, strList.toString()));
            }
            cursor.close();
        });
        T.start();
    }

    public Cursor getTopItems(int maxItems) {
        SQLiteDatabase db = mOH.getReadableDatabase();
        return db.query(SHOPPING_ITEMS_TABLE_NAME, null, null, null, null, null, "_ID", String.valueOf(maxItems));
    }

    public void slQuery() {
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getReadableDatabase();
            Cursor cursor = db.query(SHOPPING_ITEMS_TABLE_NAME, null, null, null, null, null, "_ID");
            runOnUiThread(new SLFragment.UpdateCursor(cursor, "set_adapter"));
        });
        T.start();
    }

    public void updateItem (int id, int itemPosition, String newItemVal) {
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getWritableDatabase();
            ContentValues val = new ContentValues();
            val.put("item", newItemVal);
            db.update(SHOPPING_ITEMS_TABLE_NAME, val, "_ID="+id, null);
            Cursor cursor = db.query(SHOPPING_ITEMS_TABLE_NAME, null, null, null, null, null, "_ID");
            runOnUiThread(new SLFragment.UpdateCursor(cursor,"update", itemPosition));
        });
        T.start();
    }

    public void removeItem (int id, int itemPosition) {
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getWritableDatabase();
            db.delete(SHOPPING_ITEMS_TABLE_NAME,"_ID="+id,null);
            Cursor cursor = db.query(SHOPPING_ITEMS_TABLE_NAME, null, null, null, null, null, "_ID");
            runOnUiThread(new SLFragment.UpdateCursor(cursor,"remove", itemPosition));
        });
        T.start();
    }


    // Methods for geofences

    public void insertGeofence(String placeId, String name, String address, double latitude, double longitude, long expDuration) {
        Thread T = new Thread(() -> {
            ContentValues val = new ContentValues();
            val.put("place_id", placeId);
            val.put("name", name);
            val.put("address", address);
            val.put("latitude", latitude);
            val.put("longitude", longitude);
            val.put("exp_duration", expDuration);
            val.put("radius", Constants.Geofences.RADIUS); // fixed values for now
            val.put("loitering_delay", Constants.Geofences.LOITERING_DELAY); // fixed values for now
            SQLiteDatabase db = mOH.getWritableDatabase();
            db.insert(GEOFENCES_TABLE_NAME, null, val);
        });
        T.start();
    }

    public void removeGeofence(String placeId) {
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getWritableDatabase();
            db.delete(GEOFENCES_TABLE_NAME, "place_id='"+placeId+"'", null);
        });
        T.start();
    }

    public void getGeofences(PreferenceCategory preferenceCategory, Preference newSearch) {
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getReadableDatabase();
            Cursor cursor = db.query(GEOFENCES_TABLE_NAME, null, null, null, null, null, null);
            runOnUiThread(new SettingsActivity.GeofencingFragment.ShowPlaces(preferenceCategory, newSearch, cursor));
        });
        T.start();
    }

    public Cursor getGeofenceCursor() {
        SQLiteDatabase db = mOH.getReadableDatabase();
        return db.query(GEOFENCES_TABLE_NAME, null, null, null, null, null, null);
    }

    public void getGeofenceCount(ProgressBar progressBar) {
        Thread T = new Thread(() -> {
            Cursor cursor = getGeofenceCursor();
            runOnUiThread(new PlaceSearch.SetGeofenceCount(progressBar, cursor.getCount()));
            cursor.close();
        });
        T.start();
    }

}








