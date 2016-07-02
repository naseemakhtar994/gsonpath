package adapter.auto.interface_example.primitive;

import java.lang.Override;

public final class TestUsingPrimitives_GsonPathModel implements TestUsingPrimitives {
    private final int intExample;
    private final long longExample;
    private final double doubleExample;
    private final boolean booleanExample;

    public TestUsingPrimitives_GsonPathModel(int intExample, long longExample, double doubleExample, boolean booleanExample) {
        this.intExample = intExample;
        this.longExample = longExample;
        this.doubleExample = doubleExample;
        this.booleanExample = booleanExample;
    }

    @Override
    public int getIntExample() {
        return intExample;
    }

    @Override
    public long getLongExample() {
        return longExample;
    }

    @Override
    public double getDoubleExample() {
        return doubleExample;
    }

    @Override
    public boolean getBooleanExample() {
        return booleanExample;
    }
}