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


    public static Cursor cursor;
    ContentValues newVal = new ContentValues();

    public void onCreate(Bundle savedInstanceStace) {
        super.onCreate(savedInstanceStace);
        //MainActivity.db = new DbAccess(getContext());
    //    MainActivity.db.createTableINE(DbAccess.shoppinglist_table_name);

        MainActivity.db.delete("shopping_items","_ID>0", null);

        for(int i=1;i<35;i++) {
            newVal.put("item", "food-n"+i);
            newVal.put("info", "info-n"+i);
            newVal.put("active_pos", i);
            MainActivity.db.insert(DbAccess.shoppinglist_table_name, null, newVal);
        }

        try { cursor = MainActivity.db.query(DbAccess.shoppinglist_table_name); }

        catch (Exception e) { Log.d("cursorException", "e.getMessage: "+e.getMessage() );
            // exit app somehow
        }


    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
    //    ShoppingListViewModel ShoppingListViewModel =
    //            new ViewModelProvider(this).get(ShoppingListViewModel.class);

        binding = FragmentShoppinglistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

    //    final TextView textView = binding.textShoppinglist;
    //    ShoppingListViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);



        RecyclerView rv = (RecyclerView) root.findViewById(R.id.shoppinglist_rv);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        ShoppingListAdapter slA = new ShoppingListAdapter();
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
                    MainActivity.db.insert(DbAccess.shoppinglist_table_name, null, newVal);
                    cursor = MainActivity.db.query(DbAccess.shoppinglist_table_name);
                    et.clearFocus();
                    et.getText().clear();

                    int last = cursor.getCount()-1;
                    slA.notifyItemInserted(last);
                    rv.animate().setDuration(1000);
                    rv.scrollToPosition(last);
                //    slA.notifyItemRangeChanged(cursor.getCount()-1, cursor.getCount());

                    // TODO: remove focus from EditText (it should close the keyboard), and empty its text
                }
            }
        });


        return root;
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