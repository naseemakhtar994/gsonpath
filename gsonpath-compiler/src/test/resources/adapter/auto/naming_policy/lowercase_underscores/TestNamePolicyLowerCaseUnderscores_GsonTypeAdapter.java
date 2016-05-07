package adapter.auto.naming_policy.lowercase_underscores;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestNamePolicyLowerCaseUnderscores_GsonTypeAdapter extends TypeAdapter<TestNamePolicyLowerCaseUnderscores> {
    private final Gson mGson;

    public TestNamePolicyLowerCaseUnderscores_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestNamePolicyLowerCaseUnderscores read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestNamePolicyLowerCaseUnderscores result = new TestNamePolicyLowerCaseUnderscores();

        int jsonFieldCounter0 = 0;
        in.beginObject();

        while (in.hasNext()) {
            if (jsonFieldCounter0 == 1) {
                in.skipValue();
                continue;
            }

            switch (in.nextName()) {
                case "test_value":
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
    public void write(JsonWriter out, TestNamePolicyLowerCaseUnderscores value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        // Begin
        out.beginObject();
        int obj0 = value.testValue;
        out.name("test_value");
        out.value(obj0);

        // End
        out.endObject();
    }
}