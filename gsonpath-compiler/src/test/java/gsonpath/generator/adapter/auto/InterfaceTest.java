package gsonpath.generator.adapter.auto;

import com.google.testing.compile.JavaFileObjects;
import gsonpath.GsonProcessor;
import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class InterfaceTest extends BaseGeneratorTest {
    @Test
    public void testValidInterface() {
        assertGeneratedContent(new BaseGeneratorTest.TestCriteria("adapter/auto/interface_example/valid")
                .addRelativeSource("TestValidInterface.java")
                .addRelativeGenerated("TestValidInterface_GsonPathModel.java")
                .addRelativeGenerated("TestValidInterface_GsonTypeAdapter.java"));
    }

    @Test
    public void testInvalidInterface_returningVoid() {
        JavaFileObject source = JavaFileObjects.forResource("adapter/auto/interface_example/invalid/TestValidInterface_ReturningVoid.java");

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .failsToCompile()
                .withErrorContaining("Gson Path interface methods must have a return type")
                .in(source)
                .onLine(7);
    }

    @Test
    public void testInvalidInterface_withParameters() {
        JavaFileObject source = JavaFileObjects.forResource("adapter/auto/interface_example/invalid/TestValidInterface_WithParameters.java");

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .failsToCompile()
                .withErrorContaining("Gson Path interface methods must not have parameters")
                .in(source)
                .onLine(7);
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
