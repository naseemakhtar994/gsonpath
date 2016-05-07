package gsonpath.generator.adapter.auto;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

/**
 * These tests are used whenever the input class file generates a blank type adapter.
 * <p/>
 * This can be due to the class being empty, or the content within the class isn't
 * applicable for a type adapter.
 */
public class EmptyAdapterTest extends BaseGeneratorTest {

    /**
     * Tests the output generated when only a {@link gsonpath.AutoGsonAdapter} annotation is used.
     */
    @Test
    public void testAutoGsonAdapterOnly() {
        assertGeneratedContent(new TestCriteria("adapter/auto/empty/annotation_only")
                .addRelativeSource("TestAnnotationOnly.java")
                .addRelativeGenerated("TestAnnotationOnly_GsonTypeAdapter.java"));
    }

    @Test
    public void testRequiresAnnotation() {
        assertGeneratedContent(new TestCriteria("adapter/auto/empty/ignored_fields")
                .addRelativeSource("TestIgnoredFields.java")
                .addRelativeGenerated("TestIgnoredFields_GsonTypeAdapter.java"));
    }

    @Test
    public void testIgnoreInvalidFields() {
        assertGeneratedContent(new TestCriteria("adapter/auto/empty/invalid_fields")
                .addRelativeSource("TestInvalidFields.java")
                .addRelativeGenerated("TestInvalidFields_GsonTypeAdapter.java"));
    }
}
