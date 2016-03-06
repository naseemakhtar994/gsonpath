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
                        "import static gsonpath.GsonPathUtil.getStringSafely;",
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

    @Test
    public void testGsonPathWithPrimitives() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                "package com.test;",
                "import gsonpath.GsonPathClass;",
                "import gsonpath.GsonPathElement;",
                "@GsonPathClass",
                "public class Test {",
                "  @GsonPathElement(\"Json1\")",
                "  public String value1;",
                "  public boolean value2;",
                "  public int value3;",
                "  public double value4;",
                "  public long value5;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_Adapter",
                Joiner.on('\n').join(
                        "package com.test;",
                        "",
                        "import static gsonpath.GsonPathUtil.getStringSafely;",
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
                        "    in.beginObject();",
                        "    while (in.hasNext()) {",
                        "      switch(in.nextName()) {",
                        "        case \"Json1\":",
                        "          result.value1 = getStringSafely(in);",
                        "          break;",
                        "        case \"value2\":",
                        "          result.value2 = in.nextBoolean();",
                        "          break;",
                        "        case \"value3\":",
                        "          result.value3 = in.nextInt();",
                        "          break;",
                        "        case \"value4\":",
                        "          result.value4 = in.nextDouble();",
                        "          break;",
                        "        case \"value5\":",
                        "          result.value5 = in.nextLong();",
                        "          break;",
                        "        default:",
                        "          in.skipValue();",
                        "          break;",
                        "      }",
                        "    }",
                        "    in.endObject();",
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

    @Test
    public void testGsonPathWithNestedPrimitives() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                "package com.test;",
                "import gsonpath.GsonPathClass;",
                "import gsonpath.GsonPathElement;",
                "@GsonPathClass",
                "public class Test {",
                "  @GsonPathElement(\"Json1\")",
                "  public String value1;",
                "  @GsonPathElement(\"Json2.Nest1\")",
                "  public String value2;",
                "  @GsonPathElement(\"Json2.Nest2\")",
                "  public String value3;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test.Test_Adapter",
                Joiner.on('\n').join(
                        "package com.test;",
                        "",
                        "import static gsonpath.GsonPathUtil.getStringSafely;",
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
                        "    in.beginObject();",
                        "    while (in.hasNext()) {",
                        "      switch(in.nextName()) {",
                        "        case \"Json1\":",
                        "          result.value1 = getStringSafely(in);",
                        "          break;",
                        "        case \"Json2\":",
                        "          in.beginObject();",
                        "          while (in.hasNext()) {",
                        "            switch(in.nextName()) {",
                        "              case \"Nest1\":",
                        "                result.value2 = getStringSafely(in);",
                        "                break;",
                        "              case \"Nest2\":",
                        "                result.value3 = getStringSafely(in);",
                        "                break;",
                        "              default:",
                        "                in.skipValue();",
                        "                break;",
                        "            }",
                        "          }",
                        "          in.endObject();",
                        "          break;",
                        "        default:",
                        "          in.skipValue();",
                        "          break;",
                        "      }",
                        "    }",
                        "    in.endObject();",
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

    @Test
    public void testGsonPathInvalidType() {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                "package com.test;",
                "import gsonpath.GsonPathClass;",
                "import gsonpath.GsonPathElement;",
                "@GsonPathClass",
                "public class Test {",
                "  @GsonPathElement(\"element1\")",
                "  public java.lang.Object element1;",
                "}"
        ));

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .failsToCompile()
                .withErrorContaining("Invalid field type. Expecting: [String, boolean, int, long, double]")
                .in(source).onLine(7);
    }
}
