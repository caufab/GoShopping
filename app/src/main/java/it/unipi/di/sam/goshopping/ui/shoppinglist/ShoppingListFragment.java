package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.unipi.di.sam.goshopping.R;
import it.unipi.di.sam.goshopping.databinding.FragmentShoppinglistBinding;

public class ShoppingListFragment extends Fragment {

    private FragmentShoppinglistBinding binding;
    private List<String> shoppingitems;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ShoppingListViewModel ShoppingListViewModel =
                new ViewModelProvider(this).get(ShoppingListViewModel.class);

        binding = FragmentShoppinglistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

    //    final TextView textView = binding.textShoppinglist;
    //    ShoppingListViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

    //    root.findViewById()
        RecyclerView rv = (RecyclerView) root.findViewById(R.id.shoppinglist_rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        shoppingitems = new ArrayList<>();

        for(int i=0;i<20;i++) {
            shoppingitems.add("cibo "+i);
        }

        ShoppingListAdapter adapter = new ShoppingListAdapter(shoppingitems);
        rv.setAdapter(adapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}