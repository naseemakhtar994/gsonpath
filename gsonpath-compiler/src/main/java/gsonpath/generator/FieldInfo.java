package gsonpath.generator;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;

public interface FieldInfo {
    TypeName getTypeName();

    String getParentClassName();

    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    String getFieldName();

    String[] getAnnotationNames();

    Element getElement();
}
