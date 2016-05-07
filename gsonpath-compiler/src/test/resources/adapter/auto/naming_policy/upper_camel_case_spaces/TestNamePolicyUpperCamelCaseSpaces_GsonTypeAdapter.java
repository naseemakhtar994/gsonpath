package adapter.auto.naming_policy.upper_camel_case_spaces;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestNamePolicyUpperCamelCaseSpaces_GsonTypeAdapter extends TypeAdapter<TestNamePolicyUpperCamelCaseSpaces> {
    private final Gson mGson;

    public TestNamePolicyUpperCamelCaseSpaces_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestNamePolicyUpperCamelCaseSpaces read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestNamePolicyUpperCamelCaseSpaces result = new TestNamePolicyUpperCamelCaseSpaces();

        int jsonFieldCounter0 = 0;
        in.beginObject();

        while (in.hasNext()) {
            if (jsonFieldCounter0 == 1) {
                in.skipValue();
                continue;
            }

            switch (in.nextName()) {
                case "Test Value":
                    jsonFieldCounter0++;

                    Integer safeValue0 = getIntegerSafely(in);
                    if (safeValue0 != null) {
                        result.testValue = safeValue0;
                    }
                    break;

                default:
                    in.skipValue();
                    break;
            }
        }


        in.endObject();
        return result;
    }

    @Override
    public void write(JsonWriter out, TestNamePolicyUpperCamelCaseSpaces value) throws IOException {
        // GsonPath does not support writing at this stage.
    }
}