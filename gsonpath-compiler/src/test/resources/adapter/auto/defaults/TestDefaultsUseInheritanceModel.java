package adapter.auto.defaults;

import com.google.gson.annotations.SerializedName;
import gsonpath.AutoGsonAdapter;

@AutoGsonAdapter(defaultConfiguration = TestDefaultsConfig.class)
public class TestDefaultsUseInheritanceModel {
    @SerializedName("path$")
    Integer expectedValue;

    String ignored;
}