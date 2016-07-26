package adapter.auto.interface_example.invalid;

import gsonpath.AutoGsonAdapter;

@AutoGsonAdapter
public interface TestValidInterface_WithParameters {
    int getInvalid(int invalidParameter);
}