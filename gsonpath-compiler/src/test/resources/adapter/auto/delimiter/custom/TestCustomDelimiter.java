package adapter.auto.delimiter.custom;

import gsonpath.AutoGsonAdapter;
import com.google.gson.annotations.SerializedName;
import gsonpath.FlattenDelimiter;

@AutoGsonAdapter(flattenDelimiter = @FlattenDelimiter('$'))
public class TestCustomDelimiter {
    @SerializedName("Json1$Nest1")
    public int value1;
}