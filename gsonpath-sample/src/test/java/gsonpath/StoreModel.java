package gsonpath;

/**
 * Created by Lachlan on 13/03/2016.
 */
@AutoGsonAdapter(rootField = "store")
public class StoreModel {
    @GsonPathField("book")
    BookModel[] bookList;

    @GsonPathField("bicycle.color")
    String bikeColour;

    @GsonPathField("bicycle.price")
    double bikePrice;

    @AutoGsonAdapter
    public static class BookModel {
        public String category;
        public String author;
        public String title;
        public double price;
    }
}
