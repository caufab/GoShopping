package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.content.ContentValues;
import android.database.Cursor;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import it.unipi.di.sam.goshopping.DbAccess;
import it.unipi.di.sam.goshopping.MainActivity;
import it.unipi.di.sam.goshopping.R;

public class SLFragment extends Fragment {



    public static ShoppingListAdapter slA;
    public static RecyclerView rv;
    private static View root;

    public static Cursor cursor;
    ContentValues newVal = new ContentValues();



    public void onCreate(Bundle savedInstanceStace) {
        super.onCreate(savedInstanceStace);

        slA = new ShoppingListAdapter();
        try { cursor = MainActivity.db.query(DbAccess.shoppinglist_table_name); }
        catch (Exception e) { Log.d("cursorException", "e.getMessage: "+e.getMessage() );
            // exit app somehow
        }
        // Database initial insert
            /*
        MainActivity.db.delete("shopping_items","_ID>0", null);

        for(int i=1;i<5;i++) {
            newVal = new ContentValues();
        //    newVal.put("_ID", i);
            newVal.put("item", "food-n"+i);
            newVal.put("info", "info-n"+i);
            newVal.put("active_pos", i);
            MainActivity.db.insert(DbAccess.shoppinglist_table_name, null, newVal);
        }
*/
    }



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        root = inflater.inflate(R.layout.fragment_shoppinglist, container, false);

        rv = (RecyclerView) root.findViewById(R.id.shoppinglist_rv);

        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setAdapter(slA);


        EditText et = (EditText) root.findViewById(R.id.new_item_input);
        Button addItemBtn = (Button) root.findViewById(R.id.input_item_btn_add);
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //    InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
            //    inputMethodManager.toggleSoftInputFromWindow( root.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);

                Editable ed = et.getText();
                if(ed != null) {
                    String str = ed.toString().trim();
                    if(ed.length() != 0) {
                        int p = getItemPosition(str, -1);
                        if(p == -1) {
                            // TODO: also if item is not already in list (check with query or in cursor?)
                            newVal.put("item", str);
                            newVal.put("active_pos", cursor.getCount() + 1);
                            MainActivity.db.insertItem(DbAccess.shoppinglist_table_name, null, newVal);
                        } else { // TODO: show something (effect of cardview or snackbar)
                            rv.scrollToPosition(p);
                        }
                        et.getText().clear();
                    }
                }
            }
        });

        return root;
    }

    public static void editItem(ShoppingListAdapter.ShoppingListViewHolder holder) {
        View ol = (View) root.findViewById(R.id.edit_overlay);
        Button undo = (Button) root.findViewById(R.id.input_item_btn_undo);
        Button done = (Button) root.findViewById(R.id.input_item_btn_done);
        Button add = (Button) root.findViewById(R.id.input_item_btn_add);
        EditText et = (EditText) root.findViewById(R.id.new_item_input);

        ol.setVisibility(View.VISIBLE);
        add.setVisibility(View.GONE);
        et.setText(holder.item);
        et.setSelection(holder.item.length());
        et.requestFocus();
        undo.setVisibility(View.VISIBLE);
        done.setVisibility(View.VISIBLE);


        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                undo.setVisibility(View.GONE);
                done.setVisibility(View.GONE);
                et.getText().clear();
                ol.setVisibility(View.GONE);
                add.setVisibility(View.VISIBLE);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable ed = et.getText();
                if(ed != null) {
                    String str = ed.toString().trim();
                    // update only if new item is not empty string or equal to old item
                    if (ed.length() != 0 && str.compareTo(holder.item) != 0) {
                        int p = getItemPosition(str, holder.getAdapterPosition());
                        if (p != -1) // item is not in list
                            rv.scrollToPosition(p);
                        else // if(element is in database) equals to inserting element that already exists in db
                            MainActivity.db.updateItem(holder.id, holder.active_pos, str);
                    }
                }
                undo.setVisibility(View.GONE);
                done.setVisibility(View.GONE);
                et.getText().clear();
                ol.setVisibility(View.GONE);
                add.setVisibility(View.VISIBLE);
            }
        });



    }

    // get holder position in RV, except if it's equal to exceptPos (use -1 for new items)
    public static int getItemPosition(String newItem, int exceptPos) {
        if(cursor.moveToFirst())
            do {
                if (cursor.getString(cursor.getColumnIndexOrThrow("item")).compareTo(newItem) == 0 &&
                        cursor.getPosition() != exceptPos)
                    return cursor.getPosition();
            } while(cursor.moveToNext());
        return -1;
    }

    public static class RefreshRVOnInsert implements Runnable {
        @Override
        public void run() {
            cursor = MainActivity.db.query(DbAccess.shoppinglist_table_name);
            int p=cursor.getCount()-1;
            slA.notifyItemInserted(p);
            rv.scrollToPosition(p);
        }
    }

    public static class RefreshRVOnUpdate implements Runnable {
        private int p;
        public RefreshRVOnUpdate(int position) { p = position; }

        @Override
        public void run() {
            cursor = MainActivity.db.query(DbAccess.shoppinglist_table_name);
            slA.notifyItemChanged(p);
            rv.scrollToPosition(p);
        }
    }

    public static class RefreshRVOnRemoved implements Runnable {
        private int p;
        public RefreshRVOnRemoved(int position) { p = position; }

        @Override
        public void run() {
            cursor = MainActivity.db.query(DbAccess.shoppinglist_table_name);
            slA.notifyItemRemoved(p);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    //    binding = null;
    }
}