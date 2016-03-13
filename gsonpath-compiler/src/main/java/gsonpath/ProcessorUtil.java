package gsonpath;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by Lachlan on 7/03/2016.
 */
public class ProcessorUtil {

    public static List<? extends Element> getAllFieldElements(TypeElement element, Elements elementsUtil, Types typesUtil) {
        List<Element> elements = new ArrayList<>();

        // We only care about field elements.
        for (Element childElement : elementsUtil.getAllMembers(element)) {
            if (childElement.getKind() == ElementKind.FIELD) {
                elements.add(childElement);
            }
        }

        TypeMirror superMirror;
        TypeElement typeElement = element;

        while ((superMirror = typeElement.getSuperclass()) != null) {
            typeElement = (TypeElement) typesUtil.asElement(superMirror);
            if (typeElement == null) {
                break;
            }
            List<? extends Element> superElements = elementsUtil.getAllMembers(typeElement);
            for (Element superElement : superElements) {

                // We only care about field elements.
                if (superElement.getKind() != ElementKind.FIELD) {
                    continue;
                }

                if (!elements.contains(superElement)) {
                    elements.add(superElement);
                }
            }
        }
        return elements;
    }

    public static String getElementType(Element field) {
        return field.asType().toString();
    }

    public static String getElementPackage(Element element) {
        Element currentElement = element;
        while (currentElement instanceof TypeElement) {
            currentElement = currentElement.getEnclosingElement();
        }
        return currentElement.toString();
    }

    public static String getElementClassName(Element element) {
        String packageName = getElementPackage(element);
        return element.toString().substring(packageName.length() + 1);
    }

    public static ClassName getElementJavaPoetClassName(Element element) {
        String packageName = getElementPackage(element);
        return ClassName.get(packageName, getElementClassName(element));
    }

}
