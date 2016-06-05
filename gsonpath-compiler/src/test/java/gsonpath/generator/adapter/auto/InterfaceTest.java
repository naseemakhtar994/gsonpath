package gsonpath.generator.adapter.auto;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class InterfaceTest extends BaseGeneratorTest {
    @Test
    public void testInterface() {
        assertGeneratedContent(new BaseGeneratorTest.TestCriteria("adapter/auto/interface_example")
                .addRelativeSource("TestInterface.java")
                .addRelativeGenerated("TestInterface_GsonPathModel.java")
                .addRelativeGenerated("TestInterface_GsonTypeAdapter.java"));
    }
}
