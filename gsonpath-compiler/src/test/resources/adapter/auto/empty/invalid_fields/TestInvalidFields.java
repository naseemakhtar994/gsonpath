package adapter.auto.empty.invalid_fields;

import gsonpath.AutoGsonAdapter;
import gsonpath.InheritableBoolean;

@AutoGsonAdapter(ignoreNonAnnotatedFields = InheritableBoolean.TRUE)
public class TestInvalidFields {
    public java.lang.String element1;
}