package it.unipi.di.sam.goshopping;

final class Constants {

    public static final int NOTIFICATION_MAX_ITEMS = 5;

    static final class Geofences {
        public static final int NOTIFICATION_RESPONSIVENESS = 300000; // 5 minutes in ms = 300000
        public static final int MAX_GEOFENCES = 10; // Max numbers of active geofences
        static final float RADIUS = 50; // 50 meters
        static final int LOITERING_DELAY = 5000; // 1 min in ms (when entering in a geofence it waits this time before triggering the transition dwell)
    }
}