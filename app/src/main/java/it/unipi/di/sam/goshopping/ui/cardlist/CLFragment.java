package it.unipi.di.sam.goshopping.ui.cardlist;

import android.content.Intent;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import it.unipi.di.sam.goshopping.MainActivity;
import it.unipi.di.sam.goshopping.R;
import it.unipi.di.sam.goshopping.databinding.FragmentFicardlistBinding;

public class CLFragment extends Fragment {

    private FragmentFicardlistBinding binding;
    public static View root;
    public static RecyclerView rvc;
    public static CLAdapter CLA;
    public static FloatingActionButton fabAdd;
    public static BarcodeUtils bcUtils;

    public static Cursor cursor;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bcUtils = new BarcodeUtils();

        // get cursor
        CLA = new CLAdapter();

        try { cursor = MainActivity.db.getCards(); }
        catch (Exception e) {
            Log.d("cursorException", "e.getMessage: "+e.getMessage() );
            // TODO: do something about it
        }

    }



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //root = inflater.inflate(R.layout.fragment_shoppinglist, container, false);

        binding = FragmentFicardlistBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        rvc = (RecyclerView) root.findViewById(R.id.cardlist_rv);
        rvc.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvc.setLayoutManager(llm);
        rvc.setAdapter(CLA);


        fabAdd = (FloatingActionButton) root.findViewById(R.id.add_card_fab);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addNewCard = new Intent(getContext(), NewCardActivity.class);
                startActivity(addNewCard);
            }
        });

        return root;
    }


    public static class RefreshRVOnCardInsert implements Runnable {
        @Override
        public void run() {
            cursor = MainActivity.db.getCards();
            int p=cursor.getCount()-1;
            CLA.notifyItemInserted(p);
            rvc.scrollToPosition(p);
        }
    }

    public static class RefreshRVOnCardUpdate implements Runnable {
        private final int p;
        public RefreshRVOnCardUpdate(int position) { p = position; }
        @Override
        public void run() {
            cursor = MainActivity.db.getCards();
            CLA.notifyItemChanged(p);
            rvc.scrollToPosition(p);
        }
    }


    public static class RefreshRVOnCardRemoved implements Runnable {
        private final int p;
        public RefreshRVOnCardRemoved(int position) { p = position; }
        @Override
        public void run() {
            cursor = MainActivity.db.getCards();
            CLA.notifyItemRemoved(p);
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }




}