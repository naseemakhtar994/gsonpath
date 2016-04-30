package gsonpath.generator.adapter.auto;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import gsonpath.GsonProcessor;
import org.junit.Test;

import javax.tools.JavaFileObject;
import java.util.ArrayList;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public class InheritanceTest extends BaseAutoTest {

    @Test
    public void testInheritance() {

        JavaFileObject source1 = JavaFileObjects.forSourceString("test.BaseTest", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_ELEMENT,
                "public class BaseTest {",
                "    @SerializedName(\"Json1\")",
                "    public int value1;",
                "}"
        ));

        JavaFileObject source2 = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                "@AutoGsonAdapter",
                "public class Test extends BaseTest {",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_GsonTypeAdapter", Joiner.on('\n').join(
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
                STANDARD_RESULT_FOOTER
        ));

        ArrayList<JavaFileObject> sources = new ArrayList<>();
        sources.add(source1);
        sources.add(source2);

        assertAbout(javaSources()).that(sources)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

}
