package gsonpath.generator.adapter.auto;

import com.google.common.base.Joiner;
import org.junit.Test;

public class NestedClassTest extends BaseAutoTest {

    @Test
    public void testNestedClasses() {
        String source = Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                "public class Test {",
                "    @AutoGsonAdapter",
                "    public static class NestedTest {",
                "        public int value1;",
                "    }",
                "}"
        );

        String expectedSource = Joiner.on('\n').join(
                STANDARD_RESULT_PACKAGE_AND_IMPORTS,
                createResultHeader("Test_NestedTest", "Test.NestedTest"),
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
                "            Integer safeValue0 = getIntegerSafely(in);",
                "            if (safeValue0 != null {",
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
                createResultFooter("Test.NestedTest")
        );

        assertGeneratedContent("Test", source, "test.Test_NestedTest_GsonTypeAdapter", expectedSource);
    }

}
