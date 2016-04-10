package gsonpath;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import java.util.ArrayList;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

/**
 * Created by Lachlan on 2/03/2016.
 */
public class GsonProcessorTest {

    private static final String STANDARD_PACKAGE_NAME = "package com.test;";

    private static final String IMPORT_GSON_PATH_CLASS = "import gsonpath.AutoGsonAdapter;";
    private static final String IMPORT_GSON_PATH_ELEMENT = "import com.google.gson.annotations.SerializedName;";
    private static final String IMPORT_GSON_PATH_EXCLUDE = "import gsonpath.ExcludeField;";
    private static final String IMPORT_GSON_PATH_FLATTEN = "import gsonpath.FlattenJson;";

    private static final String STANDARD_RESULT_PACKAGE_AND_IMPORTS = Joiner.on('\n').join(
            STANDARD_PACKAGE_NAME,
            "",
            "import static gsonpath.GsonPathUtil.*;",
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

    private JavaFileObject createEmptyResultSource(String className) {
        return JavaFileObjects.forSourceString(String.format("test.%s_GsonTypeAdapter", className),
                Joiner.on('\n').join(
                        STANDARD_RESULT_PACKAGE_AND_IMPORTS,
                        createResultHeader(className, className),
                        createResultFooter(className)
                )
        );
    }

    private void assertEmptyFile(JavaFileObject source) {
        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(createEmptyResultSource("Test"));
    }

    /**
     * Tests the output generated when only a {@link AutoGsonAdapter} annotation is used.
     */
    @Test
    public void testAutoGsonAdapterOnly() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                "@AutoGsonAdapter",
                "public class Test {",
                "}"
        ));

        assertEmptyFile(source);
    }

    @Test
    public void testGsonPathWithPartialFieldName() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter",
                "public class Test {",
                "    @SerializedName(\"Json1.\")",
                "    public int value1;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_GsonTypeAdapter",
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
                        "                    case \"value1\":",
                        "                        jsonFieldCounter1++;",
                        "                        result.value1 = in.nextInt();",
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
                ));

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    @Test
    public void testGsonPathWithPrimitives() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
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
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_GsonTypeAdapter",
                Joiner.on('\n').join(
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
                ));

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    @Test
    public void testGsonPathWithBoxedPrimitives() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter",
                "public class Test {",
                "    @SerializedName(\"Json1\")",
                "    public String value1;",
                "    public Boolean value2;",
                "    public Integer value3;",
                "    public Double value4;",
                "    public Long value5;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_GsonTypeAdapter",
                Joiner.on('\n').join(
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
                        "        case \"Json1\":",
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
                ));

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    @Test
    public void testGsonPathWithNestedPrimitives() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter",
                "public class Test {",
                "    @SerializedName(\"Json1\")",
                "    public int value1;",
                "    @SerializedName(\"Json2.Nest1\")",
                "    public int value2;",
                "    @SerializedName(\"Json2.Nest2.EndPoint1\")",
                "    public int value3;",
                "    @SerializedName(\"Json2.Nest2.EndPoint2\")",
                "    public int value4;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_GsonTypeAdapter",
                Joiner.on('\n').join(
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
                        "            result.value1 = in.nextInt();",
                        "            break;",
                        "",
                        "        case \"Json2\":",
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
                        "                if (jsonFieldCounter1 == 2) {",
                        "                    in.skipValue();",
                        "                    continue;",
                        "                }",
                        "",
                        "                switch(in.nextName()) {",
                        "                    case \"Nest1\":",
                        "                        jsonFieldCounter1++;",
                        "                        result.value2 = in.nextInt();",
                        "                        break;",
                        "",
                        "                    case \"Nest2\":",
                        "                        jsonFieldCounter1++;",
                        "",
                        "                        // Ensure the object is not null.",
                        "                        if (!isValidValue(in)) {",
                        "                            break;",
                        "                        }",
                        "",
                        "                        int jsonFieldCounter2 = 0;",
                        "                        in.beginObject();",
                        "",
                        "                        while (in.hasNext()) {",
                        "                            if (jsonFieldCounter2 == 2) {",
                        "                                in.skipValue();",
                        "                                continue;",
                        "                            }",
                        "",
                        "                            switch(in.nextName()) {",
                        "                                case \"EndPoint1\":",
                        "                                    jsonFieldCounter2++;",
                        "                                    result.value3 = in.nextInt();",
                        "                                    break;",
                        "",
                        "                                case \"EndPoint2\":",
                        "                                    jsonFieldCounter2++;",
                        "                                    result.value4 = in.nextInt();",
                        "                                    break;",
                        "",
                        "                                default:",
                        "                                    in.skipValue();",
                        "                                    break;",
                        "                            }",
                        "                        }",
                        "",
                        "                        in.endObject();",
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
                ));

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    @Test
    public void testGsonPathWithRootField() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter(rootField = \"Root.Nest1\")",
                "public class Test {",
                "    public int value1;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_GsonTypeAdapter",
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
                        "        case \"Root\":",
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
                        "                        // Ensure the object is not null.",
                        "                        if (!isValidValue(in)) {",
                        "                            break;",
                        "                        }",
                        "",
                        "                        int jsonFieldCounter2 = 0;",
                        "                        in.beginObject();",
                        "",
                        "                        while (in.hasNext()) {",
                        "                            if (jsonFieldCounter2 == 1) {",
                        "                                in.skipValue();",
                        "                                continue;",
                        "                            }",
                        "",
                        "                            switch(in.nextName()) {",
                        "                                case \"value1\":",
                        "                                    jsonFieldCounter2++;",
                        "                                    result.value1 = in.nextInt();",
                        "                                    break;",
                        "",
                        "                                default:",
                        "                                    in.skipValue();",
                        "                                    break;",
                        "                            }",
                        "                        }",
                        "",
                        "                        in.endObject();",
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
                ));

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    @Test
    public void testGsonPathInheritance() {

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

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_GsonTypeAdapter",
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
                        "            result.value1 = in.nextInt();",
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

    @Test
    public void testGsonPathNested() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                "public class Test {",
                "    @AutoGsonAdapter",
                "    public static class NestedTest {",
                "        public int value1;",
                "    }",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.NestedTest_GsonTypeAdapter",
                Joiner.on('\n').join(
                        STANDARD_RESULT_PACKAGE_AND_IMPORTS,
                        createResultHeader("NestedTest", "Test.NestedTest"),
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
                        "            result.value1 = in.nextInt();",
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
                ));

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    @Test
    public void testGsonPathCollapseJson() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                IMPORT_GSON_PATH_FLATTEN,
                "@AutoGsonAdapter",
                "public class Test {",
                "    @FlattenJson",
                "    @SerializedName(\"Json1\")",
                "    public String value1;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_GsonTypeAdapter",
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
                ));

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    @Test
    public void testGsonPathCustomType() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter",
                "public class Test {",
                "    @SerializedName(value = \"Json1\")",
                "    public java.util.Currency value1;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_GsonTypeAdapter",
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
                ));

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    @Test
    public void testGsonPathRequiresAnnotation() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter(ignoreNonAnnotatedFields = true)",
                "public class Test {",
                "    public java.lang.Object element1;",
                "}"
        ));

        assertEmptyFile(source);
    }

    @Test
    public void testGsonPathExcludeFields() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_EXCLUDE,
                "@AutoGsonAdapter",
                "public class Test {",
                "    public int element1;",
                "    @ExcludeField",
                "    public int element2;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_GsonTypeAdapter",
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
                        "        case \"element1\":",
                        "            jsonFieldCounter0++;",
                        "            result.element1 = in.nextInt();",
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

    @Test
    public void testGsonPathIgnoreInvalidFields() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter",
                "public class Test {",
                "    private static final String TAG = Test.class.getSimpleName();",
                "    public static final int element1 = 1;",
                "    public final int element2 = 2;",
                "    public static int element3 = 3;",
                "}"
        ));

        assertEmptyFile(source);
    }

    @Test
    public void testGsonPathInvalidType() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter",
                "public class Test {",
                "    @SerializedName(\"element1\")",
                "    public java.lang.Object element1;",
                "}"
        ));

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .failsToCompile()
                .withErrorContaining("Invalid field type: java.lang.Object")
                .in(source).onLine(7);
    }

    @Test
    public void testGsonPathGeneratedLoader() {

        JavaFileObject source1 = JavaFileObjects.forSourceString("test.Test1", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter",
                "public class Test1 {",
                "}"
        ));

        JavaFileObject source2 = JavaFileObjects.forSourceString("test.Test2", Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter",
                "public class Test2 {",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("gsonpath.GeneratedGsonPathLoader",
                Joiner.on('\n').join(
                        "package gsonpath;",
                        "",
                        "import com.google.gson.Gson;",
                        "import com.google.gson.TypeAdapter;",
                        "import com.google.gson.reflect.TypeToken;",
                        "import java.lang.Override;",
                        "",
                        "public final class GeneratedGsonPathLoader implements GsonPathLoader {",
                        "    @Override",
                        "    public TypeAdapter create(Gson gson, TypeToken type) {",
                        "        Class rawType = type.getRawType();",
                        "        if (rawType.equals(com.test.Test1.class)) {",
                        "            return new com.test.Test1_GsonTypeAdapter(gson);",
                        "",
                        "        } else if (rawType.equals(com.test.Test2.class)) {",
                        "            return new com.test.Test2_GsonTypeAdapter(gson);",
                        "        }",
                        "",
                        "        return null;",
                        "    }",
                        "}"
                ));


        ArrayList<JavaFileObject> sources = new ArrayList<>();
        sources.add(source1);
        sources.add(source2);

        assertAbout(javaSources()).that(sources)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(createEmptyResultSource("Test1"), createEmptyResultSource("Test2"), expectedSource);
    }
}
