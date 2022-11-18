package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    }

    public static class UpdateCursor implements Runnable {
        private final String request;
        private int pos;
        public UpdateCursor(Cursor updatedCursor, String updateRequest) {
            cursor = updatedCursor;
            request = updateRequest;
        }
        public UpdateCursor(Cursor updatedCursor, String updateRequest, int itemPosition) {
            cursor = updatedCursor;
            request = updateRequest;
            pos = itemPosition;
        }

        @Override
        public void run() {
            switch(request) {
                case "set_adapter":
                    rv.setAdapter(slA);
                    break;
                case "insert":
                    pos=cursor.getCount()-1;
                    slA.notifyItemInserted(pos);
                    rv.scrollToPosition(pos);
                    break;
                case "update":
                    slA.notifyItemChanged(pos);
                    rv.scrollToPosition(pos);
                    break;
                case "remove":
                    slA.notifyItemRemoved(pos);
                    break;
                default:
                    break;
            }

        }
    }



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_shoppinglist, container, false);

        rv = (RecyclerView) root.findViewById(R.id.shoppinglist_rv);
        rv.setHasFixedSize(true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int spanCount = Integer.parseInt(sharedPreferences.getString("shopping_list_span_count", "1"));
        GridLayoutManager llm = new GridLayoutManager(getContext(), spanCount);

        //LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);


        MainActivity.db.slQuery();


        EditText et = (EditText) root.findViewById(R.id.new_item_input);
        Button addItemBtn = (Button) root.findViewById(R.id.input_item_btn_add);
        addItemBtn.setOnClickListener(view -> {
            Editable ed = et.getText();
            if(ed != null) {
                String str = ed.toString().trim();
                if(ed.length() != 0) {
                    int p = getItemPosition(str, -1);
                    if(p == -1) {
                        // TODO: also if item is not already in list (check with query or in cursor?)
                        newVal.put("item", str);
                        MainActivity.db.insertItem(DbAccess.shoppinglist_table_name, null, newVal);
                    } else { // TODO: show something (effect of cardview or snackbar)
                        rv.scrollToPosition(p);
                    }
                    et.getText().clear();
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


        undo.setOnClickListener(view -> {
            undo.setVisibility(View.GONE);
            done.setVisibility(View.GONE);
            et.getText().clear();
            ol.setVisibility(View.GONE);
            add.setVisibility(View.VISIBLE);
        });

        done.setOnClickListener(view -> {
            Editable ed = et.getText();
            if(ed != null) {
                String str = ed.toString().trim();
                // update only if new item is not empty string or equal to old item
                if (ed.length() != 0 && str.compareTo(holder.item) != 0) {
                    int p = getItemPosition(str, holder.getAdapterPosition());
                    if (p != -1) // item is not in list
                        rv.scrollToPosition(p);
                    else // if(element is in database) equals to inserting element that already exists in db
                        MainActivity.db.updateItem(holder.id, holder.getAdapterPosition(), str);
                }
            }
            undo.setVisibility(View.GONE);
            done.setVisibility(View.GONE);
            et.getText().clear();
            ol.setVisibility(View.GONE);
            add.setVisibility(View.VISIBLE);
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