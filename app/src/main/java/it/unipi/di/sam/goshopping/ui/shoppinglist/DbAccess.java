package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//public class GSDatabase {




public class DbAccess extends SQLiteOpenHelper {

    // run on background thread!
    private SQLiteDatabase db = getWritableDatabase();

    public static final String DATABASE_NAME = "gs_database.db";
    public static final String shoppinglist_table_name = "shopping_items";
    public static final String ficardlist_table_name = "cards";
    public static final int DATABASE_VERSION = 2;

    public DbAccess(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public void createTableINE(String table_name) {
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
        // creating database - TODO: check if i need to create database too
        // maybe i need to use db parameter
        createTableINE(shoppinglist_table_name);
        createTableINE(ficardlist_table_name);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DB", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS "+shoppinglist_table_name);
        onCreate(db);
    }





    public Cursor query(String table) {
        return db.query(DbAccess.shoppinglist_table_name, null, null, null, null, null, null);
    }

    public void insert (String table, String nullColumnHack, ContentValues val) {
        db.insert(shoppinglist_table_name, nullColumnHack, val);
    }

    public void delete(String table, String whereClause, Object whereArgs) {
        db.delete(shoppinglist_table_name,whereClause,null);
    }

    public void update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        db.update(table, values, whereClause, whereArgs);
    }

    public void closeDb() {
        db.close();
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






