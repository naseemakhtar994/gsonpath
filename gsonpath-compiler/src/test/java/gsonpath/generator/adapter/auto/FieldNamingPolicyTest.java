package gsonpath.generator.adapter.auto;

import com.google.common.base.Joiner;
import com.google.gson.FieldNamingPolicy;
import org.junit.Test;

/**
 * Created by Lachlan on 2/03/2016.
 */
public class FieldNamingPolicyTest extends BaseAutoTest {

    private void testPolicy(FieldNamingPolicy policy, String fieldName, String jsonName) {
        String source = Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter(fieldNamingPolicy = com.google.gson.FieldNamingPolicy." + policy.name() + ")",
                "public class Test {",
                "    public int " + fieldName + ";",
                "}"
        );

        String typeAdapterSource = Joiner.on('\n').join(
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
                "        case \"" + jsonName + "\":",
                "            jsonFieldCounter0++;",
                "            result." + fieldName + " = in.nextInt();",
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

        assertGeneratedContent(source, typeAdapterSource);
    }

    @Test
    public void testIdentity() {
        testPolicy(FieldNamingPolicy.IDENTITY, "testValue", "testValue");
    }

    @Test
    public void testLowerCaseWithDashes() {
        testPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES, "testValue", "test-value");
    }

    @Test
    public void testLowerCaseWithUnderscores() {
        testPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES, "testValue", "test_value");
    }

    @Test
    public void testUpperCamelCase() {
        testPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE, "testValue", "TestValue");
    }

    @Test
    public void testUpperCamelCaseWithSpaces() {
        testPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES, "testValue", "Test Value");
    }
}
