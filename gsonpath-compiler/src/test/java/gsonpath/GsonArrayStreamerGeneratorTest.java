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
                        "package com.test;",
                        "",
                        "import static gsonpath.GsonUtil.*;",
                        "",
                        "import com.google.gson.Gson;",
                        "import com.google.gson.JsonSyntaxException;",
                        "import com.google.gson.stream.JsonReader;",
                        "import gsonpath.AbstractGsonArrayStreamer;",
                        "import gsonpath.GsonArrayStreamer;",
                        "import java.io.IOException;",
                        "import java.lang.Override;",
                        "import java.lang.String;",
                        "import java.util.List;",
                        "",
                        "public final class Test_GsonArrayStreamer extends AbstractGsonArrayStreamer<String> implements Test {",
                        "    @Override",
                        "    public String[] getArray(Gson gson, JsonReader in) throws JsonSyntaxException {",
                        "        return gson.fromJson(in, String[].class);",
                        "    }",
                        "",
                        "    @Override",
                        "    public List<String> getList(Gson gson, JsonReader in) throws JsonSyntaxException {",
                        "        return gson.fromJson(in, new com.google.gson.reflect.TypeToken<List<String>>() {}.getType());",
                        "    }",
                        "",
                        "    @Override",
                        "    public void streamArraySegmented(Gson gson, JsonReader in, int streamSize, GsonArrayStreamer.StreamCallback<String[]> callback) throws JsonSyntaxException {",
                        "        String[] results = new String[streamSize];",
                        "        StreamCallback.StreamHandler callbackResponse = new StreamCallback.StreamHandler();",
                        "        int resultIndex = -1;",
                        "",
                        "        try {",
                        "            in.beginArray();",
                        "            while (in.hasNext()) {",
                        "                results[++resultIndex] = gson.fromJson(in, String.class);",
                        "",
                        "                // Once we reach the requested stream size, we should return the results.",
                        "                if (resultIndex == streamSize - 1) {",
                        "                    callback.onValueParsed(results, streamSize, callbackResponse);",
                        "",
                        "                    if (callbackResponse.isStreamStopped()) {",
                        "                        // Since we stop the stream mid-way, we cannot call end-array safely.",
                        "                        return;",
                        "                    }",
                        "",
                        "                    resultIndex = -1;",
                        "                }",
                        "            }",
                        "            in.endArray();",
                        "        } catch (IOException e) {",
                        "            throw new JsonSyntaxException(e);",
                        "        }",
                        "",
                        "        // We have left over results to send back.",
                        "        if (resultIndex >= 0) {",
                        "            // To avoid creating a new array, we will simply remove the invalid results at the end.",
                        "            for (int i = resultIndex + 1; i < streamSize; i++) {",
                        "                results[i] = null;",
                        "            }",
                        "",
                        "            callback.onValueParsed(results, resultIndex + 1, callbackResponse);",
                        "        }",
                        "    }",
                        "}"
                ));

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

}
