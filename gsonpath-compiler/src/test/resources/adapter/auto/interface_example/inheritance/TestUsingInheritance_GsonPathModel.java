package adapter.auto.interface_example.inheritance;

import java.lang.Integer;
import java.lang.Override;

public final class TestUsingInheritance_GsonPathModel implements TestUsingInheritance {
    private final Integer value3;
    private final Integer value1;
    private final Integer value2;

    public TestUsingInheritance_GsonPathModel(Integer value3, Integer value1, Integer value2) {
        this.value3 = value3;
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public Integer getValue3() {
        return value3;
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