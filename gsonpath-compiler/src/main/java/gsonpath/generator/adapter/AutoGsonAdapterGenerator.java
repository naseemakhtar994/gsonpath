package gsonpath.generator.adapter;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.*;
import gsonpath.*;
import gsonpath.generator.BaseAdapterGenerator;
import gsonpath.generator.FieldInfo;
import gsonpath.generator.GsonFieldTree;
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
        boolean serializeNulls = autoGsonAnnotation.serializeNulls();
        FieldPolicy fieldPolicy = autoGsonAnnotation.fieldPolicy();

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
        GsonFieldTree absoluteRootFieldTree = new GsonFieldTree();
        GsonFieldTree gsonPathFieldTree = absoluteRootFieldTree;

        // The root element annotation prevents repetition in the SerializedName annotation.
        String rootField = autoGsonAnnotation.rootField();
        if (rootField.length() > 0) {
            gsonPathFieldTree = getElementsFromRoot(gsonPathFieldTree, rootField, flattenDelimiter);

        } else {
            gsonPathFieldTree = absoluteRootFieldTree;
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

            boolean isMandatory = field.getAnnotation(Mandatory.class) != null;
            boolean isOptional = field.getAnnotation(Optional.class) != null;

            // Fields cannot use both annotations.
            if (isMandatory && isOptional) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Field cannot have both Mandatory and Optional annotations", field);
                throw new ProcessingException();
            }

            boolean isRequired = isMandatory;

            // Using this policy everything is mandatory except for optionals.
            if (fieldPolicy == FieldPolicy.FAIL_ALL_EXCEPT_OPTIONAL) {
                isRequired = true;
            }

            // Optionals will never fail regardless of the policy.
            if (isOptional || fieldPolicy == FieldPolicy.NEVER_FAIL) {
                isRequired = false;
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

                GsonFieldTree currentFieldTree = gsonPathFieldTree;
                for (int i = 0; i < lastIndex + 1; i++) {
                    String currentKey = split[i];

                    if (i < lastIndex) {
                        Object o = currentFieldTree.get(currentKey);
                        if (o == null) {
                            if (i < lastIndex) {
                                GsonFieldTree newMap = new GsonFieldTree();

                                currentFieldTree.addTreeBranch(currentKey, newMap);
                                currentFieldTree = newMap;
                            }
                        } else {
                            if (o instanceof GsonFieldTree) {
                                currentFieldTree = (GsonFieldTree) o;

                            } else {
                                // If this value already exists, and it is not a tree branch, that means we have an invalid duplicate.
                                throwDuplicateFieldException(field, currentKey);
                            }
                        }

                    } else {
                        // We have reached the end of this branch, add the field at the end.
                        try {
                            currentFieldTree.addField(currentKey, new FieldInfo(field, jsonObjectName, isRequired));

                        } catch (IllegalArgumentException e) {
                            throwDuplicateFieldException(field, currentKey);
                        }
                    }
                }

            } else {
                try {
                    gsonPathFieldTree.addField(jsonObjectName, new FieldInfo(field, jsonObjectName, isRequired));

                } catch (IllegalArgumentException e) {
                    throwDuplicateFieldException(field, jsonObjectName);
                }
            }

        }

        typeBuilder.addMethod(createReadMethod(elementClassName, absoluteRootFieldTree));
        typeBuilder.addMethod(createWriteMethod(elementClassName, absoluteRootFieldTree, serializeNulls));

        if (writeFile(elementPackagePath, typeBuilder)) {
            return new HandleResult(elementClassName, ClassName.get(elementPackagePath, adapterClassName));
        }

        throw new ProcessingException();
    }

    private void throwDuplicateFieldException(Element field, String jsonKey) throws ProcessingException {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "Unexpected duplicate field '" + jsonKey + "' found. Each tree branch must use a unique value!", field);
        throw new ProcessingException();
    }

    /**
     * public ImageSizes read(JsonReader in) throws IOException {
     */
    private MethodSpec createReadMethod(final ClassName elementClassName, GsonFieldTree rootElements) throws ProcessingException {
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
    private MethodSpec createWriteMethod(ClassName elementClassName, GsonFieldTree rootElements, boolean serializeNulls) throws ProcessingException {
        MethodSpec.Builder writeMethod = MethodSpec.methodBuilder("write")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(JsonWriter.class, "out")
                .addParameter(elementClassName, "value")
                .addException(IOException.class);

        CodeBlock.Builder codeBlock = CodeBlock.builder();

        mSafeVariableCount = 0;

        // Initial block which prevents nulls being accessed.
        codeBlock.beginControlFlow("if (value == null)");
        codeBlock.addStatement("out.nullValue()");
        codeBlock.addStatement("return");
        codeBlock.endControlFlow();

        codeBlock.add("\n");
        codeBlock.add("// Begin\n");

        writeObject(0, codeBlock, rootElements, "", serializeNulls);

        writeMethod.addCode(codeBlock.build());
        return writeMethod.build();
    }

    private void writeObject(int fieldDepth, CodeBlock.Builder codeBlock, GsonFieldTree jsonMapping, String currentPath, boolean serializeNulls) throws ProcessingException {
        codeBlock.addStatement("out.beginObject()");

        for (String key : jsonMapping.keySet()) {
            Object value = jsonMapping.get(key);
            if (value instanceof FieldInfo) {
                FieldInfo fieldInfo = (FieldInfo) value;
                Element field = fieldInfo.element;

                // Make sure the field's annotations don't have any problems.
                validateFieldAnnotations(field);

                String gsonFieldType = ProcessorUtil.getElementType(field);

                //
                // Handle the primitive the same way as their wrapper class.
                // This ensures null safety is handled.
                //
                boolean isPrimitive = HANDLED_PRIMITIVES.contains(gsonFieldType);

                String objectName = "obj" + mSafeVariableCount;
                mSafeVariableCount++;

                codeBlock.addStatement("$T $L = value.$L", field, objectName, field.getSimpleName().toString());

                // If we aren't serializing nulls, we need to prevent the 'out.name' code being executed.
                if (!isPrimitive && !serializeNulls) {
                    codeBlock.beginControlFlow("if ($L != null)", objectName);
                }
                codeBlock.addStatement("out.name(\"$L\")", key);

                // Since we are serializing nulls, we defer the if-statement until after the name is written.
                if (!isPrimitive && serializeNulls) {
                    codeBlock.beginControlFlow("if ($L != null)", objectName);
                }

                boolean isStringType = gsonFieldType.equals(STRING_CLASS_PATH);
                if (isPrimitive || isStringType || HANDLED_BOXED_PRIMITIVES.contains(gsonFieldType)) {

                    codeBlock.addStatement("out.value($L)", objectName);

                } else {
                    String adapterName;

                    // TODO: Casting field to 'TypeElement' throws a cast exception, so we need to detect generics in a hacky way at the moment.
                    boolean isGenericField = gsonFieldType.contains("<");
                    if (isGenericField) {
                        // This is a generic type
                        adapterName = String.format("new com.google.gson.reflect.TypeToken<%s>(){}", gsonFieldType);

                    } else {
                        adapterName = gsonFieldType + ".class";
                    }

                    codeBlock.addStatement("mGson.getAdapter($L).write(out, $L)", adapterName, objectName);

                }

                // If we are serializing nulls, we need to ensure we output it here.
                if (!isPrimitive) {
                    if (serializeNulls) {
                        codeBlock.nextControlFlow("else");
                        codeBlock.addStatement("out.nullValue()");
                    }
                    codeBlock.endControlFlow();
                }
                codeBlock.add("\n");

            } else {
                GsonFieldTree nextLevelMap = (GsonFieldTree) value;
                if (nextLevelMap.size() > 0) {
                    String newPath;
                    if (currentPath.length() > 0) {
                        newPath = currentPath + "." + key;
                    } else {
                        newPath = key;
                    }

                    // Add a comment mentioning what nested object we are current pointing at.
                    codeBlock.add("\n// Begin $L\n", newPath);
                    codeBlock.addStatement("out.name(\"$L\")", key);
                    writeObject(fieldDepth + 1, codeBlock, nextLevelMap, newPath, serializeNulls);
                }
            }
        }

        codeBlock.add("// End $L\n", currentPath);
        codeBlock.addStatement("out.endObject()");
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
