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
import java.util.Arrays;
import java.util.List;

import it.unipi.di.sam.goshopping.R;
import it.unipi.di.sam.goshopping.databinding.FragmentShoppinglistBinding;

public class ShoppingListFragment extends Fragment {

    private FragmentShoppinglistBinding binding;
    private List<String> shoppingitems;
    private ShoppingListAdapter2 adapter;
    private SQLiteDatabase myDb;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ShoppingListViewModel ShoppingListViewModel =
                new ViewModelProvider(this).get(ShoppingListViewModel.class);

        binding = FragmentShoppinglistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

    //    final TextView textView = binding.textShoppinglist;
    //    ShoppingListViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

    //    root.findViewById()

        GsDatabaseHelper gsDb = new GsDatabaseHelper(getContext());
        SQLiteDatabase db = gsDb.getWritableDatabase();

        ContentValues values = new ContentValues();
        long newRowId = 1;
        for(int i=1;i<4;i++) {
            values.put("item", "food-n"+newRowId);
            values.put("info", "info-n"+newRowId);
            newRowId = db.insert(GsDatabaseHelper.shoppinglist_table_name, null, values);
        }
/*
        values.put("item", "food-n"+newRowId);
        values.put("info", "info-n"+newRowId);
        values.put("_ID", 99);
        newRowId = db.insert(GsDatabaseHelper.shoppinglist_table_name, null, values);
*/


    //    db = gsDb.getReadableDatabase();

     //   String[] projection = null; // columns clause: null = all columns
     //   String selection = null; // where clause: null = all rows
    //    String[] selectionArgs = { "My Title" };

    //     Cursor cursor = db.rawQuery("SELECT * FROM shopping_items;", null);

        Cursor cursor = db.query(GsDatabaseHelper.shoppinglist_table_name, null, null, null, null, null, null);

        int id, pos;
        String item;
        String info;

 //       shoppingitems = new ArrayList<>();
/*
        Log.d("cursor", "columns: "+ Arrays.toString(cursor.getColumnNames()));
        Log.d("cursor", "rows: "+(cursor.getCount()));
        while(cursor.moveToNext()) {
            id=cursor.getInt(cursor.getColumnIndexOrThrow("_ID"));
            item=cursor.getString(cursor.getColumnIndexOrThrow("item"));
            info=cursor.getString(cursor.getColumnIndexOrThrow("info"));
            pos=cursor.getPosition();
            Log.d("cursor", id+" | "+item+ " | "+info+" | pos: "+pos);
            shoppingitems.add(item);
        }
*/

        RecyclerView rv = (RecyclerView) root.findViewById(R.id.shoppinglist_rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        ShoppingListAdapter slA = new ShoppingListAdapter(cursor);
        rv.setAdapter(slA);

/*
        for(int i=0;i<10;i++) {
            shoppingitems.add("cibo "+i);
        }
*/
    //    adapter = new ShoppingListAdapter2(shoppingitems);
    //    rv.setAdapter(adapter);

    //    MyCursorAdapter myAdapter = new MyCursorAdapter(getContext(), cursor, CursorAdapter.FLAG_AUTO_REQUERY);

    //    ListView lv = (ListView) getView().findViewById(R.id.shoppinglist_rv);
    //    lv.setAdapter(myAdapter);

        return root;
    }

    @Override
    public void onResume() {

        super.onResume();
    //    shoppingitems.add("new element!!");
    //    adapter.notifyItemInserted(shoppingitems.size());

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}