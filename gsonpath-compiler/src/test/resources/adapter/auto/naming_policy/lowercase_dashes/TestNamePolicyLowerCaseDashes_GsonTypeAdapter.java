package adapter.auto.naming_policy.lowercase_dashes;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestNamePolicyLowerCaseDashes_GsonTypeAdapter extends TypeAdapter<TestNamePolicyLowerCaseDashes> {
    private final Gson mGson;

    public TestNamePolicyLowerCaseDashes_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestNamePolicyLowerCaseDashes read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestNamePolicyLowerCaseDashes result = new TestNamePolicyLowerCaseDashes();

        int jsonFieldCounter0 = 0;
        in.beginObject();

        while (in.hasNext()) {
            if (jsonFieldCounter0 == 1) {
                in.skipValue();
                continue;
            }

            switch (in.nextName()) {
                case "test-value":
                    jsonFieldCounter0++;

                    Integer value_test_value = getIntegerSafely(in);
                    if (value_test_value != null) {
                        result.testValue = value_test_value;
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
    public void write(JsonWriter out, TestNamePolicyLowerCaseDashes value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        // Begin
        out.beginObject();
        int obj0 = value.testValue;
        out.name("test-value");
        out.value(obj0);

        // End
        out.endObject();
    }
}