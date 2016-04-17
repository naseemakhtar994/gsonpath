package gsonpath.generator;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.*;
import gsonpath.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lachlan on 12/03/2016.
 */
public class AutoGsonAdapterGenerator extends BaseAdapterGenerator {

    public AutoGsonAdapterGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public HandleResult handle(TypeElement element) throws ProcessingException {
        String elementPackagePath = ProcessorUtil.getElementPackage(element);
        ClassName elementClassName = ProcessorUtil.getElementJavaPoetClassName(element);
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(GSON_PACKAGE, "TypeAdapter"), elementClassName);

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson")
                .addStatement("this.$N = $N", "mGson", "gson")
                .build();

        String adapterClassName = element.getSimpleName() + getClassNameSuffix();
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(adapterClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(parameterizedTypeName)
                .addField(Gson.class, "mGson", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(constructor);

        //
        //@Override
        //public ImageSizes read(JsonReader in) throws IOException {
        //
        MethodSpec.Builder readMethod = MethodSpec.methodBuilder("read")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(elementClassName)
                .addParameter(JsonReader.class, "in")
                .addException(IOException.class);

        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.addStatement("$T result = new $T()", elementClassName, elementClassName);

        AutoGsonAdapter autoGsonAnnotation = element.getAnnotation(AutoGsonAdapter.class);
        boolean fieldsRequireAnnotation = autoGsonAnnotation.ignoreNonAnnotatedFields();

        List<Element> fieldElements = new ArrayList<>();
        for (Element child : ProcessorUtil.getAllFieldElements(element, processingEnv.getElementUtils(), processingEnv.getTypeUtils())) {

            if (child.getModifiers().contains(Modifier.FINAL) ||
                    child.getModifiers().contains(Modifier.STATIC) ||
                    child.getModifiers().contains(Modifier.TRANSIENT)) {

                continue;
            }

            if (fieldsRequireAnnotation && (child.getAnnotation(SerializedName.class) == null)) {
                continue;
            }

            // Ignore any excluded fields
            if (child.getAnnotation(ExcludeField.class) != null) {
                continue;
            }

            fieldElements.add(child);
        }

        // Obtain the correct mapping structure beforehand.
        Map<String, Object> rootElements = new LinkedHashMap<>();
        Map<String, Object> topLevelFieldMap = rootElements;

        // The root element annotation prevents repetition in the SerializedName annotation.
        String rootField = autoGsonAnnotation.rootField();
        if (rootField.length() > 0) {
            String[] split = rootField.split("\\.");

            if (split.length > 0) {
                for (String field : split) {
                    Map<String, Object> mapWithRoot = new LinkedHashMap<>();
                    topLevelFieldMap.put(field, mapWithRoot);
                    topLevelFieldMap = mapWithRoot;
                }
            } else {
                Map<String, Object> mapWithRoot = new LinkedHashMap<>();
                topLevelFieldMap.put(rootField, mapWithRoot);
                topLevelFieldMap = mapWithRoot;
            }

        } else {
            topLevelFieldMap = rootElements;
        }

        for (Element field : fieldElements) {
            String fieldType = ProcessorUtil.getElementType(field);

            if (fieldType.equals("java.lang.Object")) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid field type: " + fieldType, field);
                throw new ProcessingException();
            }

            SerializedName annotation = field.getAnnotation(SerializedName.class);
            String fieldName = field.getSimpleName().toString();
            String jsonObjectName;

            if (annotation != null && annotation.value().length() > 0) {
                jsonObjectName = annotation.value();
            } else {
                jsonObjectName = fieldName;
            }

            if (jsonObjectName.contains(".")) {
                //
                // When the last character is a dot, we should append the variable name to
                // the end of the field name, as this may reduce annotation repetition.
                //
                if (jsonObjectName.charAt(jsonObjectName.length() - 1) == '.') {
                    jsonObjectName += fieldName;
                }

                String[] split = jsonObjectName.split("\\.");
                int lastIndex = split.length - 1;

                Map<String, Object> currentMap = topLevelFieldMap;
                for (int i = 0; i < lastIndex + 1; i++) {
                    String currentKey = split[i];

                    if (i < lastIndex) {
                        Object o = currentMap.get(currentKey);
                        if (o == null) {
                            if (i < lastIndex) {
                                Map<String, Object> newMap = new LinkedHashMap<>();

                                currentMap.put(currentKey, newMap);
                                currentMap = newMap;
                            }
                        } else {
                            currentMap = (Map<String, Object>) o;
                        }

                    } else {
                        currentMap.put(currentKey, field);
                    }
                }

            } else {
                topLevelFieldMap.put(jsonObjectName, field);
            }

        }

        if (rootElements.size() > 0) {
            mCounterVariableCount = 0;
            mSafeVariableCount = 0;

            createObjectParser(0, codeBlock, rootElements);
        }

        // Final block of code.
        codeBlock.addStatement("return result");
        readMethod.addCode(codeBlock.build());

        typeBuilder.addMethod(readMethod.build());

        //
        //@Override
        //public void write(JsonWriter out, ImageSizes value) throws IOException {
        //
        MethodSpec.Builder writeMethod = MethodSpec.methodBuilder("write")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(JsonWriter.class, "out")
                .addParameter(elementClassName, "value")
                .addException(IOException.class)
                .addCode("// GsonPath does not support writing at this stage.\n");

        typeBuilder.addMethod(writeMethod.build());

        if (writeFile(elementPackagePath, typeBuilder)) {
            return new HandleResult(elementClassName, ClassName.get(elementPackagePath, adapterClassName));
        }

        throw new ProcessingException();
    }

    @Override
    public void onJavaFileBuilt(JavaFile.Builder builder) {
        builder.addStaticImport(GsonUtil.class, "*");
    }

    @Override
    void validateFieldAnnotations(Element field) throws ProcessingException {
        // For now, we only ensure that the flatten annotation is only added to a String.
        if (field.getAnnotation(FlattenJson.class) == null) {
            return;
        }

        if (!ProcessorUtil.getElementType(field).equals(STRING_CLASS_PATH)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FlattenObject can only be used on String variables");
            throw new ProcessingException();
        }
    }

    @Override
    String getClassNameSuffix() {
        return "_GsonTypeAdapter";
    }

}
