package adapter.auto.processor_errors;

import gsonpath.AutoGsonAdapter;

@AutoGsonAdapter
public class TestInvalidFieldTypeError {
    @SerializedName("element1")
    public java.lang.Object element1;
}