package adapter.auto.defaults;

import com.google.gson.FieldNamingPolicy;
import gsonpath.GsonFieldValidationType;
import gsonpath.GsonPathDefaultConfiguration;

@GsonPathDefaultConfiguration(
        flattenDelimiter = '$',
        serializeNulls = true,
        ignoreNonAnnotatedFields = true,
        fieldNamingPolicy = FieldNamingPolicy.LOWER_CASE_WITH_DASHES,
        fieldValidationType = GsonFieldValidationType.VALIDATE_ALL_EXCEPT_NULLABLE
)
public class TestDefaultsConfig {
}