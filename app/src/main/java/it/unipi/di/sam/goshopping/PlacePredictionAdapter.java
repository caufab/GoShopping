package it.unipi.di.sam.goshopping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.AutocompletePrediction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlacePredictionAdapter extends RecyclerView.Adapter<PlacePredictionAdapter.PlacePredictionViewHolder> {

    // ViewHolder implementation
    public static class PlacePredictionViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView address;
        private final TextView distance;

        public PlacePredictionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_view_title);
            address = itemView.findViewById(R.id.text_view_address);
            distance = itemView.findViewById(R.id.distance_text);
        }

        public void setPrediction(AutocompletePrediction prediction) {
            title.setText(prediction.getPrimaryText(null));
            address.setText(prediction.getSecondaryText(null));
            if(prediction.getDistanceMeters() != null)
                distance.setText(formatDistanceForViewHolder(prediction.getDistanceMeters()));
        }
    }

    private static String formatDistanceForViewHolder(int distanceMeters) {
        if(distanceMeters<=500)
            return distanceMeters+"\nm";
        else if(distanceMeters<10000)
            return String.format(Locale.ITALY,"%.2f\nKm", distanceMeters * 0.001);
        else if(distanceMeters<=999999)
            return String.format(Locale.ITALY,"%.0f\nKm", distanceMeters * 0.001);
        else
            return ">999\nkm";
    }


    interface OnPlaceClickListener {
        void onPlaceClicked(AutocompletePrediction place);
    }

    private final List<AutocompletePrediction> predictions = new ArrayList<>();
    private OnPlaceClickListener onPlaceClickListener;

    @NonNull
    @Override
    public PlacePredictionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PlacePredictionViewHolder(inflater.inflate(R.layout.place_element, parent, false));
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    @Override
    public void onBindViewHolder(@NonNull PlacePredictionViewHolder holder, int position) {
        final AutocompletePrediction prediction = predictions.get(position);
        holder.setPrediction(prediction);
        holder.itemView.setOnClickListener(v -> {  // FIXME: should I put some add feature on this listener?
            if(onPlaceClickListener != null) {
                onPlaceClickListener.onPlaceClicked(prediction);
            }
        });
    }


    public void setPredictions(List<AutocompletePrediction> predictions) {
        this.predictions.clear();
        this.predictions.addAll(predictions);
        notifyDataSetChanged(); // FIXME: change to something more specific
    }


    // Serve per settare un listener da altre classi?
    public void setPlaceClickListener(OnPlaceClickListener onPlaceClickListener) {
        this.onPlaceClickListener = onPlaceClickListener;
    }


}
