package adapter.auto.interface_example.valid;

import java.lang.Integer;
import java.lang.Object;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestValidInterface_GsonPathModel that = (TestValidInterface_GsonPathModel) o;

        if ((value1 == null || !value1.equals(that.value1))) return false;
        if ((value2 == null || !value2.equals(that.value2))) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value1 != null ? value1.hashCode() : 0;
        result = 31 * result + (value2 != null ? value2.hashCode() : 0);
        return result;
    }
}