package adapter.auto.defaults;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;

public final class TestDefaultsOverrideInheritanceModel_GsonTypeAdapter extends TypeAdapter<TestDefaultsOverrideInheritanceModel> {
    private final Gson mGson;

    public TestDefaultsOverrideInheritanceModel_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestDefaultsOverrideInheritanceModel read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestDefaultsOverrideInheritanceModel result = new TestDefaultsOverrideInheritanceModel();

        int jsonFieldCounter0 = 0;
        in.beginObject();

        while (in.hasNext()) {
            if (jsonFieldCounter0 == 2) {
                in.skipValue();
                continue;
            }

            switch (in.nextName()) {
                case "path":
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
                            case "expectedValue":
                                jsonFieldCounter1++;

                                Integer value_path_expectedValue = getIntegerSafely(in);
                                if (value_path_expectedValue != null) {
                                    result.expectedValue = value_path_expectedValue;
                                }
                                break;

                            default:
                                in.skipValue();
                                break;
                        }
                    }


                    in.endObject();
                    break;

                case "notIgnored":
                    jsonFieldCounter0++;

                    String value_notIgnored = getStringSafely(in);
                    if (value_notIgnored != null) {
                        result.notIgnored = value_notIgnored;
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
    public void write(JsonWriter out, TestDefaultsOverrideInheritanceModel value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        // Begin
        out.beginObject();

        // Begin path
        out.name("path");
        out.beginObject();
        Integer obj0 = value.expectedValue;
        if (obj0 != null) {
            out.name("expectedValue");
            out.value(obj0);
        }

        // End path
        out.endObject();
        String obj1 = value.notIgnored;
        if (obj1 != null) {
            out.name("notIgnored");
            out.value(obj1);
        }

        // End
        out.endObject();
    }
}