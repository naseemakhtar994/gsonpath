package gsonpath;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Created by Lachlan on 2/03/2016.
 */
public class GsonArrayStreamerGeneratorTest {

    private static final String STANDARD_PACKAGE_NAME = "package com.test;";

    private static final String IMPORT_GSON_PATH_CLASS = "import gsonpath.AutoGsonArrayStreamer;";
    private static final String IMPORT_GSON_PATH_ADAPTER = "import gsonpath.GsonArrayStreamer;";
    private static final String IMPORT_GSON_PATH_ELEMENT = "import com.google.gson.annotations.SerializedName;";

    private static final String STANDARD_RESULT_PACKAGE_AND_IMPORTS = Joiner.on('\n').join(
            STANDARD_PACKAGE_NAME,
            "",
            "import static gsonpath.GsonUtil.*;",
            "",
            "import com.google.gson.Gson;",
            "import com.google.gson.TypeAdapter;",
            "import com.google.gson.stream.JsonReader;",
            "import com.google.gson.stream.JsonWriter;",
            "import java.io.IOException;",
            "import java.lang.Override;",
            ""
    );

    private static String createResultHeader(String adapterName, String pojoClassName) {
        return Joiner.on('\n').join(
                "public final class " + adapterName + "_GsonArrayStreamer implements " + pojoClassName + " {",
                "    private final Gson mGson;",
                "",
                "    public " + adapterName + "_GsonTypeAdapter(Gson gson) {",
                "        this.mGson = gson;",
                "    }",
                "",
                "    @Override",
                "    public " + pojoClassName + " read(JsonReader in) throws IOException {",
                "        " + pojoClassName + " result = new " + pojoClassName + "();"
        );
    }

    private static final String STANDARD_RESULT_HEADER = createResultHeader("Test", "Test");

    private static String createResultFooter(String className) {
        return Joiner.on('\n').join(
                "        return result;",
                "    }",
                "",
                "    @Override",
                "    public void write(JsonWriter out, " + className + " value) throws IOException {",
                "        // GsonPath does not support writing at this stage.",
                "    }",
                "}"
        );
    }

    private static final String STANDARD_RESULT_FOOTER = createResultFooter("Test");

    @Test
    public void testArrayAdapterNoRoot() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ADAPTER,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonArrayStreamer",
                "public interface Test extends GsonArrayStreamer<java.lang.String> {",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_GsonArrayStreamer",
                Joiner.on('\n').join(
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
                ));

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

}
