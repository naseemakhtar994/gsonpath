package adapter.auto.interface_example.valid;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestValidInterface_GsonTypeAdapter extends TypeAdapter<TestValidInterface> {
    private final Gson mGson;

    public TestValidInterface_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestValidInterface read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        java.lang.Integer value_Json1_Nest1 = null;
        java.lang.Integer value_value2 = null;

        int jsonFieldCounter0 = 0;
        in.beginObject();

        while (in.hasNext()) {
            if (jsonFieldCounter0 == 2) {
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

                                value_Json1_Nest1 = getIntegerSafely(in);
                                break;

                            default:
                                in.skipValue();
                                break;
                        }
                    }


                    in.endObject();
                    break;

                case "value2":
                    jsonFieldCounter0++;

                    value_value2 = getIntegerSafely(in);
                    break;

                default:
                    in.skipValue();
                    break;
            }
        }

        in.endObject();
        return new TestValidInterface_GsonPathModel(
                value_Json1_Nest1,
                value_value2
        );
    }

    @Override
    public void write(JsonWriter out, TestValidInterface value) throws IOException {
    }
}