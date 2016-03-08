package gsonpath;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
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
    private static final TypeName IO_EXCEPTION_TYPE = ClassName.get(IOException.class);
    private static final TypeName GSON_TYPE = ClassName.get("com.google.gson", "Gson");

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(GsonPathClass.class)) {
            System.out.println("Handling element: " + element.getSimpleName());
            if (!jsonPathClassHandler((TypeElement) element)) {
                return false;
            }
        }
        return true;
    }

    private boolean jsonPathClassHandler(TypeElement element) {
        String packagePath = getElementPackage(element);
        ClassName jsonPathType = getElementClassName(element);
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get("com.google.gson", "TypeAdapter"), jsonPathType);

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(GSON_TYPE, "gson")
                .addStatement("this.$N = $N", "mGson", "gson")
                .build();

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(element.getSimpleName() + "_GsonTypeAdapter")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(parameterizedTypeName)
                .addField(GSON_TYPE, "mGson", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(constructor);

        //
        //@Override
        //public ImageSizes read(JsonReader in) throws IOException {
        //
        MethodSpec.Builder readMethod = MethodSpec.methodBuilder("read")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(jsonPathType)
                .addParameter(ClassName.get("com.google.gson.stream", "JsonReader"), "in")
                .addException(IO_EXCEPTION_TYPE);

        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.addStatement("$T result = new $T()", jsonPathType, jsonPathType);

        boolean fieldsRequireAnnotation = element.getAnnotation(GsonPathClass.class).ignoreNonAnnotatedFields();

        List<Element> fieldElements = new ArrayList<>();
        for (Element child : Utils.getAllFieldElements(element, elementUtils, typeUtils)) {

            if (fieldsRequireAnnotation && (child.getAnnotation(GsonPathElement.class) == null)) {
                continue;
            }

            fieldElements.add(child);
        }

        Map<String, Object> jsonMapping = new LinkedHashMap<>();
        // Obtain the correct mapping structure beforehand.
        for (Element field : fieldElements) {
            if (!validateFieldType(field)) {
                return false;
            }

            GsonPathElement annotation = field.getAnnotation(GsonPathElement.class);
            String fieldName = field.getSimpleName().toString();
            String jsonObjectName;

            if (annotation != null && annotation.value().length() > 0) {
                jsonObjectName = annotation.value();
            } else {
                jsonObjectName = fieldName;
            }

            if (jsonObjectName.contains(".")) {
                String[] split = jsonObjectName.split("\\.");
                Object o = jsonMapping.get(split[0]);
                if (o != null) {
                    Map<String, Object> casted = (Map<String, Object>) o;
                    casted.put(split[1], field);

                } else {
                    Map<String, Object> casted = new LinkedHashMap<>();
                    casted.put(split[1], field);

                    jsonMapping.put(split[0], casted);
                }

            } else {
                jsonMapping.put(jsonObjectName, field);
            }

        }

        if (jsonMapping.size() > 0) {
            if (!createObjectParser(codeBlock, jsonMapping))
                return false;
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
                .addParameter(ClassName.get("com.google.gson.stream", "JsonWriter"), "out")
                .addParameter(jsonPathType, "value")
                .addException(IO_EXCEPTION_TYPE)
                .addCode("// GsonPath does not support writing at this stage.\n");

        typeBuilder.addMethod(writeMethod.build());

        TypeSpec typeSpec = typeBuilder.build();

        try {
            JavaFile.builder(packagePath, typeSpec)
                    .addStaticImport(ClassName.get("gsonpath", "GsonPathUtil"), "getStringSafely")
                    .addStaticImport(ClassName.get("gsonpath", "GsonPathUtil"), "getBooleanSafely")
                    .addStaticImport(ClassName.get("gsonpath", "GsonPathUtil"), "getIntegerSafely")
                    .addStaticImport(ClassName.get("gsonpath", "GsonPathUtil"), "getLongSafely")
                    .addStaticImport(ClassName.get("gsonpath", "GsonPathUtil"), "getDoubleSafely")
                    .build().writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error while writing javapoet file", element);
            return false;
        }

        return true;
    }

    private boolean createObjectParser(CodeBlock.Builder codeBlock, Map<String, Object> jsonMapping) {

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
                if (gsonMethodType.equals("boolean") ||
                        gsonMethodType.equals("int") ||
                        gsonMethodType.equals("long") ||
                        gsonMethodType.equals("double")) {

                    // Handle primitives.
                    gsonMethodType = Character.toUpperCase(gsonMethodType.charAt(0)) + gsonMethodType.substring(1);
                    codeBlock.addStatement("result.$L = in.next$L()", field.getSimpleName().toString(), gsonMethodType);

                } else {
                    boolean isStringType = gsonMethodType.equals("java.lang.String");
                    if (isStringType ||
                            gsonMethodType.equals("java.lang.Boolean") ||
                            gsonMethodType.equals("java.lang.Integer") ||
                            gsonMethodType.equals("java.lang.Long") ||
                            gsonMethodType.equals("java.lang.Double")) {

                        gsonMethodType = gsonMethodType.replace("java.lang.", "");

                        // Special handling for strings.
                        boolean handled = false;
                        if (isStringType) {
                            GsonPathElement annotation = field.getAnnotation(GsonPathElement.class);
                            if (annotation != null && annotation.collapseJson()) {
                                handled = true;
                                codeBlock.addStatement("result.$L = mGson.getAdapter(com.google.gson.JsonElement.class).read(in).toString()", field.getSimpleName().toString());
                            }
                        }

                        if (!handled) {
                            codeBlock.addStatement("result.$L = get$LSafely(in)", field.getSimpleName().toString(), gsonMethodType);
                        }
                    } else {
                        // Handle every other possible class by falling back onto the gson adapter.
                        codeBlock.addStatement("result.$L = mGson.getAdapter($L.class).read(in)", field.getSimpleName().toString(), gsonMethodType);
                    }
                }

            } else {
                if (!createObjectParser(codeBlock, (Map<String, Object>) value)) {
                    return false;
                }
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

        return true;
    }

    private String getFieldType(Element field) {
        return field.asType().toString();
    }

    private boolean validateFieldType(Element field) {
        String fieldType = getFieldType(field);

        boolean result = !fieldType.equals("java.lang.Object");

        if (!result) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid field type: " + fieldType, field);
        }

        return result;
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
        supportedTypes.add(GsonPathClass.class.getCanonicalName());
        supportedTypes.add(GsonPathElement.class.getCanonicalName());
        return supportedTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}