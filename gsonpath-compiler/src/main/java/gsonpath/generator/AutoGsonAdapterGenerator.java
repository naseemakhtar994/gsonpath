package gsonpath.generator;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import gsonpath.AutoGsonAdapter;
import gsonpath.GsonPathExclude;
import gsonpath.GsonPathField;
import gsonpath.GsonPathUtil;
import gsonpath.ProcessingException;
import gsonpath.ProcessorUtil;

/**
 * Created by Lachlan on 12/03/2016.
 */
public class AutoGsonAdapterGenerator extends Generator {
    private static final String ADAPTER_SUFFIX = "_GsonTypeAdapter";

    private static final String GSON_PACKAGE = "com.google.gson";

    private static final Set<String> HANDLED_PRIMITIVES = new HashSet<>(Arrays.asList(
            "boolean", "int", "long", "double"
    ));

    private static final Set<String> HANDLED_BOXED_PRIMITIVES = new HashSet<>(Arrays.asList(
            "java.lang.Boolean", "java.lang.Integer", "java.lang.Long", "java.lang.Double"
    ));

    private int mVariableCount; // Used to avoid naming conflicts.

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

        String adapterClassName = element.getSimpleName() + ADAPTER_SUFFIX;
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

        boolean fieldsRequireAnnotation = element.getAnnotation(AutoGsonAdapter.class).ignoreNonAnnotatedFields();

        List<Element> fieldElements = new ArrayList<>();
        for (Element child : ProcessorUtil.getAllFieldElements(element, processingEnv.getElementUtils(), processingEnv.getTypeUtils())) {

            if (child.getModifiers().contains(Modifier.FINAL) ||
                    child.getModifiers().contains(Modifier.STATIC) ||
                    child.getModifiers().contains(Modifier.TRANSIENT)) {

                continue;
            }

            if (fieldsRequireAnnotation && (child.getAnnotation(GsonPathField.class) == null)) {
                continue;
            }

            // Ignore any excluded fields
            if (child.getAnnotation(GsonPathExclude.class) != null) {
                continue;
            }

            fieldElements.add(child);
        }

        // Obtain the correct mapping structure beforehand.
        Map<String, Object> jsonMapping = new LinkedHashMap<>();
        for (Element field : fieldElements) {
            String fieldType = ProcessorUtil.getElementType(field);

            if (fieldType.equals("java.lang.Object")) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid field type: " + fieldType, field);
                throw new ProcessingException();
            }

            GsonPathField annotation = field.getAnnotation(GsonPathField.class);
            String fieldName = field.getSimpleName().toString();
            String jsonObjectName;

            if (annotation != null && annotation.value().length() > 0) {
                jsonObjectName = annotation.value();
            } else {
                jsonObjectName = fieldName;
            }

            if (jsonObjectName.contains(".")) {
                String[] split = jsonObjectName.split("\\.");
                int lastIndex = split.length - 1;

                Map<String, Object> currentMap = jsonMapping;
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
                jsonMapping.put(jsonObjectName, field);
            }

        }

        if (jsonMapping.size() > 0) {
            mVariableCount = 0;

            createObjectParser(codeBlock, jsonMapping);
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
        builder.addStaticImport(GsonPathUtil.class, "*");
    }

    private void createObjectParser(CodeBlock.Builder codeBlock, Map<String, Object> jsonMapping) {
        codeBlock.addStatement("in.beginObject()");
        codeBlock.beginControlFlow("while (in.hasNext())");
        codeBlock.beginControlFlow("switch(in.nextName())");

        for (String key : jsonMapping.keySet()) {
            codeBlock.add("case \"$L\":\n", key);
            codeBlock.indent();

            Object value = jsonMapping.get(key);
            if (value instanceof Element) {
                Element field = (Element) value;

                String gsonMethodType = ProcessorUtil.getElementType(field);
                if (HANDLED_PRIMITIVES.contains(gsonMethodType)) {

                    // Handle primitives.
                    gsonMethodType = Character.toUpperCase(gsonMethodType.charAt(0)) + gsonMethodType.substring(1);
                    codeBlock.addStatement("result.$L = in.next$L()", field.getSimpleName().toString(), gsonMethodType);

                } else {
                    boolean isStringType = gsonMethodType.equals("java.lang.String");
                    boolean callToString = false;

                    if (isStringType || HANDLED_BOXED_PRIMITIVES.contains(gsonMethodType)) {

                        gsonMethodType = gsonMethodType.replace("java.lang.", "");

                        // Special handling for strings.
                        boolean handled = false;
                        if (isStringType) {
                            GsonPathField annotation = field.getAnnotation(GsonPathField.class);
                            if (annotation != null && annotation.collapseJson()) {
                                handled = true;
                                codeBlock.addStatement("com.google.gson.JsonElement safeValue$L = mGson.getAdapter(com.google.gson.JsonElement.class).read(in)", mVariableCount);

                                callToString = true;
                            }
                        }

                        if (!handled) {
                            codeBlock.addStatement("$L safeValue$L = get$LSafely(in)", gsonMethodType, mVariableCount, gsonMethodType);
                        }
                    } else {
                        // Handle every other possible class by falling back onto the gson adapter.
                        codeBlock.addStatement("$L safeValue$L = mGson.getAdapter($L.class).read(in)", gsonMethodType, mVariableCount, gsonMethodType);
                    }

                    codeBlock.beginControlFlow("if (safeValue$L != null)", mVariableCount);
                    codeBlock.addStatement("result.$L = safeValue$L$L", field.getSimpleName().toString(), mVariableCount, callToString ? ".toString()" : "");
                    codeBlock.endControlFlow();

                    mVariableCount++;
                }

            } else {
                createObjectParser(codeBlock, (Map<String, Object>) value);
            }
            codeBlock.addStatement("break");
            codeBlock.unindent();
        }

        codeBlock.add("default:\n");
        codeBlock.indent();
        codeBlock.addStatement("in.skipValue()");
        codeBlock.addStatement("break");
        codeBlock.unindent();

        codeBlock.endControlFlow();
        codeBlock.endControlFlow();
        codeBlock.addStatement("in.endObject()");
    }

}
