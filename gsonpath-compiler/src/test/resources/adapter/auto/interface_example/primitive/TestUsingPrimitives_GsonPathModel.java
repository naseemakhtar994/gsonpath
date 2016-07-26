package adapter.auto.interface_example.primitive;

import java.lang.Double;
import java.lang.Object;
import java.lang.Override;

public final class TestUsingPrimitives_GsonPathModel implements TestUsingPrimitives {
    private final int intExample;
    private final long longExample;
    private final double doubleExample;
    private final boolean booleanExample;
    private final int[] intArrayExample;
    private final long[] longArrayExample;
    private final double[] doubleArrayExample;
    private final boolean[] booleanArrayExample;

    public TestUsingPrimitives_GsonPathModel(int intExample, long longExample, double doubleExample, boolean booleanExample, int[] intArrayExample, long[] longArrayExample, double[] doubleArrayExample, boolean[] booleanArrayExample) {
        this.intExample = intExample;
        this.longExample = longExample;
        this.doubleExample = doubleExample;
        this.booleanExample = booleanExample;
        this.intArrayExample = intArrayExample;
        this.longArrayExample = longArrayExample;
        this.doubleArrayExample = doubleArrayExample;
        this.booleanArrayExample = booleanArrayExample;
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

    @Override
    public int[] getIntArrayExample() {
        return intArrayExample;
    }

    @Override
    public long[] getLongArrayExample() {
        return longArrayExample;
    }

    @Override
    public double[] getDoubleArrayExample() {
        return doubleArrayExample;
    }

    @Override
    public boolean[] getBooleanArrayExample() {
        return booleanArrayExample;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TestUsingPrimitives_GsonPathModel that = (TestUsingPrimitives_GsonPathModel) o;

        if (intExample != that.intExample) return false;
        if (longExample != that.longExample) return false;
        if (doubleExample != that.doubleExample) return false;
        if (booleanExample != that.booleanExample) return false;
        if (java.util.Arrays.equals(intArrayExample, that.intArrayExample));
        if (java.util.Arrays.equals(longArrayExample, that.longArrayExample));
        if (java.util.Arrays.equals(doubleArrayExample, that.doubleArrayExample));
        if (java.util.Arrays.equals(booleanArrayExample, that.booleanArrayExample));

        return true;
    }

    @Override
    public int hashCode() {
        long temp;
        int result = intExample;
        result = 31 * result + ((int) (longExample ^ (longExample >>> 32)));
        temp = Double.doubleToLongBits(doubleExample);
        result = 31 * result + ((int) (temp ^ (temp >>> 32)));
        result = 31 * result + ((booleanExample ? 1 : 0));
        result = 31 * result + (java.util.Arrays.hashCode(intArrayExample));
        result = 31 * result + (java.util.Arrays.hashCode(longArrayExample));
        result = 31 * result + (java.util.Arrays.hashCode(doubleArrayExample));
        result = 31 * result + (java.util.Arrays.hashCode(booleanArrayExample));
        return result;
    }
}