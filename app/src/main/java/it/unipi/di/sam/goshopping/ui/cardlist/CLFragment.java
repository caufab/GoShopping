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

        CLA = new CLAdapter();

    }

    public static class UpdateCursor implements Runnable {
        private final String request;
        private int pos;
        public UpdateCursor(Cursor updatedCursor, String updateRequest) {
            cursor = updatedCursor;
            request = updateRequest;
        }
        public UpdateCursor(Cursor updatedCursor, String updateRequest, int itemPosition) {
            cursor = updatedCursor;
            request = updateRequest;
            pos = itemPosition;
        }

        @Override
        public void run() {
            switch(request) {
                case "set_adapter":
                    rvc.setAdapter(CLA);
                    break;
                case "insert":
                    pos=cursor.getCount()-1;
                    CLA.notifyItemInserted(pos);
                    rvc.scrollToPosition(pos);
                    break;
                case "update":
                    CLA.notifyItemChanged(pos);
                    rvc.scrollToPosition(pos);
                    break;
                case "remove":
                    CLA.notifyItemRemoved(pos);
                    break;
                default:
                    break;
            }

        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFicardlistBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        rvc = (RecyclerView) root.findViewById(R.id.cardlist_rv);
        rvc.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvc.setLayoutManager(llm);

        MainActivity.db.clQuery();

        fabAdd = (FloatingActionButton) root.findViewById(R.id.add_card_fab);
        fabAdd.setOnClickListener(view -> startActivity(new Intent(getContext(), NewCardActivity.class)));

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }



}