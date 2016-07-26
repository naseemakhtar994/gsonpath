package gsonpath.generator.adapter.auto;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class InterfaceTest extends BaseGeneratorTest {
    @Test
    public void testValidInterface() {
        assertGeneratedContent(new BaseGeneratorTest.TestCriteria("adapter/auto/interface_example/valid")
                .addRelativeSource("TestValidInterface.java")
                .addRelativeGenerated("TestValidInterface_GsonPathModel.java")
                .addRelativeGenerated("TestValidInterface_GsonTypeAdapter.java"));
    }

    @Test
    public void testUsingPrimitives() {
        assertGeneratedContent(new BaseGeneratorTest.TestCriteria("adapter/auto/interface_example/primitive")
                .addRelativeSource("TestUsingPrimitives.java")
                .addRelativeGenerated("TestUsingPrimitives_GsonPathModel.java")
                .addRelativeGenerated("TestUsingPrimitives_GsonTypeAdapter.java"));
    }

    @Test
    public void testUsingInheritance() {
        assertGeneratedContent(new BaseGeneratorTest.TestCriteria("adapter/auto/interface_example/inheritance")
                .addRelativeSource("TestUsingInheritanceBase.java")
                .addRelativeSource("TestUsingInheritance.java")
                .addRelativeGenerated("TestUsingInheritance_GsonPathModel.java")
                .addRelativeGenerated("TestUsingInheritance_GsonTypeAdapter.java"));
    }
}
