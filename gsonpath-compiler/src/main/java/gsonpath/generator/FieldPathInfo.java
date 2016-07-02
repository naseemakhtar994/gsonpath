package gsonpath.generator;

public class FieldPathInfo {
    public final FieldInfo fieldInfo;
    public final String jsonPath;
    public final boolean isRequired;

    public FieldPathInfo(FieldInfo fieldInfo, String jsonPath, boolean isRequired) {
        this.fieldInfo = fieldInfo;
        this.jsonPath = jsonPath;
        this.isRequired = isRequired;
    }

    public String getSafeVariableName() {
        return "value_" + jsonPath.replaceAll("[^A-Za-z0-9_]", "_");
    }
}
