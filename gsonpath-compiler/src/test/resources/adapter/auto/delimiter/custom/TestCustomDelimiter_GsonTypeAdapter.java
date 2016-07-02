package adapter.auto.delimiter.custom;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestCustomDelimiter_GsonTypeAdapter extends TypeAdapter<TestCustomDelimiter> {
    private final Gson mGson;

    public TestCustomDelimiter_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestCustomDelimiter read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestCustomDelimiter result = new TestCustomDelimiter();

        int jsonFieldCounter0 = 0;
        in.beginObject();

        while (in.hasNext()) {
            if (jsonFieldCounter0 == 1) {
                in.skipValue();
                continue;
            }

            switch (in.nextName()) {
                case "Json1":
                    jsonFieldCounter0++;

                    // Ensure the object is not null.
                    if (!isValidValue(in)) {
                        break;
                    }
                    int jsonFieldCounter1 = 0;
                    in.beginObject();

                    while (in.hasNext()) {
                        if (jsonFieldCounter1 == 1) {
                            in.skipValue();
                            continue;
                        }

                        switch (in.nextName()) {
                            case "Nest1":
                                jsonFieldCounter1++;

                                Integer value_Json1_Nest1 = getIntegerSafely(in);
                                if (value_Json1_Nest1 != null) {
                                    result.value1 = value_Json1_Nest1;
                                }
                                break;

                            default:
                                in.skipValue();
                                break;
                        }
                    }


                    in.endObject();
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
    public void write(JsonWriter out, TestCustomDelimiter value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        // Begin
        out.beginObject();

        // Begin Json1
        out.name("Json1");
        out.beginObject();
        int obj0 = value.value1;
        out.name("Nest1");
        out.value(obj0);

        // End Json1
        out.endObject();
        // End
        out.endObject();
    }
}