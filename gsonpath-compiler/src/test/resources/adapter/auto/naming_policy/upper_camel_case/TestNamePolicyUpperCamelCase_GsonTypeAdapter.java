package adapter.auto.naming_policy.upper_camel_case;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestNamePolicyUpperCamelCase_GsonTypeAdapter extends TypeAdapter<TestNamePolicyUpperCamelCase> {
    private final Gson mGson;

    public TestNamePolicyUpperCamelCase_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestNamePolicyUpperCamelCase read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestNamePolicyUpperCamelCase result = new TestNamePolicyUpperCamelCase();

        int jsonFieldCounter0 = 0;
        in.beginObject();

        while (in.hasNext()) {
            if (jsonFieldCounter0 == 1) {
                in.skipValue();
                continue;
            }

            switch (in.nextName()) {
                case "TestValue":
                    jsonFieldCounter0++;

                    Integer value_TestValue = getIntegerSafely(in);
                    if (value_TestValue != null) {
                        result.testValue = value_TestValue;
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
    public void write(JsonWriter out, TestNamePolicyUpperCamelCase value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        // Begin
        out.beginObject();
        int obj0 = value.testValue;
        out.name("TestValue");
        out.value(obj0);

        // End
        out.endObject();
    }
}