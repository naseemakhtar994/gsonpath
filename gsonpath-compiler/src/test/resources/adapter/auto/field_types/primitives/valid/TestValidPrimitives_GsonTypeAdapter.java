package adapter.auto.field_types.primitives.valid;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestValidPrimitives_GsonTypeAdapter extends TypeAdapter<TestValidPrimitives> {
    private final Gson mGson;

    public TestValidPrimitives_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestValidPrimitives read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestValidPrimitives result = new TestValidPrimitives();

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

                    Boolean value_value1 = getBooleanSafely(in);
                    if (value_value1 != null) {
                        result.value1 = value_value1;
                    }
                    break;

                case "value2":
                    jsonFieldCounter0++;

                    Integer value_value2 = getIntegerSafely(in);
                    if (value_value2 != null) {
                        result.value2 = value_value2;
                    }
                    break;

                case "value3":
                    jsonFieldCounter0++;

                    Double value_value3 = getDoubleSafely(in);
                    if (value_value3 != null) {
                        result.value3 = value_value3;
                    }
                    break;

                case "value4":
                    jsonFieldCounter0++;

                    Long value_value4 = getLongSafely(in);
                    if (value_value4 != null) {
                        result.value4 = value_value4;
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
    public void write(JsonWriter out, TestValidPrimitives value) throws IOException {
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