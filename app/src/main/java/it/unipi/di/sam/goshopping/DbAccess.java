package it.unipi.di.sam.goshopping;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import it.unipi.di.sam.goshopping.ui.cardlist.CLFragment;
import it.unipi.di.sam.goshopping.ui.shoppinglist.SLFragment;

public class DbAccess extends Activity {

    // FIXME: all this values can be private
    public static final String DATABASE_NAME = "gs_database.db";
    public static final String shoppinglist_table_name = "shopping_items";
    public static final String cardlist_table_name = "cards";
    public static final String geofences_table_name = "geofences";
    public static final int DATABASE_VERSION = 2;

    // Database creator
    private static class mSQLiteOH extends SQLiteOpenHelper {

        mSQLiteOH(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }

        public void createTableINE(SQLiteDatabase db, String table_name) {
            Thread T = new Thread(() -> {
                String q = "";
                switch(table_name) {
                    case shoppinglist_table_name:
                        q = "CREATE TABLE IF NOT EXISTS "+shoppinglist_table_name+
                                " (_ID INTEGER PRIMARY KEY, item TEXT);";
                        db.execSQL(q);
                        break;
                    case cardlist_table_name:
                        q = "CREATE TABLE IF NOT EXISTS "+ cardlist_table_name +
                                " (_ID INTEGER PRIMARY KEY, name TEXT, code TEXT, format TEXT, used_times INTEGER)";
                        db.execSQL(q);
                        break;
                    case geofences_table_name:
                        q = "CREATE TABLE IF NOT EXISTS "+ geofences_table_name +
                                " (_ID INTEGER PRIMARY KEY, name TEXT, latitude REAL, longitude REAL, radius REAL, exp_duration INTEGER, loitering_delay INTEGER)";
                        db.execSQL(q);
                        break;
                    default:
                        break;
                };
            });
            T.start();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTableINE(db,shoppinglist_table_name);
            createTableINE(db, cardlist_table_name);
            createTableINE(db, geofences_table_name);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("DB", "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            // TODO: start thread
            // TODO: how to hupdate all tables?
            db.execSQL("DROP TABLE IF EXISTS "+shoppinglist_table_name);
            onCreate(db);
        }
    }


    private mSQLiteOH mOH;

    public DbAccess(Context context) {
        mOH = new mSQLiteOH(context);
        // making sure tables still exists
        SQLiteDatabase db = mOH.getWritableDatabase();
        mOH.createTableINE(db, shoppinglist_table_name);
        mOH.createTableINE(db, cardlist_table_name);
        mOH.createTableINE(db, geofences_table_name);
    }


    // Methods for card list

    public Cursor getCards() {
        // TODO: thread on query?
        SQLiteDatabase db = mOH.getReadableDatabase();
        return db.query(cardlist_table_name, null, null, null, null, null, "used_times");
    }

    public void updateCard(int id, String name, String code, String barcodeFormat, int cardRvPos) {
        ContentValues val = new ContentValues();
        val.put("name", name);
        val.put("code", code);
        val.put("format", barcodeFormat);
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getWritableDatabase();
            db.update(cardlist_table_name, val, "_ID="+id, null);
            runOnUiThread(new CLFragment.RefreshRVOnCardUpdate(cardRvPos));
        });
        T.start();
    }

    public void addCard(String name, String code, String barcodeFormat) {
        ContentValues val = new ContentValues();
        val.put("name", name);
        val.put("code", code);
        val.put("format", barcodeFormat);
        val.put("used_times", 0);
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getWritableDatabase();
            db.insert(cardlist_table_name, null, val);
            runOnUiThread(new CLFragment.RefreshRVOnCardInsert());
        });
        T.start();
    }

    public void removeCard(int id, int cardRvPos) {
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getWritableDatabase();
            db.delete(cardlist_table_name, "_ID="+id, null);
            runOnUiThread(new CLFragment.RefreshRVOnCardRemoved(cardRvPos));
        });
        T.start();
    }


    // Methods for shopping list Items

    public void insertItem (String table, String nullColumnHack, ContentValues val) {
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getWritableDatabase();
            db.insert(table, nullColumnHack, val);
            runOnUiThread(new SLFragment.RefreshRVOnInsert());
        });
        T.start();
    }

    public Cursor query(String table) {
        // TODO: add thread handler
        SQLiteDatabase db = mOH.getReadableDatabase();
        return db.query(shoppinglist_table_name, null, null, null, null, null, "_ID");
    }

    public void updateItem (int id, int adapterPos, String newItemVal) {
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getWritableDatabase();
            ContentValues val = new ContentValues();
            val.put("item", newItemVal);
            db.update(shoppinglist_table_name, val, "_ID="+id, null);
            runOnUiThread(new SLFragment.RefreshRVOnUpdate(adapterPos));
        });
        T.start();
    }

    public void removeItem (int id, int itemPosition) {
        Thread T = new Thread(() -> {
            SQLiteDatabase db = mOH.getWritableDatabase();
            db.delete(shoppinglist_table_name,"_ID="+id,null);
            runOnUiThread(new SLFragment.RefreshRVOnRemoved(itemPosition));
        });
        T.start();
    }


    // Methods for geofences

    public long insertGeofence(String name, double latitude, double longitude) { // FIXME: need other params from GMaps Api
        ContentValues val = new ContentValues();
        val.put("name", name);
        val.put("latitude", latitude);
        val.put("longitude", longitude);
        val.put("radius", Constants.Geofences.RADIUS); // fixed values for now
        val.put("exp_duration", Constants.Geofences.EXPIRATION_DURATION); // fixed values for now
        val.put("loitering_delay", Constants.Geofences.LOITERING_DELAY); // fixed values for now
        // TODO: thread handler
        SQLiteDatabase db = mOH.getWritableDatabase();
        return db.insert(geofences_table_name, null, val);
    }

    public void removeGeofence(int id) { // remove by id or name?
        // TODO: thread handler
        SQLiteDatabase db = mOH.getWritableDatabase();
        db.delete(geofences_table_name, "_ID="+id, null);
    }

    public Cursor getGeofences() {
        // TODO: thread handler
        SQLiteDatabase db = mOH.getReadableDatabase();
        return db.query(geofences_table_name, null, null, null, null, null, null);
    }

}








