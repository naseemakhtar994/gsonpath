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
public class GsonProcessorTest {

    /**
     * Tests the output generated when only a {@link GsonPathClass} annotation is used.
     */
    @Test
    public void testGsonPathClassOnly() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                "package com.test;",
                "import gsonpath.GsonPathClass;",
                "@GsonPathClass",
                "public class Test {",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_Adapter",
                Joiner.on('\n').join(
                        "package com.test;",
                        "",
                        "import com.google.gson.TypeAdapter;",
                        "import com.google.gson.stream.JsonReader;",
                        "import com.google.gson.stream.JsonWriter;",
                        "import java.io.IOException;",
                        "import java.lang.Override;",
                        "",
                        "public final class Test_Adapter extends TypeAdapter<Test> {",
                        "  @Override",
                        "  public Test read(JsonReader in) throws IOException {",
                        "    Test result = new Test();",
                        "    return result;",
                        "  }",
                        "",
                        "  @Override",
                        "  public void write(JsonWriter out, Test value) throws IOException {",
                        "    // GsonPath does not support writing at this stage.",
                        "  }",
                        "}"
                ));

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }
}
