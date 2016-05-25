package adapter.auto.defaults;

import com.google.gson.annotations.SerializedName;
import gsonpath.*;

@AutoGsonAdapter(
        defaultConfiguration = TestDefaultsConfig.class,
        flattenDelimiter = @FlattenDelimiter('.'),
        serializeNulls = InheritableBoolean.FALSE,
        ignoreNonAnnotatedFields = InheritableBoolean.FALSE,
        fieldNamingPolicy = GsonPathFieldNamingPolicy.IDENTITY,
        fieldValidationType = GsonFieldValidationType.NO_VALIDATION
)
public class TestDefaultsOverrideInheritanceModel {
    @SerializedName("path.")
    Integer expectedValue;

    String notIgnored;
}