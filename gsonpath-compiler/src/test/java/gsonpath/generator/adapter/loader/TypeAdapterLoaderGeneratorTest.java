package gsonpath.generator.adapter.loader;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class TypeAdapterLoaderGeneratorTest extends BaseGeneratorTest {
    @Test
    public void testGeneratedLoader() {
        assertGeneratedContent(new BaseGeneratorTest.TestCriteria("adapter/loader")
                .addRelativeSource("TestLoaderSource1.java")
                .addRelativeSource("TestLoaderSource2.java")
                .addRelativeGenerated("GeneratedTypeAdapterLoader.java"));
    }
}
