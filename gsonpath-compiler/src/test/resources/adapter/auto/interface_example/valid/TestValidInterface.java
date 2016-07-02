package adapter.auto.interface_example.valid;

import gsonpath.AutoGsonAdapter;
import com.google.gson.annotations.SerializedName;

@AutoGsonAdapter
public interface TestValidInterface {
    @SerializedName("Json1.Nest1")
    Integer getValue1();

    Integer getValue2();
}