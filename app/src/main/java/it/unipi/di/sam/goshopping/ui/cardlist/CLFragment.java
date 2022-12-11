package it.unipi.di.sam.goshopping.ui.cardlist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import it.unipi.di.sam.goshopping.AppMain;
import it.unipi.di.sam.goshopping.R;

public class CLFragment extends Fragment {

    public CLFragment() {}

    public static RecyclerView rvc;
    public static CLAdapter clAdapter;
    public static FloatingActionButton fabAdd;
    public static BarcodeUtils bcUtils;
    public static Cursor cursor;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bcUtils = new BarcodeUtils();
        clAdapter = new CLAdapter();
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
                    rvc.setAdapter(clAdapter);
                    break;
                case "insert":
                    pos=cursor.getCount()-1;
                    clAdapter.notifyItemInserted(pos);
                    rvc.scrollToPosition(pos);
                    break;
                case "update":
                    clAdapter.notifyItemChanged(pos);
                    rvc.scrollToPosition(pos);
                    break;
                case "remove":
                    clAdapter.notifyItemRemoved(pos);
                    break;
                case "increment":
                    clAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_cardlist, container, false);

        rvc = root.findViewById(R.id.cardlist_rv);
        rvc.setHasFixedSize(true);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(root.getContext());
        int spanCount = Integer.parseInt(sharedPreferences.getString("card_list_span_count", "1"));
        GridLayoutManager llm = new GridLayoutManager(getContext(), spanCount);
        rvc.setLayoutManager(llm);

        AppMain.getDb().clQuery();

        fabAdd = root.findViewById(R.id.add_card_fab);
        fabAdd.setOnClickListener(view -> startActivity(new Intent(getContext(), NewCardActivity.class)));

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
    }

}