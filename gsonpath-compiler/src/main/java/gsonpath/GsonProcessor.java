package gsonpath;

import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by Lachlan on 1/03/2016.
 */
@AutoService(Processor.class)
public class GsonProcessor extends AbstractProcessor {
    private static final String ADAPTER_SUFFIX = "_GsonTypeAdapter";
    private static final String GSON_PACKAGE = "com.google.gson";

    private static final Set<String> HANDLED_PRIMITIVES = new HashSet<>(Arrays.asList(
            "boolean", "int", "long", "double"
    ));

    private static final Set<String> HANDLED_BOXED_PRIMITIVES = new HashSet<>(Arrays.asList(
            "java.lang.Boolean", "java.lang.Integer", "java.lang.Long", "java.lang.Double"
    ));

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;

    private int mVariableCount; // Used to avoid naming conflicts.

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Set<? extends Element> generatedAdapters = env.getElementsAnnotatedWith(AutoGsonAdapter.class);

        for (Element element : generatedAdapters) {
            System.out.println("Handling element: " + element.getSimpleName());

            try {
                jsonPathClassHandler((TypeElement) element);
            } catch (UnexpectedAnnotationException e) {
                return false;
            }

        }

        if (generatedAdapters.size() > 0) {
            // Create the GsonPathLoader which is used by the GsonPathTypeAdapterFactory class.
            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder("GeneratedGsonPathLoader")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addSuperinterface(GsonPathLoader.class);

            //
            // <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type);
            //
            MethodSpec.Builder createMethod = MethodSpec.methodBuilder("create")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeAdapter.class)
                    .addParameter(Gson.class, "gson")
                    .addParameter(TypeToken.class, "type");

            CodeBlock.Builder codeBlock = CodeBlock.builder();
            codeBlock.addStatement("Class rawType = type.getRawType()");

            int currentAdapterIndex = 0;
            for (Element adapter : generatedAdapters) {
                ClassName adapterClassName = getElementClassName(adapter);

                if (currentAdapterIndex == 0) {
                    codeBlock.beginControlFlow("if (rawType.equals($T.class))", adapterClassName);
                } else {
                    codeBlock.add("\n"); // New line for easier readability.
                    codeBlock.nextControlFlow("else if (rawType.equals($T.class))", adapterClassName);
                }
                codeBlock.addStatement("return new $T(gson)", ClassName.get(getElementPackage(adapter), adapter.getSimpleName() + ADAPTER_SUFFIX));

                currentAdapterIndex++;
            }
            codeBlock.endControlFlow();
            codeBlock.addStatement("return null");

            createMethod.addCode(codeBlock.build());
            typeBuilder.addMethod(createMethod.build());

            try {
                JavaFile.builder("gsonpath", typeBuilder.build())
                        .build()
                        .writeTo(filer);

            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error while writing javapoet file: " + e.getMessage());

                return false;
            }
        }

        return true;
    }

    private JavaFile jsonPathClassHandler(TypeElement element) throws UnexpectedAnnotationException {
        String packagePath = getElementPackage(element);
        ClassName jsonPathType = getElementClassName(element);
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(GSON_PACKAGE, "TypeAdapter"), jsonPathType);

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson")
                .addStatement("this.$N = $N", "mGson", "gson")
                .build();

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(element.getSimpleName() + ADAPTER_SUFFIX)
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
                .returns(jsonPathType)
                .addParameter(JsonReader.class, "in")
                .addException(IOException.class);

        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.addStatement("$T result = new $T()", jsonPathType, jsonPathType);

        boolean fieldsRequireAnnotation = element.getAnnotation(AutoGsonAdapter.class).ignoreNonAnnotatedFields();

        List<Element> fieldElements = new ArrayList<>();
        for (Element child : Utils.getAllFieldElements(element, elementUtils, typeUtils)) {

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

        Map<String, Object> jsonMapping = new LinkedHashMap<>();
        // Obtain the correct mapping structure beforehand.
        for (Element field : fieldElements) {
            validateFieldType(field);

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
                .addParameter(jsonPathType, "value")
                .addException(IOException.class)
                .addCode("// GsonPath does not support writing at this stage.\n");

        typeBuilder.addMethod(writeMethod.build());

        TypeSpec typeSpec = typeBuilder.build();

        try {
            JavaFile javaFile = JavaFile.builder(packagePath, typeSpec)
                    .addStaticImport(GsonPathUtil.class, "*")
                    .build();
            javaFile.writeTo(filer);

            return javaFile;
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error while writing javapoet file", element);

            throw new UnexpectedAnnotationException();
        }
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

                String gsonMethodType = getFieldType(field);
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

    private String getFieldType(Element field) {
        return field.asType().toString();
    }

    private void validateFieldType(Element field) throws UnexpectedAnnotationException {
        String fieldType = getFieldType(field);

        if (fieldType.equals("java.lang.Object")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid field type: " + fieldType, field);
            throw new UnexpectedAnnotationException();
        }
    }

    private String getElementPackage(Element element) {
        return element.getEnclosingElement().asType().toString();
    }

    private ClassName getElementClassName(Element element) {
        return ClassName.get(getElementPackage(element), element.getSimpleName().toString());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedTypes = new LinkedHashSet<>();
        supportedTypes.add(AutoGsonAdapter.class.getCanonicalName());
        supportedTypes.add(GsonPathField.class.getCanonicalName());
        return supportedTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public static class UnexpectedAnnotationException extends Exception {

    }
}