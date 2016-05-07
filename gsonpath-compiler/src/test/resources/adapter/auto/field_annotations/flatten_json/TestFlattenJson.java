package adapter.auto.field_annotations.flatten_json;

import gsonpath.AutoGsonAdapter;
import gsonpath.FlattenJson;

@AutoGsonAdapter
public class TestFlattenJson {
    @FlattenJson
    public String value1;
}