package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import it.unipi.di.sam.goshopping.R;
import it.unipi.di.sam.goshopping.databinding.FragmentShoppinglistBinding;

public class ShoppingListFragment extends Fragment {

    private FragmentShoppinglistBinding binding;
    private GsDatabaseHelper gsDb;
    private SQLiteDatabase db;
    private Cursor cursor;
    private List<ShopItem> itemsList;


    public void onCreate(Bundle savedInstaceStace) {
        super.onCreate(savedInstaceStace);
        gsDb = new GsDatabaseHelper(getContext());
        db = gsDb.getWritableDatabase();
        cursor = db.query(GsDatabaseHelper.shoppinglist_table_name, null, null, null, null, null, null);
    //    Log.e("cursor", "just queried");
        itemsList = new Vector<ShopItem>();

        int ids;
        String s;

        // Filling the itemsList map with cursor values form db
        while(cursor.moveToNext()) {
            ids = cursor.getInt(cursor.getColumnIndexOrThrow("_ID"));
            s = cursor.getString(cursor.getColumnIndexOrThrow("item"));
            itemsList.add(new ShopItem(ids, s));
    //        Log.d("cursor", "just put ("+cursor.getInt(cursor.getColumnIndexOrThrow("_ID"))+","+cursor.getString(cursor.getColumnIndexOrThrow("item"))+")");
        }
/*
        ContentValues values = new ContentValues();
        long newRowId = 1;
        for(int i=1;i<5;i++) {
            values.put("item", "food-n"+newRowId);
            values.put("info", "info-n"+newRowId);
            newRowId = db.insert(GsDatabaseHelper.shoppinglist_table_name, null, values);
        }
*/
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
    //    ShoppingListViewModel ShoppingListViewModel =
    //            new ViewModelProvider(this).get(ShoppingListViewModel.class);

        binding = FragmentShoppinglistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

    //    final TextView textView = binding.textShoppinglist;
    //    ShoppingListViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

    //    root.findViewById()


        RecyclerView rv = (RecyclerView) root.findViewById(R.id.shoppinglist_rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        ShoppingListAdapter slA = new ShoppingListAdapter(itemsList);
        rv.setAdapter(slA);


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
        db.close();
        gsDb.close();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}