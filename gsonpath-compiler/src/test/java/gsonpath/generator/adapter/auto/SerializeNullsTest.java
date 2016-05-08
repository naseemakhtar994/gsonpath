package gsonpath.generator.adapter.auto;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class SerializeNullsTest extends BaseGeneratorTest {

    @Test
    public void testSerializeNulls() {
        assertGeneratedContent(new TestCriteria("adapter/auto/serialize_nulls")
                .addRelativeSource("TestSerializeNulls.java")
                .addRelativeGenerated("TestSerializeNulls_GsonTypeAdapter.java"));
    }

}
