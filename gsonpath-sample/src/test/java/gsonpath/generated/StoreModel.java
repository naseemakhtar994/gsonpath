package gsonpath.generated;

import gsonpath.AutoGsonAdapter;
import gsonpath.GsonPathField;

/**
 * Created by Lachlan on 13/03/2016.
 */
@AutoGsonAdapter(rootField = "store")
public class StoreModel {
    @GsonPathField("book")
    public BookModel[] bookList;

    @GsonPathField("bicycle.color")
    public String bikeColour;

    @GsonPathField("bicycle.price")
    public double bikePrice;

    @AutoGsonAdapter
    public static class BookModel {
        public String category;
        public String author;
        public String title;
        public double price;
    }
}
