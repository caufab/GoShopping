package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.Editable;
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

import it.unipi.di.sam.goshopping.DbAccess;
import it.unipi.di.sam.goshopping.MainActivity;
import it.unipi.di.sam.goshopping.R;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder> {

    //private Cursor cursor;


    public static class ShoppingListViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView tv;
        int id,active_pos;
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
    ContentValues newVal = new ContentValues();

    @Override
    public void onBindViewHolder(@NonNull ShoppingListViewHolder holder, int position) {

        if(SLFragment.cursor.moveToPosition(position)) {
            holder.id = SLFragment.cursor.getInt(SLFragment.cursor.getColumnIndexOrThrow("_ID"));
            holder.active_pos = SLFragment.cursor.getInt(SLFragment.cursor.getColumnIndexOrThrow("active_pos"));
            holder.item = SLFragment.cursor.getString(SLFragment.cursor.getColumnIndexOrThrow("item"));
        }

        tmp = "[ID: "+holder.id+"] "+holder.item+"\n[active_pos: "+holder.active_pos+"] [rvPos: "+position+"]";
        holder.tv.setText(tmp);


        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        holder.cv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // make me edit it
            //    holder.setIsRecyclable(false);
                holder.et.setText(holder.item);
                holder.tv.setVisibility(View.INVISIBLE);
                holder.cl.setVisibility(View.VISIBLE);
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Editable ed = holder.et.getText();
                        if(ed != null && ed.length() != 0) {
                            MainActivity.db.updateItem(holder.id, holder.active_pos, holder.et.getText().toString());
                       //     SLFragment.cursor=MainActivity.db.query(DbAccess.shoppinglist_table_name);
                       //    notifyItemChanged(holder.active_pos-1);

                        }

                        holder.cl.setVisibility(View.INVISIBLE);
                        holder.tv.setVisibility(View.VISIBLE);
                    }
                });


                return true;
            }
        });


    }




}
