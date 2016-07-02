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
    private static final int MANDATORY_INDEX_MANDATORY3 = 2;
    private static final int MANDATORY_FIELDS_SIZE = 3;

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
            if (jsonFieldCounter0 == 4) {
                in.skipValue();
                continue;
            }

            switch (in.nextName()) {
                case "mandatory1":
                    jsonFieldCounter0++;

                    Integer value_mandatory1 = getIntegerSafely(in);
                    if (value_mandatory1 != null) {
                        result.mandatory1 = value_mandatory1;
                        mandatoryFieldsCheckList[MANDATORY_INDEX_MANDATORY1] = true;
                    } else {
                        throw new gsonpath.JsonFieldMissingException("Mandatory JSON element 'mandatory1' was null for class 'adapter.auto.field_policy.validate_explicit_non_null.TestValidateExplicitNonNull'");
                    }
                    break;

                case "mandatory2":
                    jsonFieldCounter0++;

                    Integer value_mandatory2 = getIntegerSafely(in);
                    if (value_mandatory2 != null) {
                        result.mandatory2 = value_mandatory2;
                        mandatoryFieldsCheckList[MANDATORY_INDEX_MANDATORY2] = true;
                    } else {
                        throw new gsonpath.JsonFieldMissingException("Mandatory JSON element 'mandatory2' was null for class 'adapter.auto.field_policy.validate_explicit_non_null.TestValidateExplicitNonNull'");
                    }
                    break;

                case "mandatory3":
                    jsonFieldCounter0++;

                    Integer value_mandatory3 = getIntegerSafely(in);
                    if (value_mandatory3 != null) {
                        result.mandatory3 = value_mandatory3;
                        mandatoryFieldsCheckList[MANDATORY_INDEX_MANDATORY3] = true;
                    } else {
                        throw new gsonpath.JsonFieldMissingException("Mandatory JSON element 'mandatory3' was null for class 'adapter.auto.field_policy.validate_explicit_non_null.TestValidateExplicitNonNull'");
                    }
                    break;

                case "optional1":
                    jsonFieldCounter0++;

                    Integer value_optional1 = getIntegerSafely(in);
                    if (value_optional1 != null) {
                        result.optional1 = value_optional1;
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

                    case MANDATORY_INDEX_MANDATORY3:
                        fieldName = "mandatory3";
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

        int obj2 = value.mandatory3;
        out.name("mandatory3");
        out.value(obj2);

        Integer obj3 = value.optional1;
        if (obj3 != null) {
            out.name("optional1");
            out.value(obj3);
        }

        // End
        out.endObject();
    }
}