package it.unipi.di.sam.goshopping.ui.ficardlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import it.unipi.di.sam.goshopping.databinding.FragmentFicardlistBinding;

public class FiCardListFragment extends Fragment {

    private FragmentFicardlistBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FiCardListViewModel fiCardListViewModel =
                new ViewModelProvider(this).get(FiCardListViewModel.class);

        binding = FragmentFicardlistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textFicardlist;
        fiCardListViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}