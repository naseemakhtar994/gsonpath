package gsonpath.generator.adapter.auto;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class DefaultsTest extends BaseGeneratorTest {
    @Test
    public void testUseInheritance() {
        assertGeneratedContent(new BaseGeneratorTest.TestCriteria("adapter/auto/defaults")
                .addRelativeSource("TestDefaultsConfig.java")
                .addRelativeSource("TestDefaultsUseInheritanceModel.java")
                .addRelativeGenerated("TestDefaultsUseInheritanceModel_GsonTypeAdapter.java"));
    }

    @Test
    public void testOverrideInheritance() {
        assertGeneratedContent(new BaseGeneratorTest.TestCriteria("adapter/auto/defaults")
                .addRelativeSource("TestDefaultsConfig.java")
                .addRelativeSource("TestDefaultsOverrideInheritanceModel.java")
                .addRelativeGenerated("TestDefaultsOverrideInheritanceModel_GsonTypeAdapter.java"));
    }
}
