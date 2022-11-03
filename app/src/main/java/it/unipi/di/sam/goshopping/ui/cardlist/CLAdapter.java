package it.unipi.di.sam.goshopping.ui.cardlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import it.unipi.di.sam.goshopping.R;

public class CLAdapter extends RecyclerView.Adapter<CLAdapter.CLViewHolder> {

    public static class CLViewHolder extends RecyclerView.ViewHolder {

        TextView tv;
        CardView cv;
        String code;
        String name;
        String barcodeFormat;
        int id;
        Color bgColor;

        //ColorUtils.calculateLuminance(Color.green(1));


        public CLViewHolder(@NonNull View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_cv);
            tv = (TextView) itemView.findViewById(R.id.card_name);
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
        }

        holder.tv.setText(holder.name);

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: show card bar code and number
                // TODO: increment number of used times in db
                //CLFragment.showCard(view.getContext(), holder);
                showCard(holder);
            }
        });

    }


    public static void showCard(CLAdapter.CLViewHolder holder) {
        View overlay = CLFragment.root.findViewById(R.id.show_card_overlay);
        ConstraintLayout popupCL = CLFragment.root.findViewById(R.id.show_card_cl);
        TextView cardName = CLFragment.root.findViewById(R.id.show_card_name);
        ImageView barcodeImageView = CLFragment.root.findViewById(R.id.barcode_image);
        TextView code = CLFragment.root.findViewById(R.id.barcode_text);
        FloatingActionButton fabEdit = CLFragment.root.findViewById(R.id.edit_card_fab);


        cardName.setText(holder.name);
        code.setText(holder.code);
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

        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), NewCardActivity.class);
                Bundle b = new Bundle();
                b.putInt("id", holder.id);
                b.putInt("rv_pos", holder.getAdapterPosition());
                b.putString("name", holder.name);
                b.putString("code", holder.code);
                b.putString("format", holder.barcodeFormat);
                intent.putExtras(b);
                view.getContext().startActivity(intent);
                backToFragment(overlay,popupCL,barcodeImageView,fabEdit);
            }
        });

        overlay.setOnClickListener(view -> backToFragment(overlay,popupCL, barcodeImageView,fabEdit));
    }


    public static void backToFragment(View overlay, ConstraintLayout popupCL, ImageView barcodeImageView, FloatingActionButton fabEdit) {
        overlay.setVisibility(View.GONE);
        popupCL.setVisibility(View.GONE);
        fabEdit.setVisibility(View.GONE);
        barcodeImageView.setImageDrawable(null);
        CLFragment.fabAdd.setVisibility(View.VISIBLE);
    }


}
