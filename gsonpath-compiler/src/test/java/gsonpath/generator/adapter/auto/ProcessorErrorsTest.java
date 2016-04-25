package gsonpath.generator.adapter.auto;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import gsonpath.GsonProcessor;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ProcessorErrorsTest extends BaseAutoTest {

    @Test
    public void testInvalidType() {
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
                .in(source)
                .onLine(7);
    }
}
