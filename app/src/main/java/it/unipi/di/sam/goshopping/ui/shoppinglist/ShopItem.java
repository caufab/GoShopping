package it.unipi.di.sam.goshopping.ui.shoppinglist;

public class ShopItem {

    public int id;
    public String item;
    public boolean modified;
    public boolean removed;

    public ShopItem(int i, String s) {
        id=i;
        item = s;
        modified = false;
        removed = false;
        // more to come
    }

    public String getItem() {
        return item;
    }



}
