package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.database.Cursor;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

import it.unipi.di.sam.goshopping.R;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder> {

    private final Cursor cursor;
    public static class ShoppingListViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView tv;
        public ShoppingListViewHolder(@NonNull View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view);
            tv = (TextView) itemView.findViewById(R.id.shoppingitem_text);
        }
    }

    public ShoppingListAdapter(Cursor c) {
        this.cursor = c;
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }


    @NonNull
    @Override
    public ShoppingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shoplist_item, parent, false);
        return new ShoppingListViewHolder(v);
    }

    int id;
    String item;

    @Override
    public void onBindViewHolder(@NonNull ShoppingListViewHolder holder, int position) {
        if(cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow("_ID"));
            item = cursor.getString(cursor.getColumnIndexOrThrow("item"));
            Log.d("cursor", id + " | " + item);

            holder.tv.setText(item);
        }
    }




}
