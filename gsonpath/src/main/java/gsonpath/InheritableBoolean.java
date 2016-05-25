package gsonpath;

/**
* Created due to a requirement to specify a boolean value on an annotation which may potentially
 * be replaced with a default value if the enum has specified that it inherits.
*/
public enum InheritableBoolean {
    TRUE(true, false),
    FALSE(false, false),
    TRUE_OR_INHERIT_DEFAULT_IF_AVAILABLE(true, true),
    FALSE_OR_INHERIT_DEFAULT_IF_AVAILABLE(false, true);

    public final boolean booleanValue;
    public final boolean inheritDefaultIfAvailable;

    InheritableBoolean(boolean booleanValue, boolean inheritDefaultIfAvailable) {
        this.booleanValue = booleanValue;
        this.inheritDefaultIfAvailable = inheritDefaultIfAvailable;
    }
}
