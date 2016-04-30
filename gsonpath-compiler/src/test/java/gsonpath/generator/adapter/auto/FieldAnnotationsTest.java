package gsonpath.generator.adapter.auto;

import com.google.common.base.Joiner;
import org.junit.Test;

public class FieldAnnotationsTest extends BaseAutoTest {

    @Test
    public void testFlattenJson() {
        String source = Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                IMPORT_GSON_PATH_FLATTEN,
                "@AutoGsonAdapter",
                "public class Test {",
                "    @FlattenJson",
                "    public String value1;",
                "}"
        );

        String expectedSource = Joiner.on('\n').join(
                STANDARD_RESULT_PACKAGE_AND_IMPORTS,
                STANDARD_RESULT_HEADER,
                "int jsonFieldCounter0 = 0;",
                "in.beginObject();",
                "",
                "while (in.hasNext()) {",
                "    if (jsonFieldCounter0 == 1) {",
                "        in.skipValue();",
                "        continue;",
                "    }",
                "",
                "    switch(in.nextName()) {",
                "        case \"value1\":",
                "            jsonFieldCounter0++;",
                "",
                "            com.google.gson.JsonElement safeValue0 = mGson.getAdapter(com.google.gson.JsonElement.class).read(in);",
                "            if (safeValue0 != null) {",
                "                result.value1 = safeValue0.toString();",
                "            }",
                "            break;",
                "",
                "        default:",
                "            in.skipValue();",
                "            break;",
                "    }",
                "}",
                "",
                "in.endObject();",
                STANDARD_RESULT_FOOTER
        );

        assertGeneratedContent(source, expectedSource);
    }

    @Test
    public void testExcludeFields() {
        String source = Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_EXCLUDE,
                "@AutoGsonAdapter",
                "public class Test {",
                "    public int element1;",
                "    @ExcludeField",
                "    public int element2;",
                "}"
        );

        String expectedSource = Joiner.on('\n').join(
                STANDARD_RESULT_PACKAGE_AND_IMPORTS,
                STANDARD_RESULT_HEADER,
                "int jsonFieldCounter0 = 0;",
                "in.beginObject();",
                "",
                "while (in.hasNext()) {",
                "    if (jsonFieldCounter0 == 1) {",
                "        in.skipValue();",
                "        continue;",
                "    }",
                "",
                "    switch(in.nextName()) {",
                "        case \"element1\":",
                "            jsonFieldCounter0++;",
                "",
                "            Integer safeValue0 = getIntegerSafely(in);",
                "            if (safeValue0 != null {",
                "                result.element1 = safeValue0;",
                "            }",
                "            break;",
                "",
                "        default:",
                "            in.skipValue();",
                "            break;",
                "    }",
                "}",
                "",
                "in.endObject();",
                STANDARD_RESULT_FOOTER
        );

        assertGeneratedContent(source, expectedSource);
    }
}
