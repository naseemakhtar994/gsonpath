package adapter.auto.field_policy.fail_all_except_optional;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Override;

public final class TestFailAllExceptOptional_GsonTypeAdapter extends TypeAdapter<TestFailAllExceptOptional> {
    private final Gson mGson;

    public TestFailAllExceptOptional_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestFailAllExceptOptional read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestFailAllExceptOptional result = new TestFailAllExceptOptional();

        int jsonFieldCounter0 = 0;
        in.beginObject();

        while (in.hasNext()) {
            if (jsonFieldCounter0 == 3) {
                in.skipValue();
                continue;
            }

            switch (in.nextName()) {
                case "mandatory1":
                    jsonFieldCounter0++;

                    Integer safeValue0 = getIntegerSafely(in);
                    if (safeValue0 != null) {
                        result.mandatory1 = safeValue0;
                    } else {
                        throw new gsonpath.JsonFieldMissingException("Mandatory JSON element 'mandatory1' not found for class 'adapter.auto.field_policy.fail_all_except_optional.TestFailAllExceptOptional'");
                    }
                    break;

                case "mandatory2":
                    jsonFieldCounter0++;

                    Integer safeValue1 = getIntegerSafely(in);
                    if (safeValue1 != null) {
                        result.mandatory2 = safeValue1;
                    } else {
                        throw new gsonpath.JsonFieldMissingException("Mandatory JSON element 'mandatory2' not found for class 'adapter.auto.field_policy.fail_all_except_optional.TestFailAllExceptOptional'");
                    }
                    break;

                case "optional1":
                    jsonFieldCounter0++;

                    Integer safeValue2 = getIntegerSafely(in);
                    if (safeValue2 != null) {
                        result.optional1 = safeValue2;
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
    public void write(JsonWriter out, TestFailAllExceptOptional value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        // Begin
        out.beginObject();
        int obj0 = value.mandatory1;
        out.name("mandatory1");
        out.value(obj0);

        int obj1 = value.mandatory2;
        out.name("mandatory2");
        out.value(obj1)

        int obj2 = value.optional1;
        out.name("optional1");
        out.value(obj2);

        // End
        out.endObject();
    }
}