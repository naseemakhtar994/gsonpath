package gsonpath.generator.streamer;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class GsonArrayStreamerGeneratorTest extends BaseGeneratorTest {
    @Test
    public void testNoRoot() {
        assertGeneratedContent(new TestCriteria("streamer/no_root")
                .addRelativeSource("TestNoRoot.java")
                .addRelativeGenerated("TestNoRoot_GsonArrayStreamer.java"));
    }

    @Test
    public void testRootReadingObjectEntirely() {
        assertGeneratedContent(new TestCriteria("streamer/read_object_entirely")
                .addRelativeSource("TestReadObjectEntirely.java")
                .addRelativeGenerated("TestReadObjectEntirely_GsonArrayStreamer.java"));
    }

    @Test
    public void testRootPartialStreamReading() {
        assertGeneratedContent(new TestCriteria("streamer/partial_stream_reading")
                .addRelativeSource("TestStreamReading.java")
                .addRelativeGenerated("TestStreamReading_GsonArrayStreamer.java"));
    }
}
