package adapter.auto.naming_policy.identity;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestNamePolicyIdentity_GsonTypeAdapter extends TypeAdapter<TestNamePolicyIdentity> {
    private final Gson mGson;

    public TestNamePolicyIdentity_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestNamePolicyIdentity read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestNamePolicyIdentity result = new TestNamePolicyIdentity();

        int jsonFieldCounter0 = 0;
        in.beginObject();

        while (in.hasNext()) {
            if (jsonFieldCounter0 == 1) {
                in.skipValue();
                continue;
            }

            switch (in.nextName()) {
                case "testValue":
                    jsonFieldCounter0++;

                    Integer value_testValue = getIntegerSafely(in);
                    if (value_testValue != null) {
                        result.testValue = value_testValue;
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
    public void write(JsonWriter out, TestNamePolicyIdentity value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        // Begin
        out.beginObject();
        int obj0 = value.testValue;
        out.name("testValue");
        out.value(obj0);

        // End
        out.endObject();
    }
}