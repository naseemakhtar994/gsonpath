package gsonpath.generator.adapter.auto;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class FieldNamingPolicyTest extends BaseGeneratorTest {
    @Test
    public void testIdentity() {
        assertGeneratedContent(new TestCriteria("adapter/auto/naming_policy/identity")
                .addRelativeSource("TestNamePolicyIdentity.java")
                .addRelativeGenerated("TestNamePolicyIdentity_GsonTypeAdapter.java"));
    }

    @Test
    public void testLowerCaseWithDashes() {
        assertGeneratedContent(new TestCriteria("adapter/auto/naming_policy/lowercase_dashes")
                .addRelativeSource("TestNamePolicyLowerCaseDashes.java")
                .addRelativeGenerated("TestNamePolicyLowerCaseDashes_GsonTypeAdapter.java"));
    }

    @Test
    public void testLowerCaseWithUnderscores() {
        assertGeneratedContent(new TestCriteria("adapter/auto/naming_policy/lowercase_underscores")
                .addRelativeSource("TestNamePolicyLowerCaseUnderscores.java")
                .addRelativeGenerated("TestNamePolicyLowerCaseUnderscores_GsonTypeAdapter.java"));
    }

    @Test
    public void testUpperCamelCase() {
        assertGeneratedContent(new TestCriteria("adapter/auto/naming_policy/upper_camel_case")
                .addRelativeSource("TestNamePolicyUpperCamelCase.java")
                .addRelativeGenerated("TestNamePolicyUpperCamelCase_GsonTypeAdapter.java"));
    }

    @Test
    public void testUpperCamelCaseWithSpaces() {
        assertGeneratedContent(new TestCriteria("adapter/auto/naming_policy/upper_camel_case_spaces")
                .addRelativeSource("TestNamePolicyUpperCamelCaseSpaces.java")
                .addRelativeGenerated("TestNamePolicyUpperCamelCaseSpaces_GsonTypeAdapter.java"));
    }
}
