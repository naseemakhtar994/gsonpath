package adapter.auto.nested_json.field_nesting_autocomplete;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestFieldNestingAutocomplete_GsonTypeAdapter extends TypeAdapter<TestFieldNestingAutocomplete> {
    private final Gson mGson;

    public TestFieldNestingAutocomplete_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestFieldNestingAutocomplete read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestFieldNestingAutocomplete result = new TestFieldNestingAutocomplete();

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
                            case "value1":
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
    public void write(JsonWriter out, TestFieldNestingAutocomplete value) throws IOException {
        // GsonPath does not support writing at this stage.
    }
}