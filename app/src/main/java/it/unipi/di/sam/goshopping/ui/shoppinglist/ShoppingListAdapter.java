package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import it.unipi.di.sam.goshopping.MainActivity;
import it.unipi.di.sam.goshopping.R;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder> {

    public static class ShoppingListViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView tv;
        int id;
        String item;
        ConstraintLayout cl;

        public ShoppingListViewHolder(@NonNull View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view);
            tv = (TextView) itemView.findViewById(R.id.shoppingitem_text);
            cl = (ConstraintLayout) itemView.findViewById(R.id.input_item_cl);
        }
    }

    public ShoppingListAdapter() {
    }

    @Override
    public int getItemCount() {
        return SLFragment.cursor.getCount();
    }


    @NonNull
    @Override
    public ShoppingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shoplist_item, parent, false);
        return new ShoppingListViewHolder(v);
    }

    String tmp;

    @Override
    public void onBindViewHolder(@NonNull ShoppingListViewHolder holder, int position) {

        if(SLFragment.cursor.moveToPosition(position)) {
            holder.id = SLFragment.cursor.getInt(SLFragment.cursor.getColumnIndexOrThrow("_ID"));
            holder.item = SLFragment.cursor.getString(SLFragment.cursor.getColumnIndexOrThrow("item"));
        }

        tmp = holder.item;
        holder.tv.setText(tmp);

        holder.cv.setOnClickListener(view -> MainActivity.db.removeItem(holder.id, holder.getAdapterPosition()));

        holder.cv.setOnLongClickListener(view -> {
            SLFragment.editItem(holder);
            return true;
        });

    }

}
