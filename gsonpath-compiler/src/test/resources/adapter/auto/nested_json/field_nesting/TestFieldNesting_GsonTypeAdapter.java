package adapter.auto.nested_json.field_nesting;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestFieldNesting_GsonTypeAdapter extends TypeAdapter<TestFieldNesting> {
    private final Gson mGson;

    public TestFieldNesting_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestFieldNesting read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestFieldNesting result = new TestFieldNesting();

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

                    Integer safeValue0 = getIntegerSafely(in);
                    if (safeValue0 != null) {
                        result.value1 = safeValue0;
                    }
                    break;

                case "Json2":
                    jsonFieldCounter0++;

                    // Ensure the object is not null.
                    if (!isValidValue(in)) {
                        break;
                    }
                    int jsonFieldCounter1 = 0;
                    in.beginObject();

                    while (in.hasNext()) {
                        if (jsonFieldCounter1 == 2) {
                            in.skipValue();
                            continue;
                        }

                        switch (in.nextName()) {
                            case "Nest1":
                                jsonFieldCounter1++;

                                Integer safeValue1 = getIntegerSafely(in);
                                if (safeValue1 != null) {
                                    result.value2 = safeValue1;
                                }
                                break;

                            case "Nest2":
                                jsonFieldCounter1++;

                                // Ensure the object is not null.
                                if (!isValidValue(in)) {
                                    break;
                                }
                                int jsonFieldCounter2 = 0;
                                in.beginObject();

                                while (in.hasNext()) {
                                    if (jsonFieldCounter2 == 2) {
                                        in.skipValue();
                                        continue;
                                    }

                                    switch (in.nextName()) {
                                        case "EndPoint1":
                                            jsonFieldCounter2++;

                                            Integer safeValue2 = getIntegerSafely(in);
                                            if (safeValue2 != null) {
                                                result.value3 = safeValue2;
                                            }
                                            break;

                                        case "EndPoint2":
                                            jsonFieldCounter2++;

                                            Integer safeValue3 = getIntegerSafely(in);
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
    public void write(JsonWriter out, TestFieldNesting value) throws IOException {
        // GsonPath does not support writing at this stage.
    }
}