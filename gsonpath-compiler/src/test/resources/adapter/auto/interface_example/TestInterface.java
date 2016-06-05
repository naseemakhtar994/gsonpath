package adapter.auto.interface_example;

import gsonpath.AutoGsonAdapter;
import com.google.gson.annotations.SerializedName;

@AutoGsonAdapter
public interface TestInterface {
    @SerializedName("Json1.Nest1")
    int getValue1();
}