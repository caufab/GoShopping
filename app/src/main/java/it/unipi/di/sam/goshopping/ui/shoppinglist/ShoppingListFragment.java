package it.unipi.di.sam.goshopping.ui.shoppinglist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import it.unipi.di.sam.goshopping.databinding.FragmentShoppinglistBinding;

public class ShoppingListFragment extends Fragment {

    private FragmentShoppinglistBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ShoppingListViewModel ShoppingListViewModel =
                new ViewModelProvider(this).get(ShoppingListViewModel.class);

        binding = FragmentShoppinglistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textShoppinglist;
        ShoppingListViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}