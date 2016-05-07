package gsonpath.generator.adapter;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.*;
import gsonpath.*;
import gsonpath.generator.BaseAdapterGenerator;
import gsonpath.generator.HandleResult;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

public class AutoGsonAdapterGenerator extends BaseAdapterGenerator {

    public AutoGsonAdapterGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public HandleResult handle(TypeElement element) throws ProcessingException {
        String elementPackagePath = ProcessorUtil.getElementPackage(element);
        final ClassName elementClassName = ProcessorUtil.getElementJavaPoetClassName(element);
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(GSON_PACKAGE, "TypeAdapter"), elementClassName);

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson")
                .addStatement("this.$N = $N", "mGson", "gson")
                .build();

        String adapterClassName = getClassName(element);
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(adapterClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(parameterizedTypeName)
                .addField(Gson.class, "mGson", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(constructor);

        AutoGsonAdapter autoGsonAnnotation = element.getAnnotation(AutoGsonAdapter.class);
        boolean fieldsRequireAnnotation = autoGsonAnnotation.ignoreNonAnnotatedFields();
        char flattenDelimiter = autoGsonAnnotation.flattenDelimiter();
        FieldNamingPolicy fieldNamingPolicy = autoGsonAnnotation.fieldNamingPolicy();

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
            topLevelFieldMap = getElementsFromRoot(topLevelFieldMap, rootField, flattenDelimiter);

        } else {
            topLevelFieldMap = rootElements;
        }

        for (Element field : fieldElements) {
            String fieldType = ProcessorUtil.getElementType(field);

            if (fieldType.equals("java.lang.Object")) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid field type: " + fieldType, field);
                throw new ProcessingException();
            }

            SerializedName serializedNameAnnotation = field.getAnnotation(SerializedName.class);
            String fieldName = field.getSimpleName().toString();
            String jsonObjectName;

            if (serializedNameAnnotation != null && serializedNameAnnotation.value().length() > 0) {
                jsonObjectName = serializedNameAnnotation.value();

            } else {
                // Since the serialized annotation wasn't specified, we need to apply the naming policy instead.
                jsonObjectName = applyFieldNamingPolicy(fieldNamingPolicy, fieldName);
            }

            if (jsonObjectName.contains(String.valueOf(flattenDelimiter))) {
                //
                // When the last character is a delimiter, we should append the variable name to
                // the end of the field name, as this may reduce annotation repetition.
                //
                if (jsonObjectName.charAt(jsonObjectName.length() - 1) == flattenDelimiter) {
                    jsonObjectName += fieldName;
                }

                // Ensure that the delimiter is correctly escaped before attempting to split the string.
                String regexSafeDelimiter = Pattern.quote(String.valueOf(flattenDelimiter));
                String[] split = jsonObjectName.split(regexSafeDelimiter);
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

        typeBuilder.addMethod(createReadMethod(elementClassName, rootElements));
        typeBuilder.addMethod(createWriteMethod(elementClassName));

        if (writeFile(elementPackagePath, typeBuilder)) {
            return new HandleResult(elementClassName, ClassName.get(elementPackagePath, adapterClassName));
        }

        throw new ProcessingException();
    }

    /**
     * public ImageSizes read(JsonReader in) throws IOException {
     */
    private MethodSpec createReadMethod(final ClassName elementClassName, Map<String, Object> rootElements) throws ProcessingException {
        MethodSpec.Builder readMethod = MethodSpec.methodBuilder("read")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(elementClassName)
                .addParameter(JsonReader.class, "in")
                .addException(IOException.class);

        final CodeBlock.Builder codeBlock = CodeBlock.builder();

        mCounterVariableCount = 0;
        mSafeVariableCount = 0;

        createObjectParser(0, codeBlock, rootElements, new ObjectParserCallback() {
            @Override
            public void onInitialObjectNull() {
                codeBlock.addStatement("return null");
            }

            @Override
            public void onInitialise() {
                codeBlock.addStatement("$T result = new $T()", elementClassName, elementClassName);
            }

            @Override
            public void onNodeEmpty() {
            }
        });

        codeBlock.addStatement("return result");
        readMethod.addCode(codeBlock.build());

        return readMethod.build();
    }

    /**
     * public void write(JsonWriter out, ImageSizes value) throws IOException {
     */
    private MethodSpec createWriteMethod(ClassName elementClassName) throws ProcessingException {
        MethodSpec.Builder writeMethod = MethodSpec.methodBuilder("write")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(JsonWriter.class, "out")
                .addParameter(elementClassName, "value")
                .addException(IOException.class)
                .addCode("// GsonPath does not support writing at this stage.\n");

        return writeMethod.build();
    }

    @Override
    public void onJavaFileBuilt(JavaFile.Builder builder) {
        builder.addStaticImport(GsonUtil.class, "*");
    }

    @Override
    protected void validateFieldAnnotations(Element field) throws ProcessingException {
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
    protected String getClassNameSuffix() {
        return "GsonTypeAdapter";
    }

    /**
     * Applies the gson field naming policy using the given field name.
     *
     * @param fieldNamingPolicy the field naming policy to apply
     * @param fieldName         the name being altered.
     * @return the altered name.
     */
    private String applyFieldNamingPolicy(FieldNamingPolicy fieldNamingPolicy, String fieldName) throws ProcessingException {
        //
        // Unfortunately the field naming policy uses a Field parameter to translate name.
        // As a result, for now it was decided to create a fake field class which supplies the correct name,
        // as opposed to copying the logic from GSON and potentially breaking compatibility if they add another enum.
        //
        Constructor<Field> fieldConstructor = (Constructor<Field>) Field.class.getDeclaredConstructors()[0];
        fieldConstructor.setAccessible(true);
        Field fakeField;
        try {
            fakeField = fieldConstructor.newInstance(null, fieldName, null, -1, -1, null, null);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error while creating 'fake' field for naming policy.");
            throw new ProcessingException();
        }

        // Applies the naming transformation on the input field name.
        return fieldNamingPolicy.translateName(fakeField);
    }

}
