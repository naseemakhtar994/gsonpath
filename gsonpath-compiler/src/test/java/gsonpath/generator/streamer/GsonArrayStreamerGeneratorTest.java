package gsonpath.generator.streamer;

import com.google.common.base.Joiner;
import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class GsonArrayStreamerGeneratorTest extends BaseGeneratorTest {

    private static final String IMPORT_GSON_PATH_CLASS = "import gsonpath.AutoGsonArrayStreamer;";
    private static final String IMPORT_GSON_PATH_ADAPTER = "import gsonpath.GsonArrayStreamer;";

    @Test
    public void testArrayAdapterNoRoot() {

        String source = Joiner.on('\n').join(
                STANDARD_PACKAGE_NAME,
                IMPORT_GSON_PATH_CLASS,
                IMPORT_GSON_PATH_ADAPTER,
                "@AutoGsonArrayStreamer",
                "public interface Test extends GsonArrayStreamer<java.lang.String> {",
                "}"
        );

        String expectedSource = Joiner.on('\n').join(
                "package com.test;",
                "",
                "import static gsonpath.GsonUtil.*;",
                "",
                "import com.google.gson.Gson;",
                "import com.google.gson.JsonSyntaxException;",
                "import com.google.gson.stream.JsonReader;",
                "import gsonpath.AbstractGsonArrayStreamer;",
                "import gsonpath.GsonArrayStreamer;",
                "import java.io.IOException;",
                "import java.lang.Override;",
                "import java.lang.String;",
                "import java.util.List;",
                "",
                "public final class Test_GsonArrayStreamer extends AbstractGsonArrayStreamer<String> implements Test {",
                "    @Override",
                "    public String[] getArray(Gson gson, JsonReader in) throws JsonSyntaxException {",
                "        return gson.fromJson(in, String[].class);",
                "    }",
                "",
                "    @Override",
                "    public List<String> getList(Gson gson, JsonReader in) throws JsonSyntaxException {",
                "        return gson.fromJson(in, new com.google.gson.reflect.TypeToken<List<String>>() {}.getType());",
                "    }",
                "",
                "    @Override",
                "    public void streamArraySegmented(Gson gson, JsonReader in, int streamSize, GsonArrayStreamer.StreamCallback<String[]> callback) throws JsonSyntaxException {",
                "        String[] results = new String[streamSize];",
                "        StreamCallback.StreamHandler callbackResponse = new StreamCallback.StreamHandler();",
                "        int resultIndex = -1;",
                "",
                "        try {",
                "            in.beginArray();",
                "            while (in.hasNext()) {",
                "                results[++resultIndex] = gson.fromJson(in, String.class);",
                "",
                "                // Once we reach the requested stream size, we should return the results.",
                "                if (resultIndex == streamSize - 1) {",
                "                    callback.onValueParsed(results, streamSize, callbackResponse);",
                "",
                "                    if (callbackResponse.isStreamStopped()) {",
                "                        // Since we stop the stream mid-way, we cannot call end-array safely.",
                "                        return;",
                "                    }",
                "",
                "                    resultIndex = -1;",
                "                }",
                "            }",
                "            in.endArray();",
                "        } catch (IOException e) {",
                "            throw new JsonSyntaxException(e);",
                "        }",
                "",
                "        // We have left over results to send back.",
                "        if (resultIndex >= 0) {",
                "            // To avoid creating a new array, we will simply remove the invalid results at the end.",
                "            for (int i = resultIndex + 1; i < streamSize; i++) {",
                "                results[i] = null;",
                "            }",
                "",
                "            callback.onValueParsed(results, resultIndex + 1, callbackResponse);",
                "        }",
                "    }",
                "}"
        );

        assertGeneratedContent(source, expectedSource);
    }

}
