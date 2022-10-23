package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unipi.di.sam.goshopping.R;

public class ShoppingListAdapter2 extends RecyclerView.Adapter<ShoppingListAdapter2.ShoppingItemViewHolder> {

    public static class ShoppingItemViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView itemText;

        public ShoppingItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view);
            itemText = (TextView) itemView.findViewById(R.id.shoppingitem_text);
        }
    }

    List<String> itemsList;

    ShoppingListAdapter2(List<String> l) {
        this.itemsList = l;
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }


    @NonNull
    @Override
    public ShoppingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shoplist_item, parent, false);
        return new ShoppingItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingItemViewHolder holder, int position) {
        holder.itemText.setText(itemsList.get(position));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView rv) {
        super.onAttachedToRecyclerView(rv);
    }


}
