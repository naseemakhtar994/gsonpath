package adapter.auto.field_types.primitives;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestPrimitives_GsonTypeAdapter extends TypeAdapter<TestPrimitives> {
    private final Gson mGson;

    public TestPrimitives_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestPrimitives read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestPrimitives result = new TestPrimitives();

        int jsonFieldCounter0 = 0;
        in.beginObject();

        while (in.hasNext()) {
            if (jsonFieldCounter0 == 4) {
                in.skipValue();
                continue;
            }

            switch (in.nextName()) {
                case "value1":
                    jsonFieldCounter0++;

                    Boolean safeValue0 = getBooleanSafely(in);
                    if (safeValue0 != null) {
                        result.value1 = safeValue0;
                    }
                    break;

                case "value2":
                    jsonFieldCounter0++;

                    Integer safeValue1 = getIntegerSafely(in);
                    if (safeValue1 != null) {
                        result.value2 = safeValue1;
                    }
                    break;

                case "value3":
                    jsonFieldCounter0++;

                    Double safeValue2 = getDoubleSafely(in);
                    if (safeValue2 != null) {
                        result.value3 = safeValue2;
                    }
                    break;

                case "value4":
                    jsonFieldCounter0++;

                    Long safeValue3 = getLongSafely(in);
                    if (safeValue3 != null) {
                        result.value4 = safeValue3;
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
    public void write(JsonWriter out, TestPrimitives value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        // Begin
        out.beginObject();
        boolean obj0 = value.value1;
        out.name("value1");
        out.value(obj0);

        int obj1 = value.value2;
        out.name("value2");
        out.value(obj1);

        double obj2 = value.value3;
        out.name("value3");
        out.value(obj2);

        long obj3 = value.value4;
        out.name("value4");
        out.value(obj3);

        // End
        out.endObject();
    }
}