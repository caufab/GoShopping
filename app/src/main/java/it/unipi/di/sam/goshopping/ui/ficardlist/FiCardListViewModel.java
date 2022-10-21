package it.unipi.di.sam.goshopping.ui.ficardlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FiCardListViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public FiCardListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Fidelity Card fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}