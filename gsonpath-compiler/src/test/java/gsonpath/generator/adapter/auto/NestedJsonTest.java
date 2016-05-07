package gsonpath.generator.adapter.auto;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class NestedJsonTest extends BaseGeneratorTest {
    @Test
    public void testFlatteningUsingFields() {
        assertGeneratedContent(new TestCriteria("adapter/auto/nested_json/field_nesting")
                .addRelativeSource("TestFieldNesting.java")
                .addRelativeGenerated("TestFieldNesting_GsonTypeAdapter.java"));
    }

    @Test
    public void testFlatteningUsingRootField() {
        assertGeneratedContent(new TestCriteria("adapter/auto/nested_json/root_nesting")
                .addRelativeSource("TestRootNesting.java")
                .addRelativeGenerated("TestRootNesting_GsonTypeAdapter.java"));
    }

    @Test
    public void testFlatteningUsingFieldAutoComplete() {
        assertGeneratedContent(new TestCriteria("adapter/auto/nested_json/field_nesting_autocomplete")
                .addRelativeSource("TestFieldNestingAutocomplete.java")
                .addRelativeGenerated("TestFieldNestingAutocomplete_GsonTypeAdapter.java"));
    }
}
