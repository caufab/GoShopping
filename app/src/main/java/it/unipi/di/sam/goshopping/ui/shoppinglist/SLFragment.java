package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import it.unipi.di.sam.goshopping.DbAccess;
import it.unipi.di.sam.goshopping.MainActivity;
import it.unipi.di.sam.goshopping.R;
import it.unipi.di.sam.goshopping.databinding.FragmentShoppinglistBinding;

public class SLFragment extends Fragment {

    private FragmentShoppinglistBinding binding;

    public static ShoppingListAdapter slA;
    public static RecyclerView rv;

    public static Cursor cursor;
    ContentValues newVal = new ContentValues();

    public void onCreate(Bundle savedInstanceStace) {
        super.onCreate(savedInstanceStace);

        slA = new ShoppingListAdapter();
        try { cursor = MainActivity.db.query(DbAccess.shoppinglist_table_name); }
        catch (Exception e) { Log.d("cursorException", "e.getMessage: "+e.getMessage() );
            // exit app somehow
        }
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

        binding = FragmentShoppinglistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        rv = (RecyclerView) root.findViewById(R.id.shoppinglist_rv);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setAdapter(slA);


        String tmp;
        EditText et = (EditText) root.findViewById(R.id.new_item_input);
        Button addItemBtn = (Button) root.findViewById(R.id.input_item_btn);
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable ed = et.getText();
                if(ed != null && ed.length() != 0) {
                    // TODO: also if item is not already in list (check with query or in cursor?)
                    newVal.put("item", ed.toString());
                    newVal.put("active_pos", cursor.getCount()+1);
                    MainActivity.db.insertItem(DbAccess.shoppinglist_table_name, null, newVal);
                    et.clearFocus();
                    et.getText().clear();
                }
            }
        });

        return root;
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
            slA.notifyItemInserted(p);
            rv.scrollToPosition(p);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // close the cursor
        cursor.close();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}