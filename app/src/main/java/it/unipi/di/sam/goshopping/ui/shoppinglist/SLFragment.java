package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.unipi.di.sam.goshopping.AppMain;
import it.unipi.di.sam.goshopping.R;

public class SLFragment extends Fragment {

    public static ShoppingListAdapter slAdapter;
    public static RecyclerView rv;

    public static Cursor cursor;

    public void onCreate(Bundle savedInstanceStace) {
        super.onCreate(savedInstanceStace);
        slAdapter = new ShoppingListAdapter();
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
                    rv.setAdapter(slAdapter);
                    break;
                case "insert":
                    pos=cursor.getCount()-1;
                    slAdapter.notifyItemInserted(pos);
                    rv.scrollToPosition(pos);
                    break;
                case "update":
                    slAdapter.notifyItemChanged(pos);
                    rv.scrollToPosition(pos);
                    break;
                case "remove":
                    slAdapter.notifyItemRemoved(pos);
                    break;
                default:
                    break;
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_shoppinglist, container, false);

        rv = root.findViewById(R.id.shoppinglist_rv);
        rv.setHasFixedSize(true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(root.getContext());
        int spanCount = Integer.parseInt(sharedPreferences.getString("shopping_list_span_count", "1"));
        GridLayoutManager llm = new GridLayoutManager(root.getContext(), spanCount);

        rv.setLayoutManager(llm);

        AppMain.getDb().slQuery();

        EditText et = root.findViewById(R.id.new_item_input);
        Button addItemBtn = root.findViewById(R.id.input_item_btn_add);
        addItemBtn.setOnClickListener(view -> {
            Editable ed = et.getText();
            if(ed != null) {
                String str = ed.toString().trim();
                if(ed.length() != 0) {
                    int p = getItemPosition(str, -1);
                    if(p == -1) {
                        AppMain.getDb().insertItem(str);
                    } else {
                        rv.scrollToPosition(p);
                    }
                    et.getText().clear();
                }
            }
        });
        return root;
    }

    public static void editItem(View rootView, ShoppingListAdapter.ShoppingListViewHolder holder) {
        View ol = rootView.findViewById(R.id.edit_overlay);
        Button undo = rootView.findViewById(R.id.input_item_btn_undo);
        Button done = rootView.findViewById(R.id.input_item_btn_done);
        Button add = rootView.findViewById(R.id.input_item_btn_add);
        EditText et = rootView.findViewById(R.id.new_item_input);

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
                        AppMain.getDb().updateItem(holder.id, holder.getAdapterPosition(), str);
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
    }
}