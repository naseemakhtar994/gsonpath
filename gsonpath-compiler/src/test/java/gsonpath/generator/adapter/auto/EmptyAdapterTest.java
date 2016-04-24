package gsonpath.generator.adapter.auto;

import com.google.common.base.Joiner;
import org.junit.Test;

/**
 * These tests are used whenever the input class file generates a blank type adapter.
 * <p/>
 * This can be due to the class being empty, or the content within the class isn't
 * applicable for a type adapter.
 */
public class EmptyAdapterTest extends BaseAutoTest {

    void assertEmptyFile(String source) {
        assertGeneratedContent(source, createEmptyResultSource("Test"));
    }

    /**
     * Tests the output generated when only a {@link gsonpath.AutoGsonAdapter} annotation is used.
     */
    @Test
    public void testAutoGsonAdapterOnly() {
        assertEmptyFile(Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                "@AutoGsonAdapter",
                "public class Test {",
                "}"
        ));
    }

    @Test
    public void testRequiresAnnotation() {
        assertEmptyFile(Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ELEMENT,
                "@AutoGsonAdapter(ignoreNonAnnotatedFields = true)",
                "public class Test {",
                "    public java.lang.Object element1;",
                "}"
        ));
    }

    @Test
    public void testIgnoreInvalidFields() {
        assertEmptyFile(Joiner.on('\n').join(
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
    }
}
