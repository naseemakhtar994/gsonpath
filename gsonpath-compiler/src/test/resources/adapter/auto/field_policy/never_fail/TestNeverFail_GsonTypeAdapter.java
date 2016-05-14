package adapter.auto.field_policy.never_fail;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestNeverFail_GsonTypeAdapter extends TypeAdapter<TestNeverFail> {
    private final Gson mGson;

    public TestNeverFail_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestNeverFail read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestNeverFail result = new TestNeverFail();

        int jsonFieldCounter0 = 0;
        in.beginObject();

        while (in.hasNext()) {
            if (jsonFieldCounter0 == 3) {
                in.skipValue();
                continue;
            }

            switch (in.nextName()) {
                case "optional1":
                    jsonFieldCounter0++;

                    Integer safeValue0 = getIntegerSafely(in);
                    if (safeValue0 != null) {
                        result.optional1 = safeValue0;
                    }
                    break;

                case "optional2":
                    jsonFieldCounter0++;

                    Integer safeValue1 = getIntegerSafely(in);
                    if (safeValue1 != null) {
                        result.optional2 = safeValue1;
                    }
                    break;

                case "optional3":
                    jsonFieldCounter0++;

                    Integer safeValue2 = getIntegerSafely(in);
                    if (safeValue2 != null) {
                        result.optional3 = safeValue2;
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
    public void write(JsonWriter out, TestNeverFail value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        // Begin
        out.beginObject();
        int obj0 = value.optional1;
        out.name("optional1");
        out.value(obj0);

        int obj1 = value.optional2;
        out.name("optional2");
        out.value(obj1)

        int obj2 = value.optional3;
        out.name("optional3");
        out.value(obj2);
        
        // End
        out.endObject();
    }
}