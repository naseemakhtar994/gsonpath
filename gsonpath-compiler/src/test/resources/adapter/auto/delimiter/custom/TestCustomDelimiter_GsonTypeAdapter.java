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

                                Integer safeValue0 = getIntegerSafely(in);
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
        // GsonPath does not support writing at this stage.
    }
}