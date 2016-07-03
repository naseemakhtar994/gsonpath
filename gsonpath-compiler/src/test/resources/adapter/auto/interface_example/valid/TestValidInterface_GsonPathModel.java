package adapter.auto.interface_example.valid;

import java.lang.Integer;
import java.lang.Override;

public final class TestValidInterface_GsonPathModel implements TestValidInterface {
    private final Integer value1;
    private final Integer value2;

    public TestValidInterface_GsonPathModel(Integer value1, Integer value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public Integer getValue1() {
        return value1;
    }

    @Override
    public Integer getValue2() {
        return value2;
    }
}