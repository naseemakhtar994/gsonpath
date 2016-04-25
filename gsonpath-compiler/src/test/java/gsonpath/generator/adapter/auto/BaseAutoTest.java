package gsonpath.generator.adapter.auto;

import com.google.common.base.Joiner;
import gsonpath.generator.BaseGeneratorTest;

public abstract class BaseAutoTest extends BaseGeneratorTest {
    static final String IMPORT_GSON_PATH_CLASS = "import gsonpath.AutoGsonAdapter;";
    static final String IMPORT_GSON_PATH_ELEMENT = "import com.google.gson.annotations.SerializedName;";
    static final String IMPORT_GSON_PATH_EXCLUDE = "import gsonpath.ExcludeField;";
    static final String IMPORT_GSON_PATH_FLATTEN = "import gsonpath.FlattenJson;";

    static final String STANDARD_RESULT_PACKAGE_AND_IMPORTS = Joiner.on('\n').join(
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

    static String createResultHeader(String adapterName, String pojoClassName) {
        return Joiner.on('\n').join(
                "public final class " + adapterName + "_GsonTypeAdapter extends TypeAdapter<" + pojoClassName + "> {",
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

    static final String STANDARD_RESULT_HEADER = createResultHeader("Test", "Test");

    static String createResultFooter(String className) {
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

    static final String STANDARD_RESULT_FOOTER = createResultFooter("Test");

    String createEmptyResultSource(String className) {
        return Joiner.on('\n').join(
                STANDARD_RESULT_PACKAGE_AND_IMPORTS,
                createResultHeader(className, className),
                createResultFooter(className)
        );
    }

}
