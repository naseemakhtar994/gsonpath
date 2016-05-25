package adapter.auto.field_policy.validate_explicit_non_null;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Integer;
import java.lang.Override;

public final class TestValidateExplicitNonNull_GsonTypeAdapter extends TypeAdapter<TestValidateExplicitNonNull> {
    private static final int MANDATORY_INDEX_MANDATORY1 = 0;
    private static final int MANDATORY_INDEX_MANDATORY2 = 1;
    private static final int MANDATORY_FIELDS_SIZE = 2;

    private final Gson mGson;

    public TestValidateExplicitNonNull_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestValidateExplicitNonNull read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestValidateExplicitNonNull result = new TestValidateExplicitNonNull();
        boolean[] mandatoryFieldsCheckList = new boolean[MANDATORY_FIELDS_SIZE];

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
                        mandatoryFieldsCheckList[MANDATORY_INDEX_MANDATORY1] = true;
                    } else {
                        throw new gsonpath.JsonFieldMissingException("Mandatory JSON element 'mandatory1' was null for class 'adapter.auto.field_policy.validate_explicit_non_null.TestValidateExplicitNonNull'");
                    }
                    break;

                case "mandatory2":
                    jsonFieldCounter0++;

                    Integer safeValue1 = getIntegerSafely(in);
                    if (safeValue1 != null) {
                        result.mandatory2 = safeValue1;
                        mandatoryFieldsCheckList[MANDATORY_INDEX_MANDATORY2] = true;
                    } else {
                        throw new gsonpath.JsonFieldMissingException("Mandatory JSON element 'mandatory2' was null for class 'adapter.auto.field_policy.validate_explicit_non_null.TestValidateExplicitNonNull'");
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

        // Mandatory object validation
        for (int mandatoryFieldIndex = 0; mandatoryFieldIndex < MANDATORY_FIELDS_SIZE; mandatoryFieldIndex++) {

            // Check if a mandatory value is missing.
            if (!mandatoryFieldsCheckList[mandatoryFieldIndex]) {

                // Find the field name of the missing json value.
                String fieldName = null;
                switch (mandatoryFieldIndex) {
                    case MANDATORY_INDEX_MANDATORY1:
                        fieldName = "mandatory1";
                        break;

                    case MANDATORY_INDEX_MANDATORY2:
                        fieldName = "mandatory2";
                        break;
                }
                throw new gsonpath.JsonFieldMissingException("Mandatory JSON element '" + fieldName + "' was not found for class 'adapter.auto.field_policy.validate_explicit_non_null.TestValidateExplicitNonNull'");
            }
        }

        return result;
    }

    @Override
    public void write(JsonWriter out, TestValidateExplicitNonNull value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        // Begin
        out.beginObject();
        Integer obj0 = value.mandatory1;
        if (obj0 != null) {
            out.name("mandatory1");
            out.value(obj0);
        }

        Integer obj1 = value.mandatory2;
        if (obj1 != null) {
            out.name("mandatory2");
            out.value(obj1);
        }

        Integer obj2 = value.optional1;
        if (obj2 != null) {
            out.name("optional1");
            out.value(obj2);
        }

        // End
        out.endObject();
    }
}