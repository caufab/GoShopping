package it.unipi.di.sam.goshopping;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DbAccess {

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
                            " (_ID INTEGER PRIMARY KEY, item TEXT, info TEXT, active_pos INTEGER);";
                    break;
                case ficardlist_table_name:
                    q = "CREATE TABLE IF NOT EXISTS "+ ficardlist_table_name +
                            " (_ID INTEGER PRIMARY KEY, card TEXT, info TEXT, active_pos INTEGER)";
                default:
                    return;
            };
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
            db.execSQL("DROP TABLE IF EXISTS "+shoppinglist_table_name);
            onCreate(db);
        }
    }


    private mSQLiteOH mOH;

    public DbAccess(Context context) {
        mOH = new mSQLiteOH(context);
    }


    public Cursor query(String table) {
        SQLiteDatabase db = mOH.getReadableDatabase();
        return db.query(shoppinglist_table_name, null, null, null, null, null, null);
    }

    public void insert (String table, String nullColumnHack, ContentValues val) {
      //  Thread Tins = new Thread(new Runnable() {
      //      @Override
      //      public void run() {
           //     try { Thread.sleep(2000); } catch (InterruptedException e) {}
        SQLiteDatabase db = mOH.getWritableDatabase();
        db.insert(table, nullColumnHack, val);

       //     }

     //   });
       // Tins.start();

    }

    public void delete(String table, String whereClause, Object whereArgs) {
        SQLiteDatabase db = mOH.getWritableDatabase();
        db.delete(shoppinglist_table_name,whereClause,null);
    }

    public void update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = mOH.getWritableDatabase();
        db.update(table, values, whereClause, whereArgs);
    }


}



/*
    private DatabaseHelper gsOpenHelper;

    @Override
    public boolean onCreate() {
        gsOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
*/






