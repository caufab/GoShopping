package it.unipi.di.sam.goshopping;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class AppMain extends Application {

    private static DbAccess database;
    private static Context AppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        AppContext = getApplicationContext();
        initDb();
    }

    private static void initDb() {
        database = new DbAccess(AppContext);
    }

    public static DbAccess getDb() {
        if(database == null)
            initDb();
        return database;
    }

    // TODO: write new public static method to get application context

}
