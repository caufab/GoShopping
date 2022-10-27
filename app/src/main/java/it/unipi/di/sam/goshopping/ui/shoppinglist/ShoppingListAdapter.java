package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import it.unipi.di.sam.goshopping.MainActivity;
import it.unipi.di.sam.goshopping.R;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder> {

    private Cursor cursor;


    public static class ShoppingListViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView tv;
        int id;
        String item;
        TextInputEditText et;
        ConstraintLayout cl;
        Button btn;

        public ShoppingListViewHolder(@NonNull View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view);
            tv = (TextView) itemView.findViewById(R.id.shoppingitem_text);
            et = (TextInputEditText) itemView.findViewById(R.id.shoppingitem_editText);
            cl = (ConstraintLayout) itemView.findViewById(R.id.input_item_cl);
            btn = (Button) itemView.findViewById(R.id.cv_input_item_btn);
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


    int _id,active_pos;
    String tmp;
    ContentValues conVal = new ContentValues();

    @Override
    public void onBindViewHolder(@NonNull ShoppingListViewHolder holder, int position) {

        if(cursor.moveToPosition(position)) {
            holder.id = cursor.getInt(cursor.getColumnIndexOrThrow("_ID"));
       //     active_pos = cursor.getInt(cursor.getColumnIndexOrThrow("active_pos"));
            holder.item = cursor.getString(cursor.getColumnIndexOrThrow("item"));
        }

        tmp = "["+holder.id+"] "+holder.item;
        holder.tv.setText(tmp);


        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                conVal.put("item", "newval("+holder.id+")");
                Log.d("database", "updating row: "+holder.id);
                MainActivity.db.update("shopping_items", conVal,"_ID="+holder.id, null);

                cursor=MainActivity.db.query(DbAccess.shoppinglist_table_name);

                cursor.moveToPosition(holder.id);
                Log.e("cursor", "item changed: "+cursor.getString(cursor.getColumnIndexOrThrow("item")));

                notifyItemChanged(holder.id-1);


            }
        });


        holder.cv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // make me edit it
                holder.setIsRecyclable(false);
                holder.et.setText(holder.item);
                holder.tv.setVisibility(View.INVISIBLE);
                holder.cl.setVisibility(View.VISIBLE);
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("text-example", "Item ["+holder.id+"] new value: "+holder.et.getText());
                        // TODO: update db
                        // TODO: notifyItemChanged
                        holder.cl.setVisibility(View.INVISIBLE);
                        holder.tv.setVisibility(View.VISIBLE);
                    }
                });


                return true;
            }
        });


    }




}
