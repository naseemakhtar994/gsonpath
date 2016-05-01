package gsonpath.generator.adapter.auto;

import com.google.common.base.Joiner;
import org.junit.Test;

public class DelimiterTest extends BaseAutoTest {

    private static String EXPECTED_BASIC_SOURCE = Joiner.on('\n').join(
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
            "        case \"Json1\":",
            "            jsonFieldCounter0++;",
            "",
            "            // Ensure the object is not null.",
            "            if (!isValidValue(in)) {",
            "                break;",
            "            }",
            "",
            "            int jsonFieldCounter1 = 0;",
            "            in.beginObject();",
            "",
            "            while (in.hasNext()) {",
            "                if (jsonFieldCounter1 == 1) {",
            "                    in.skipValue();",
            "                    continue;",
            "                }",
            "",
            "                switch(in.nextName()) {",
            "                    case \"Nest1\":",
            "                        jsonFieldCounter1++;",
            "",
            "                        Integer safeValue0 = getIntegerSafely(in);",
            "                        if (safeValue0 != null {",
            "                            result.value1 = safeValue0;",
            "                        }",
            "                        break;",
            "",
            "                    default:",
            "                        in.skipValue();",
            "                        break;",
            "                }",
            "            }",
            "",
            "            in.endObject();",
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

    @Test
    public void testStandardDelimiter() {
        String source = Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter",
                "public class Test {",
                "    @SerializedName(\"Json1.Nest1\")",
                "    public int value1;",
                "}"
        );

        assertGeneratedContent(source, EXPECTED_BASIC_SOURCE);
    }

    @Test
    public void testCustomDelimiter() {
        String source = Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter(flattenDelimiter = '$')",
                "public class Test {",
                "    @SerializedName(\"Json1$Nest1\")",
                "    public int value1;",
                "}"
        );

        assertGeneratedContent(source, EXPECTED_BASIC_SOURCE);
    }

    @Test
    public void testMultipleDelimiters() {
        // Within this test we want to ensure when supplying a custom delimiter, that the '.' delimiter isn't applied.
        String source = Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter(flattenDelimiter = '$')",
                "public class Test {",
                "    @SerializedName(\"Json1$Nest1\")",
                "    public int value1;",
                "",
                "    @SerializedName(\"Json2.Nest1\")",
                "    public int value2;",
                "}"
        );

        String expectedSource = Joiner.on('\n').join(
                STANDARD_RESULT_PACKAGE_AND_IMPORTS,
                STANDARD_RESULT_HEADER,
                "int jsonFieldCounter0 = 0;",
                "in.beginObject();",
                "",
                "while (in.hasNext()) {",
                "    if (jsonFieldCounter0 == 2) {",
                "        in.skipValue();",
                "        continue;",
                "    }",
                "",
                "    switch(in.nextName()) {",
                "        case \"Json1\":",
                "            jsonFieldCounter0++;",
                "",
                "            // Ensure the object is not null.",
                "            if (!isValidValue(in)) {",
                "                break;",
                "            }",
                "",
                "            int jsonFieldCounter1 = 0;",
                "            in.beginObject();",
                "",
                "            while (in.hasNext()) {",
                "                if (jsonFieldCounter1 == 1) {",
                "                    in.skipValue();",
                "                    continue;",
                "                }",
                "",
                "                switch(in.nextName()) {",
                "                    case \"Nest1\":",
                "                        jsonFieldCounter1++;",
                "",
                "                        Integer safeValue0 = getIntegerSafely(in);",
                "                        if (safeValue0 != null {",
                "                            result.value1 = safeValue0;",
                "                        }",
                "                        break;",
                "",
                "                    default:",
                "                        in.skipValue();",
                "                        break;",
                "                }",
                "            }",
                "",
                "            in.endObject();",
                "            break;",
                "",
                "        case \"Json2.Nest1\":",
                "            jsonFieldCounter0++;",
                "",
                "            Integer safeValue1 = getIntegerSafely(in);",
                "            if (safeValue1 != null {",
                "                result.value2 = safeValue1;",
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
