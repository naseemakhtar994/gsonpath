package gsonpath;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Lachlan on 2/03/2016.
 */
public class GsonPathTest {
    @Test
    public void test() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new GsonPathTypeAdapterFactory());

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("BookJson.json");

        Gson gson = builder.create();
        StoreModel model = gson.fromJson(new InputStreamReader(resourceAsStream), StoreModel.class);

        Assert.assertEquals(model.bikeColour, "red");
        Assert.assertEquals(model.bookList.length, 4);
        Assert.assertEquals(model.bookList[3].author, "J. R. R. Tolkien");
    }
}
