package it.unipi.di.sam.goshopping.ui.cardlist;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import it.unipi.di.sam.goshopping.AppMain;
import it.unipi.di.sam.goshopping.R;

public class CLAdapter extends RecyclerView.Adapter<CLAdapter.CLViewHolder> {

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        CardView cv;
        String code;
        String name;
        String barcodeFormat;
        int id, usedTimes;
        int bgColor;

        public CLViewHolder(@NonNull View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.card_cv);
            tv = itemView.findViewById(R.id.card_name);
        }
    }

    @Override
    public int getItemCount() {
        return CLFragment.cursor.getCount();
    }

    @NonNull
    @Override
    public CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new CLViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CLViewHolder holder, int position) {

        if(CLFragment.cursor.moveToPosition(position)) {
            holder.code = CLFragment.cursor.getString(CLFragment.cursor.getColumnIndexOrThrow("code"));
            holder.id = CLFragment.cursor.getInt(CLFragment.cursor.getColumnIndexOrThrow("_ID"));
            holder.name = CLFragment.cursor.getString(CLFragment.cursor.getColumnIndexOrThrow("name"));
            holder.barcodeFormat = CLFragment.cursor.getString(CLFragment.cursor.getColumnIndexOrThrow("format"));
            holder.usedTimes = CLFragment.cursor.getInt(CLFragment.cursor.getColumnIndexOrThrow("used_times"));
            holder.bgColor = CLFragment.cursor.getInt(CLFragment.cursor.getColumnIndexOrThrow("color"));
        }

        holder.tv.setText(holder.name);
        holder.tv.setTextColor(getTextColorByBackground(holder.bgColor));
        holder.cv.setCardBackgroundColor(holder.bgColor);
        holder.cv.setOnClickListener(view -> showCard(view.getRootView(), holder));

    }

    // help choose text color base on how dark is it's background
    private static int getTextColorByBackground(int backgroundColor) {
        if(Color.valueOf(backgroundColor).luminance() <= 0.5)
            return Color.parseColor("#FFFFFF");
        else
            return Color.parseColor("#121212");
    }

    public static void showCard(View rootView, CLAdapter.CLViewHolder holder) {
        View overlay = rootView.findViewById(R.id.show_card_overlay);
        CardView popupCL = rootView.findViewById(R.id.show_card_cl);
        TextView cardName = rootView.findViewById(R.id.show_card_name);
        ImageView barcodeImageView = rootView.findViewById(R.id.barcode_image);
        TextView code = rootView.findViewById(R.id.barcode_text);
        FloatingActionButton fabEdit = rootView.findViewById(R.id.edit_card_fab);

        cardName.setText(holder.name);
        code.setText(holder.code);
        popupCL.setCardBackgroundColor(holder.bgColor);
        cardName.setTextColor(getTextColorByBackground(holder.bgColor));

        overlay.setVisibility(View.VISIBLE);
        popupCL.setVisibility(View.VISIBLE);

        // If barcode ImageView has been created generate immediately the barcode, otherwise wait until it is created
        if(barcodeImageView.isLaidOut())
            CLFragment.bcUtils.generateBarcodeImage(barcodeImageView, holder.barcodeFormat,  holder.code);
        else
            barcodeImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    barcodeImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    CLFragment.bcUtils.generateBarcodeImage(barcodeImageView, holder.barcodeFormat,  holder.code);
                }
            });

        CLFragment.fabAdd.setVisibility(View.GONE);
        fabEdit.setVisibility(View.VISIBLE);

        fabEdit.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), NewCardActivity.class);
            Bundle b = new Bundle();
            b.putInt("id", holder.id);
            b.putInt("rv_pos", holder.getAdapterPosition());
            b.putString("name", holder.name);
            b.putString("code", holder.code);
            b.putString("format", holder.barcodeFormat);
            b.putInt("color", holder.bgColor);
            intent.putExtras(b);
            view.getContext().startActivity(intent);
            backToFragment(overlay,popupCL,barcodeImageView,fabEdit);
        });

        overlay.setOnClickListener(view -> {
            backToFragment(overlay,popupCL, barcodeImageView,fabEdit);
            AppMain.getDb().incrementCardUsedTimes(holder.id, holder.usedTimes+1);
        });
    }

    public static void backToFragment(View overlay, CardView popupCL, ImageView barcodeImageView, FloatingActionButton fabEdit) {
        overlay.setVisibility(View.GONE);
        popupCL.setVisibility(View.GONE);
        fabEdit.setVisibility(View.GONE);
        barcodeImageView.setImageDrawable(null);
        CLFragment.fabAdd.setVisibility(View.VISIBLE);
    }

}
