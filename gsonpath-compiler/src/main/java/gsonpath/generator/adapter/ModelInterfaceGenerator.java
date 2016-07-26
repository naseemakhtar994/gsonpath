package gsonpath.generator.adapter;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
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

        List<InterfaceFieldInfo> interfaceInfoList = new ArrayList<>();
        for (final Element memberElement : processingEnv.getElementUtils().getAllMembers(element)) {
            if (memberElement.getKind() == ElementKind.METHOD) {

                // Ignore methods from the base Object class
                if (TypeName.get(memberElement.getEnclosingElement().asType()).equals(TypeName.OBJECT)) {
                    continue;
                }

                ExecutableType methodType = (ExecutableType) memberElement.asType();
                TypeMirror returnType = methodType.getReturnType();
                TypeName typeName = TypeName.get(returnType);

                if (typeName == null) {
                    throw new ProcessingException("Interface methods must not return null", memberElement);
                }

                String methodName = memberElement.getSimpleName().toString();

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

                interfaceInfoList.add(new InterfaceFieldInfo(memberElement, typeName, fieldName));
            }
        }

        typeBuilder.addMethod(constructorBuilder.build());

        if (!writeFile(outputClassName.packageName(), typeBuilder)) {
            throw new ProcessingException("Failed to write generated file: " + outputClassName.simpleName());
        }

        return new InterfaceInfo(outputClassName, interfaceInfoList.toArray(new InterfaceFieldInfo[interfaceInfoList.size()]));
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
