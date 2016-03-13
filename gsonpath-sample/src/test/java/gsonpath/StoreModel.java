package gsonpath;

/**
 * Created by Lachlan on 13/03/2016.
 */
@AutoGsonAdapter
public class StoreModel {
    @GsonPathField("store.book")
    BookModel[] bookList;

    @GsonPathField("store.bicycle.color")
    String bikeColour;

    @GsonPathField("store.bicycle.price")
    double bikePrice;

    @AutoGsonAdapter
    public static class BookModel {
        public String category;
        public String author;
        public String title;
        public double price;
    }
}
