package gsonpath.generator.adapter.auto;

import com.google.common.base.Joiner;
import org.junit.Test;

public class FieldTypesTest extends BaseAutoTest {

    @Test
    public void testPrimitives() {
        String source = Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter",
                "public class Test {",
                "    public boolean value1;",
                "    public int value2;",
                "    public double value3;",
                "    public long value4;",
                "}"
        );

        String expectedSource = Joiner.on('\n').join(
                STANDARD_RESULT_PACKAGE_AND_IMPORTS,
                STANDARD_RESULT_HEADER,
                "int jsonFieldCounter0 = 0;",
                "in.beginObject();",
                "",
                "while (in.hasNext()) {",
                "    if (jsonFieldCounter0 == 4) {",
                "        in.skipValue();",
                "        continue;",
                "    }",
                "",
                "    switch(in.nextName()) {",
                "        case \"value1\":",
                "            jsonFieldCounter0++;",
                "            result.value1 = in.nextBoolean();",
                "            break;",
                "",
                "        case \"value2\":",
                "            jsonFieldCounter0++;",
                "            result.value2 = in.nextInt();",
                "            break;",
                "",
                "        case \"value3\":",
                "            jsonFieldCounter0++;",
                "            result.value3 = in.nextDouble();",
                "            break;",
                "",
                "        case \"value4\":",
                "            jsonFieldCounter0++;",
                "            result.value4 = in.nextLong();",
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
    public void testBoxedPrimitives() {
        String source = Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter",
                "public class Test {",
                "    public String value1;",
                "    public Boolean value2;",
                "    public Integer value3;",
                "    public Double value4;",
                "    public Long value5;",
                "}"
        );

        String expectedSource = Joiner.on('\n').join(
                STANDARD_RESULT_PACKAGE_AND_IMPORTS,
                STANDARD_RESULT_HEADER,
                "int jsonFieldCounter0 = 0;",
                "in.beginObject();",
                "",
                "while (in.hasNext()) {",
                "    if (jsonFieldCounter0 == 5) {",
                "        in.skipValue();",
                "        continue;",
                "    }",
                "",
                "    switch(in.nextName()) {",
                "        case \"value1\":",
                "            jsonFieldCounter0++;",
                "",
                "            String safeValue0 = getStringSafely(in);",
                "            if (safeValue0 != null {",
                "                result.value1 = safeValue0;",
                "            }",
                "            break;",
                "",
                "        case \"value2\":",
                "            jsonFieldCounter0++;",
                "",
                "            Boolean safeValue1 = getBooleanSafely(in);",
                "            if (safeValue1 != null {",
                "                result.value2 = safeValue1;",
                "            }",
                "            break;",
                "",
                "        case \"value3\":",
                "            jsonFieldCounter0++;",
                "",
                "            Integer safeValue2 = getIntegerSafely(in);",
                "            if (safeValue2 != null {",
                "                result.value3 = safeValue2;",
                "            }",
                "            break;",
                "",
                "        case \"value4\":",
                "            jsonFieldCounter0++;",
                "",
                "            Double safeValue3 = getDoubleSafely(in);",
                "            if (safeValue3 != null {",
                "                result.value4 = safeValue3;",
                "            }",
                "            break;",
                "",
                "        case \"value5\":",
                "            jsonFieldCounter0++;",
                "",
                "            Long safeValue4 = getLongSafely(in);",
                "            if (safeValue4 != null {",
                "                result.value5 = safeValue4;",
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
    public void testGenericFields() {
        String source = Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                IMPORT_GSON_PATH_FLATTEN,
                "@AutoGsonAdapter",
                "public class Test {",
                "    public java.util.List<String> value1;",
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
                "            java.util.List<java.lang.String> safeValue0 = mGson.getAdapter(new com.google.gson.reflect.TypeToken<java.util.List<java.lang.String>>(){}).read(in);",
                "            if (safeValue0 != null) {",
                "                result.value1 = safeValue0;",
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
    public void testCustomFieldType() {
        String source = Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter",
                "public class Test {",
                "    public java.util.Currency value1;",
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
                "            java.util.Currency safeValue0 = mGson.getAdapter(java.util.Currency.class).read(in);",
                "            if (safeValue0 != null) {",
                "                result.value1 = safeValue0;",
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
