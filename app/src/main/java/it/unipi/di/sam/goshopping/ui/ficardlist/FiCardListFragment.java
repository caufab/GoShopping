package it.unipi.di.sam.goshopping.ui.ficardlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import it.unipi.di.sam.goshopping.R;
import it.unipi.di.sam.goshopping.databinding.FragmentFicardlistBinding;

public class FiCardListFragment extends Fragment {

    private FragmentFicardlistBinding binding;
    private ListView lv;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    //    FiCardListViewModel fiCardListViewModel = new ViewModelProvider(this).get(FiCardListViewModel.class);

        binding = FragmentFicardlistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
    //    View root =  inflater.inflate(R.layout.fragment_shoppinglist, container, false);












        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}