package adapter.auto.field_types.generics;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestGenerics_GsonTypeAdapter extends TypeAdapter<TestGenerics> {
    private final Gson mGson;

    public TestGenerics_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestGenerics read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestGenerics result = new TestGenerics();

        int jsonFieldCounter0 = 0;
        in.beginObject();

        while (in.hasNext()) {
            if (jsonFieldCounter0 == 1) {
                in.skipValue();
                continue;
            }

            switch (in.nextName()) {
                case "value1":
                    jsonFieldCounter0++;

                    java.util.List<java.lang.String> safeValue0 = mGson.getAdapter(new com.google.gson.reflect.TypeToken<java.util.List<java.lang.String>>() {
                    }).read(in);
                    if (safeValue0 != null) {
                        result.value1 = safeValue0;
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
    public void write(JsonWriter out, TestGenerics value) throws IOException {
        // GsonPath does not support writing at this stage.
    }
}