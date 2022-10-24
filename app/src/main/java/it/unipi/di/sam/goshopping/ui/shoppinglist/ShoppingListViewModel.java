package it.unipi.di.sam.goshopping.ui.shoppinglist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/* ViewModel is a class that is responsible for preparing and managing the data for an Activity or a Fragment.
It also handles the communication of the Activity / Fragment with the rest of the application (e.g. calling the business logic classes).
 */
public class ShoppingListViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ShoppingListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is shopping list fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}