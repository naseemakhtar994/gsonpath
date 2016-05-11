package gsonpath.generator;

import javax.lang.model.element.Element;

public class FieldInfo {
    public final Element element;
    public final String jsonPath;
    public final boolean isRequired;

    public FieldInfo(Element element, String jsonPath, boolean isRequired) {
        this.element = element;
        this.jsonPath = jsonPath;
        this.isRequired = isRequired;
    }
}
