package gsonpath.generator.adapter.auto;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class FieldPolicyTest extends BaseGeneratorTest {
    @Test
    public void testFailAllMandatory() {
        assertGeneratedContent(new TestCriteria("adapter/auto/field_policy/fail_mandatory")
                .addRelativeSource("TestFailMandatory.java")
                .addRelativeGenerated("TestFailMandatory_GsonTypeAdapter.java"));
    }

    @Test
    public void testFailAllExceptOptional() {
        assertGeneratedContent(new TestCriteria("adapter/auto/field_policy/fail_all_except_optional")
                .addRelativeSource("TestFailAllExceptOptional.java")
                .addRelativeGenerated("TestFailAllExceptOptional_GsonTypeAdapter.java"));
    }

    @Test
    public void testNeverFail() {
        assertGeneratedContent(new TestCriteria("adapter/auto/field_policy/never_fail")
                .addRelativeSource("TestNeverFail.java")
                .addRelativeGenerated("TestNeverFail_GsonTypeAdapter.java"));
    }
}