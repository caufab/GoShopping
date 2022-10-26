package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import it.unipi.di.sam.goshopping.MainActivity;
import it.unipi.di.sam.goshopping.R;
import it.unipi.di.sam.goshopping.databinding.FragmentShoppinglistBinding;

public class ShoppingListFragment extends Fragment {

    private FragmentShoppinglistBinding binding;


    private Cursor cursor;
 //   private List<ShopItem> itemsList;


    public void onCreate(Bundle savedInstaceStace) {
        super.onCreate(savedInstaceStace);
        MainActivity.db = new DbAccess(getContext());
        MainActivity.db.createTableINE(DbAccess.shoppinglist_table_name);

        MainActivity.db.delete("shopping_items","_ID>0", null);
        ContentValues values = new ContentValues();
        for(int i=1;i<30;i++) {
            values.put("item", "food-n"+i);
            values.put("info", "info-n"+i);
            values.put("active_pos", i);
            MainActivity.db.insert(DbAccess.shoppinglist_table_name, null, values);
        }



        try { cursor = MainActivity.db.query(DbAccess.shoppinglist_table_name);
        } catch (Exception e) {
            Log.d("cursorException", "e.getMessage: "+e.getMessage() );
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

    //    root.findViewById()


        RecyclerView rv = (RecyclerView) root.findViewById(R.id.shoppinglist_rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        ShoppingListAdapter slA = new ShoppingListAdapter(cursor);
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
        MainActivity.db.closeDb();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}