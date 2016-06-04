package gsonpath.generator;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;

public class FieldInfo {
    public final Element element;

    public final TypeName typeName;
    public final String className;
    public final String fieldName;
    public final String jsonFieldPath;
    public final boolean isRequired;

    public FieldInfo(Element element, String jsonFieldPath, boolean isRequired) {
        this.element = element;
        this.typeName = TypeName.get(element.asType());
        this.className = element.getEnclosingElement().toString();
        this.fieldName = element.getSimpleName().toString();
        this.jsonFieldPath = jsonFieldPath;
        this.isRequired = isRequired;
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return element.getAnnotation(annotationType);
    }
}
