package gsonpath;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;
import java.util.ArrayList;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

/**
 * Created by Lachlan on 2/03/2016.
 */
public class TypeAdapterLoaderGeneratorTest {

    private static final String STANDARD_PACKAGE_NAME = "package com.test;";

    private static final String IMPORT_GSON_PATH_CLASS = "import gsonpath.AutoGsonAdapter;";
    private static final String IMPORT_GSON_PATH_ELEMENT = "import com.google.gson.annotations.SerializedName;";

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

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("gsonpath.GeneratedTypeAdapterLoader",
                Joiner.on('\n').join(
                        "package gsonpath;",
                        "",
                        "import com.google.gson.Gson;",
                        "import com.google.gson.TypeAdapter;",
                        "import com.google.gson.reflect.TypeToken;",
                        "import gsonpath.internal.TypeAdapterLoader;",
                        "import java.lang.Override;",
                        "",
                        "public final class GeneratedTypeAdapterLoader implements TypeAdapterLoader {",
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
                .generatesSources(expectedSource);
    }
}
