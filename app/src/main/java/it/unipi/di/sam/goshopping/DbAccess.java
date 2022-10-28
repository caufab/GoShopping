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

import it.unipi.di.sam.goshopping.ui.shoppinglist.SLFragment;

public class DbAccess extends Activity {

    // run on background thread!


    public static final String DATABASE_NAME = "gs_database.db";
    public static final String shoppinglist_table_name = "shopping_items";
    public static final String ficardlist_table_name = "cards";
    public static final int DATABASE_VERSION = 2;




     private static class mSQLiteOH extends SQLiteOpenHelper {

         mSQLiteOH(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }

         public void createTableINE(SQLiteDatabase db, String table_name) {
            String q;
            switch(table_name) {
                case shoppinglist_table_name:
                    q = "CREATE TABLE IF NOT EXISTS "+shoppinglist_table_name+
                            " (_ID INTEGER PRIMARY KEY, item TEXT);";
                    break;
                case ficardlist_table_name:
                    q = "CREATE TABLE IF NOT EXISTS "+ ficardlist_table_name +
                            " (_ID INTEGER PRIMARY KEY, card TEXT)";
                default:
                    return;
            };
            // TODO: start thread
            db.execSQL(q);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTableINE(db,shoppinglist_table_name);
            createTableINE(db, ficardlist_table_name);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("DB", "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            // TODO: start thread
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
        mOH.createTableINE(db, ficardlist_table_name);
    }


    public Cursor query(String table) {
        // TODO: add thread handler
        SQLiteDatabase db = mOH.getReadableDatabase();
        return db.query(shoppinglist_table_name, null, null, null, null, null, "_ID");
    }

    public void insertItem (String table, String nullColumnHack, ContentValues val) {
        Thread T = new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = mOH.getWritableDatabase();
                db.insert(table, nullColumnHack, val);



                runOnUiThread(new SLFragment.RefreshRVOnInsert());
            }
        });
        T.start();
    }


    public void updateItem (int id, int adapterPos, String newItemVal) {
        Thread T = new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = mOH.getWritableDatabase();
                ContentValues val = new ContentValues();
                val.put("item", newItemVal);
                db.update(shoppinglist_table_name, val, "_ID="+id, null);
                runOnUiThread(new SLFragment.RefreshRVOnUpdate(adapterPos));
            }
        });
        T.start();
    }

    public void removeItem (int id, int itemPosition) {
        Thread T = new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = mOH.getWritableDatabase();
                db.delete(shoppinglist_table_name,"_ID="+id,null);
                runOnUiThread(new SLFragment.RefreshRVOnRemoved(itemPosition));
            }
        });
        T.start();
    }


}








