package it.unipi.di.sam.goshopping.ui.stats;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;

import it.unipi.di.sam.goshopping.MainActivity;
import it.unipi.di.sam.goshopping.R;
import it.unipi.di.sam.goshopping.databinding.FragmentStatsBinding;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button addNotifBtn = root.findViewById(R.id.add_notification);
        Button remNotifBtn = root.findViewById(R.id.remove_notification);


        addNotifBtn.setOnClickListener(view -> {
            // send notification
            createNotificationChannel();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_dashboard_black_24dp)
                    .setContentText("Titolo della notifica")
                    .setContentText("descrizione della notifica, ciao!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("Much longer text that cannot fit one line..."));
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
            notificationManager.notify(1, builder.build());
        });

        remNotifBtn.setOnClickListener(v -> {
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.cancel(1);

        });






        return root;
    }


    String CHANNEL_ID = "CanaleBoh";

    // create notification channel ( API 26+ )
    private void createNotificationChannel() {
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}