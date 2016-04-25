package gsonpath.generated;

import com.google.gson.annotations.SerializedName;
import gsonpath.AutoGsonAdapter;

import java.util.List;

@AutoGsonAdapter(rootField = "store")
public class StoreModel {
    @SerializedName("book")
    public List<BookModel> bookList;

    @SerializedName("bicycle.color")
    public String bikeColour;

    @SerializedName("bicycle.price")
    public double bikePrice;

    @AutoGsonAdapter
    public static class BookModel {
        public String category;
        public String author;
        public String title;
        public double price;
    }
}
