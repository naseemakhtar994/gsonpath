package gsonpath.generator.adapter;

import com.squareup.javapoet.*;
import gsonpath.ProcessingException;
import gsonpath.generator.BaseAdapterGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

class ModelInterfaceGenerator extends BaseAdapterGenerator {

    ModelInterfaceGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected String getClassNameSuffix() {
        return "GsonPathModel";
    }

    InterfaceInfo handle(TypeElement element) throws ProcessingException {
        ClassName modelClassName = ClassName.get(element);

        ClassName outputClassName = ClassName.get(modelClassName.packageName(), generateClassName(modelClassName));
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(outputClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(modelClassName);

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        List<Element> methodElements = getMethodElements(element);

        // Equals method
        CodeBlock.Builder toStringCodeBlock = CodeBlock.builder();
        toStringCodeBlock.addStatement("if (this == o) return true");
        toStringCodeBlock.addStatement("if (o == null || getClass() != o.getClass()) return false");
        toStringCodeBlock.add("\n");
        toStringCodeBlock.addStatement("$T that = ($T) o", outputClassName, outputClassName);
        toStringCodeBlock.add("\n");

        // Hash code method
        CodeBlock.Builder hasCodeCodeBlock = CodeBlock.builder();

        // An optimisation for hash codes which prevents us creating too many temp long variables.
        boolean hasDoubleField = false;

        for (Element enclosedElement : methodElements) {
            ExecutableType methodType = (ExecutableType) enclosedElement.asType();
            TypeMirror returnType = methodType.getReturnType();
            if (TypeName.get(returnType).equals(TypeName.DOUBLE)) {
                hasDoubleField = true;
                break;
            }
        }

        if (hasDoubleField) {
            hasCodeCodeBlock.addStatement("long temp");
        }

        List<InterfaceFieldInfo> interfaceInfoList = new ArrayList<>();
        for (int elementIndex = 0; elementIndex < methodElements.size(); elementIndex++) {
            Element enclosedElement = methodElements.get(elementIndex);

            ExecutableType methodType = (ExecutableType) enclosedElement.asType();
            TypeMirror returnType = methodType.getReturnType();
            TypeName typeName = TypeName.get(returnType);

            if (typeName == null || typeName.equals(TypeName.VOID)) {
                throw new ProcessingException("Gson Path interface methods must have a return type", enclosedElement);
            }

            if (methodType.getParameterTypes().size() > 0) {
                throw new ProcessingException("Gson Path interface methods must not have parameters", enclosedElement);
            }
            
            String methodName = enclosedElement.getSimpleName().toString();

            // Transform the method name into the field name by removing the first camel-cased portion.
            String fieldName = methodName;

            for (int i = 0; i < fieldName.length() - 1; i++) {
                char character = fieldName.charAt(i);
                if (Character.isUpperCase(character)) {
                    fieldName = Character.toLowerCase(character) + fieldName.substring(i + 1);
                    break;
                }
            }

            typeBuilder.addField(typeName, fieldName, Modifier.PRIVATE, Modifier.FINAL);

            MethodSpec.Builder accessorMethod = MethodSpec.methodBuilder(methodName)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(typeName);

            accessorMethod.addCode("return $L;\n", fieldName);

            typeBuilder.addMethod(accessorMethod.build());

            // Add the parameter to the constructor
            constructorBuilder.addParameter(typeName, fieldName);
            constructorBuilder.addStatement("this.$L = $L", fieldName, fieldName);

            interfaceInfoList.add(new InterfaceFieldInfo(enclosedElement, typeName, fieldName));

            // Add to the equals method
            if (typeName.isPrimitive()) {
                toStringCodeBlock.addStatement("if ($L != that.$L) return false", fieldName, fieldName);
            } else {
                if (typeName instanceof ArrayTypeName) {
                    toStringCodeBlock.addStatement("if (java.util.Arrays.equals($L, that.$L))", fieldName, fieldName);

                } else {
                    toStringCodeBlock.addStatement("if (($L == null || !$L.equals(that.$L))) return false", fieldName, fieldName, fieldName);
                }
            }

            // Add to the hash code method
            String hashCodeLine;

            if (typeName.isPrimitive()) {
                // The allowed primitive types are: int, long, double, boolean
                if (typeName.equals(TypeName.INT)) {
                    hashCodeLine = fieldName;

                } else if (typeName.equals(TypeName.LONG)) {
                    hashCodeLine = String.format("(int) (%s ^ (%s >>> 32))", fieldName, fieldName);

                } else if (typeName.equals(TypeName.DOUBLE)) {
                    hasCodeCodeBlock.addStatement("temp = $T.doubleToLongBits($L)", Double.class, fieldName);
                    hashCodeLine = "(int) (temp ^ (temp >>> 32))";

                } else {
                    // Last possible outcome in a boolean.
                    hashCodeLine = String.format("(%s ? 1 : 0)", fieldName);
                }
            } else {
                if (typeName instanceof ArrayTypeName) {
                    hashCodeLine = String.format("java.util.Arrays.hashCode(%s)", fieldName);

                } else {
                    hashCodeLine = String.format("%s != null ? %s.hashCode() : 0", fieldName, fieldName);
                }
            }

            if (elementIndex == 0) {
                hasCodeCodeBlock.addStatement("int result = $L", hashCodeLine);
            } else {
                hasCodeCodeBlock.addStatement("result = 31 * result + ($L)", hashCodeLine);
            }
        }

        typeBuilder.addMethod(constructorBuilder.build());

        // Add the equals method
        MethodSpec.Builder toStringBuilder = MethodSpec.methodBuilder("equals")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(TypeName.OBJECT, "o");

        toStringCodeBlock.add("\n");
        toStringCodeBlock.addStatement("return true");
        toStringBuilder.addCode(toStringCodeBlock.build());
        typeBuilder.addMethod(toStringBuilder.build());

        // Add the hashCode method
        MethodSpec.Builder hashCodeBuilder = MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT);

        // If we have no elements, 'result' won't be initialised!
        if (methodElements.size() > 0) {
            hasCodeCodeBlock.addStatement("return result");
        } else {
            hasCodeCodeBlock.addStatement("return 0");
        }

        hashCodeBuilder.addCode(hasCodeCodeBlock.build());
        typeBuilder.addMethod(hashCodeBuilder.build());

        if (!writeFile(outputClassName.packageName(), typeBuilder)) {
            throw new ProcessingException("Failed to write generated file: " + outputClassName.simpleName());
        }

        return new InterfaceInfo(outputClassName, interfaceInfoList.toArray(new InterfaceFieldInfo[interfaceInfoList.size()]));
    }

    private List<Element> getMethodElements(TypeElement element) {
        List<Element> methodElements = new ArrayList<>();
        for (Element memberElement : processingEnv.getElementUtils().getAllMembers(element)) {
            if (memberElement.getKind() == ElementKind.METHOD) {

                // Ignore methods from the base Object class
                if (TypeName.get(memberElement.getEnclosingElement().asType()).equals(TypeName.OBJECT)) {
                    continue;
                }
                
                methodElements.add(memberElement);
            }
        }
        return methodElements;
    }

    static class InterfaceInfo {
        ClassName parentClassName;
        InterfaceFieldInfo[] fieldInfo;

        InterfaceInfo(ClassName parentClassName, InterfaceFieldInfo[] fieldInfo) {
            this.parentClassName = parentClassName;
            this.fieldInfo = fieldInfo;
        }
    }

    static class InterfaceFieldInfo {
        Element methodElement;
        TypeName typeName;
        String fieldName;

        InterfaceFieldInfo(Element methodElement, TypeName typeName, String fieldName) {
            this.methodElement = methodElement;
            this.typeName = typeName;
            this.fieldName = fieldName;
        }
    }
}
