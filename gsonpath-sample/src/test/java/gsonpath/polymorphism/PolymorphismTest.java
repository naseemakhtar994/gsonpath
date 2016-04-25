package gsonpath.polymorphism;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import gsonpath.GsonPath;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Lachlan on 25/04/2016.
 */
public class PolymorphismTest {

    @Test
    public void testPolymorphism() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(GsonPath.createTypeAdapterFactory());

        RuntimeTypeAdapterFactory<Type> type = RuntimeTypeAdapterFactory.of(Type.class, "type");
        type.registerSubtype(Type1.class, "type1");
        type.registerSubtype(Type2.class, "type2");
        type.registerSubtype(Type3.class, "type3");

        builder.registerTypeAdapterFactory(type);
        Gson gson = builder.create();

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("Polymorphism.json");

        Type[] array = GsonPath.getArrayStreamer(TypeStreamer.class).getArray(gson, new InputStreamReader(resourceAsStream));
        Assert.assertEquals(array.length, 4);

        Type1 value1 = (Type1) array[0];
        Assert.assertEquals(value1.name, "Type1 Example 1");
        Assert.assertEquals(value1.intTest, 1);

        Type1 value2 = (Type1) array[1];
        Assert.assertEquals(value2.name, "Type1 Example 2");
        Assert.assertEquals(value2.intTest, 2);

        Type2 value3 = (Type2) array[2];
        Assert.assertEquals(value3.name, "Type2 Example 1");
        Assert.assertEquals(value3.doubleTest, 1.0, 0);

        Type3 value4 = (Type3) array[3];
        Assert.assertEquals(value4.name, "Type3 Example 1");
        Assert.assertEquals(value4.stringTest, "123");
    }

}
