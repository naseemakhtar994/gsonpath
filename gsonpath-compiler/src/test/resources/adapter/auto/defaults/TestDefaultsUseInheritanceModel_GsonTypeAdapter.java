package adapter.auto.defaults;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.Integer;
import java.lang.Override;

public final class TestDefaultsUseInheritanceModel_GsonTypeAdapter extends TypeAdapter<TestDefaultsUseInheritanceModel> {
    private static final int MANDATORY_INDEX_EXPECTEDVALUE = 0;

    private static final int MANDATORY_FIELDS_SIZE = 1;

    private final Gson mGson;

    public TestDefaultsUseInheritanceModel_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public TestDefaultsUseInheritanceModel read(JsonReader in) throws IOException {

        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        TestDefaultsUseInheritanceModel result = new TestDefaultsUseInheritanceModel();
        boolean[] mandatoryFieldsCheckList = new boolean[MANDATORY_FIELDS_SIZE];

        int jsonFieldCounter0 = 0;
        in.beginObject();

        while (in.hasNext()) {
            if (jsonFieldCounter0 == 1) {
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
                                    mandatoryFieldsCheckList[MANDATORY_INDEX_EXPECTEDVALUE] = true;

                                } else {
                                    throw new gsonpath.JsonFieldMissingException("Mandatory JSON element 'path$expectedValue' was null for class 'adapter.auto.defaults.TestDefaultsUseInheritanceModel'");
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

        // Mandatory object validation
        for (int mandatoryFieldIndex = 0; mandatoryFieldIndex < MANDATORY_FIELDS_SIZE; mandatoryFieldIndex++) {

            // Check if a mandatory value is missing.
            if (!mandatoryFieldsCheckList[mandatoryFieldIndex]) {

                // Find the field name of the missing json value.
                String fieldName = null;
                switch (mandatoryFieldIndex) {
                    case MANDATORY_INDEX_EXPECTEDVALUE:
                        fieldName = "path$expectedValue";
                        break;

                }
                throw new gsonpath.JsonFieldMissingException("Mandatory JSON element '" + fieldName + "' was not found for class 'adapter.auto.defaults.TestDefaultsUseInheritanceModel'");
            }
        }
        return result;
    }

    @Override
    public void write(JsonWriter out, TestDefaultsUseInheritanceModel value) throws IOException {
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
        out.name("expectedValue");
        if (obj0 != null) {
            out.value(obj0);
        } else {
            out.nullValue();
        }

        // End path
        out.endObject();
        // End
        out.endObject();
    }
}