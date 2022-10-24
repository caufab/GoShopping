package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import it.unipi.di.sam.goshopping.R;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder> {

//    private final Cursor cursor;


    public static class ShoppingListViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView tv;
        TextInputEditText et;

        public ShoppingListViewHolder(@NonNull View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view);
            tv = (TextView) itemView.findViewById(R.id.shoppingitem_text);
            et = (TextInputEditText) itemView.findViewById(R.id.shoppingitem_editText);
        }
    }

    List<ShopItem> itemsList;

    public ShoppingListAdapter(List<ShopItem> l) {
     //   this.cursor = c;
        this.itemsList = l;
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }


    @NonNull
    @Override
    public ShoppingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shoplist_item, parent, false);
        return new ShoppingListViewHolder(v);
    }




    @Override
    public void onBindViewHolder(@NonNull ShoppingListViewHolder holder, int position) {
    /*
        if(cursor.moveToPosition(position)) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow("_ID"));
            item = cursor.getString(cursor.getColumnIndexOrThrow("item"));
        //    Log.d("cursor", id + " | " + item+ " | holder position: "+position);
*/

        String item = "[" + (position+1) + "] " + itemsList.get(position).item;

        holder.tv.setText(item);

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                itemsList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, itemsList.size());

            }
        });
        holder.cv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // make me edit it
                holder.setIsRecyclable(false);
                holder.et.setText(item);
                holder.tv.setVisibility(View.INVISIBLE);
                holder.et.setVisibility(View.VISIBLE);
                return true;
            }
        });


    }




}
