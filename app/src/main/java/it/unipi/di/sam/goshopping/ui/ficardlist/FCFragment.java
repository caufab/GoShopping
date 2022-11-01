package it.unipi.di.sam.goshopping.ui.ficardlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import it.unipi.di.sam.goshopping.MainActivity;
import it.unipi.di.sam.goshopping.R;
import it.unipi.di.sam.goshopping.databinding.FragmentFicardlistBinding;

public class FCFragment extends Fragment {

    private FragmentFicardlistBinding binding;
    private ListView lv;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    //    FiCardListViewModel fiCardListViewModel = new ViewModelProvider(this).get(FiCardListViewModel.class);

        binding = FragmentFicardlistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
    //    View root =  inflater.inflate(R.layout.fragment_shoppinglist, container, false);

        FloatingActionButton Fab = (FloatingActionButton) root.findViewById(R.id.add_card_fab);
        Fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addNewCard = new Intent(getContext(), NewCardActivity.class);
                startActivity(addNewCard);
            }
        });










        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}