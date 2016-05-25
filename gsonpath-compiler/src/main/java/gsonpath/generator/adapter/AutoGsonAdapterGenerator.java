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
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

public class AutoGsonAdapterGenerator extends BaseAdapterGenerator {

    private Map<String, MandatoryFieldInfo> mandatoryInfoMap;

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

        boolean fieldsRequireAnnotation = autoGsonAnnotation.ignoreNonAnnotatedFields().booleanValue;
        boolean serializeNulls = autoGsonAnnotation.serializeNulls().booleanValue;
        char flattenDelimiter = autoGsonAnnotation.flattenDelimiter().value();
        GsonFieldValidationType gsonFieldValidationType = autoGsonAnnotation.fieldValidationType();

        // We want to translate the Gson Path 'GsonPathFieldNamingPolicy' enum into the standard Gson version.
        FieldNamingPolicy gsonFieldNamingPolicy = null;
        GsonPathFieldNamingPolicy gsonPathFieldNamingPolicy = autoGsonAnnotation.fieldNamingPolicy();
        if (gsonPathFieldNamingPolicy != null) {
            switch (gsonPathFieldNamingPolicy) {
                case IDENTITY:
                case IDENTITY_OR_INHERIT_DEFAULT_IF_AVAILABLE:
                    gsonFieldNamingPolicy = FieldNamingPolicy.IDENTITY;
                    break;

                case LOWER_CASE_WITH_DASHES:
                    gsonFieldNamingPolicy = FieldNamingPolicy.LOWER_CASE_WITH_DASHES;
                    break;

                case LOWER_CASE_WITH_UNDERSCORES:
                    gsonFieldNamingPolicy = FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
                    break;

                case UPPER_CAMEL_CASE:
                    gsonFieldNamingPolicy = FieldNamingPolicy.UPPER_CAMEL_CASE;
                    break;

                case UPPER_CAMEL_CASE_WITH_SPACES:
                    gsonFieldNamingPolicy = FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES;
                    break;
            }
        }

        // Annotation processors seem to make obtaining this value difficult!
        TypeMirror defaultsTypeMirror = null;

        try {
            autoGsonAnnotation.defaultConfiguration();
        } catch (MirroredTypeException mte) {
            defaultsTypeMirror = mte.getTypeMirror();
        }

        Element defaultsElement = processingEnv.getTypeUtils().asElement(defaultsTypeMirror);

        if (defaultsElement != null) {
            // If an inheritable annotation is used, used the default instead.
            GsonPathDefaultConfiguration defaultsAnnotation = defaultsElement.getAnnotation(GsonPathDefaultConfiguration.class);

            if (defaultsAnnotation == null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Defaults property must point to a class which uses the @GsonPathDefaultConfiguration annotation");
                throw new ProcessingException();
            }

            // Inherit 'ignoreNonAnnotatedFields'
            if (autoGsonAnnotation.ignoreNonAnnotatedFields().inheritDefaultIfAvailable) {
                fieldsRequireAnnotation = defaultsAnnotation.ignoreNonAnnotatedFields();
            }

            // Inherit 'serializeNulls'
            if (autoGsonAnnotation.serializeNulls().inheritDefaultIfAvailable) {
                serializeNulls = defaultsAnnotation.serializeNulls();
            }

            // Inherit 'flattenDelimiter'
            if (autoGsonAnnotation.flattenDelimiter().inheritDefaultIfAvailable()) {
                flattenDelimiter = defaultsAnnotation.flattenDelimiter();
            }

            // Inherit 'fieldNamingPolicy'
            if (autoGsonAnnotation.fieldNamingPolicy().equals(GsonPathFieldNamingPolicy.IDENTITY_OR_INHERIT_DEFAULT_IF_AVAILABLE)) {
                gsonFieldNamingPolicy = defaultsAnnotation.fieldNamingPolicy();
            }

            // Inherit 'fieldValidationType'
            if (gsonFieldValidationType.equals(GsonFieldValidationType.NO_VALIDATION_OR_INHERIT_DEFAULT_IF_AVAILABLE)) {
                gsonFieldValidationType = defaultsAnnotation.fieldValidationType();
            }
        }

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
                jsonObjectName = applyFieldNamingPolicy(gsonFieldNamingPolicy, fieldName);
            }

            boolean isMandatory = false;
            boolean isOptional = false;

            // Attempt to find a Nullable or NonNull annotation type.
            for (AnnotationMirror annotationMirror : field.getAnnotationMirrors()) {
                Element annotationElement = annotationMirror.getAnnotationType().asElement();

                switch (annotationElement.getSimpleName().toString())
                {
                    case "Nullable":
                        isOptional = true;
                        break;

                    // Intentional fall-through. There are several different variations!
                    case "NonNull":
                    case "Nonnull":
                    case "NotNull":
                    case "Notnull":
                        isMandatory = true;
                        break;
                }
            }

            // Fields cannot use both annotations.
            if (isMandatory && isOptional) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Field cannot have both Mandatory and Optional annotations", field);
                throw new ProcessingException();
            }

            // Primitives should not use either annotation.
            if (isMandatory || isOptional) {
                if (field.asType().getKind().isPrimitive()) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Primitives should not use NonNull or Nullable annotations", field);
                    throw new ProcessingException();
                }
            }

            boolean isRequired = isMandatory;

            // Using this policy everything is mandatory except for optionals.
            if (gsonFieldValidationType == GsonFieldValidationType.VALIDATE_ALL_EXCEPT_NULLABLE) {
                isRequired = true;
            }

            // Optionals will never fail regardless of the policy.
            if (isOptional || gsonFieldValidationType == GsonFieldValidationType.NO_VALIDATION) {
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

        // Adds the mandatory field index constants and also populates the mandatoryInfoMap values.
        mandatoryInfoMap = new LinkedHashMap<>();
        addMandatoryFieldConstants(typeBuilder, absoluteRootFieldTree);

        int mandatoryFieldSize = mandatoryInfoMap.size();
        if (mandatoryFieldSize > 0) {
            typeBuilder.addField(FieldSpec.builder(TypeName.INT, "MANDATORY_FIELDS_SIZE")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("" + mandatoryFieldSize)
                    .build());
        }

        typeBuilder.addMethod(createReadMethod(elementClassName, absoluteRootFieldTree));
        typeBuilder.addMethod(createWriteMethod(elementClassName, absoluteRootFieldTree, serializeNulls));

        if (writeFile(elementPackagePath, typeBuilder)) {
            return new HandleResult(elementClassName, ClassName.get(elementPackagePath, adapterClassName));
        }

        throw new ProcessingException();
    }

    /**
     * Add any mandatory field indexes as constants. This is done for code readability.
     * We will obtain the values using a depth-first recursion.
     */
    private void addMandatoryFieldConstants(TypeSpec.Builder typeBuilder, GsonFieldTree gsonFieldTree) {
        for (String branchKey : gsonFieldTree.keySet()) {
            Object treeObject = gsonFieldTree.get(branchKey);

            if (treeObject instanceof FieldInfo) {
                FieldInfo info = (FieldInfo) treeObject;

                //
                // For all required fields we add an index field so we can easily check whether the
                // value has been assigned after the json has been parsed.
                //
                if (info.isRequired) {
                    int mandatoryFieldSize = mandatoryInfoMap.size();
                    String fieldName = info.element.getSimpleName().toString();

                    String mandatoryFieldIndexName = "MANDATORY_INDEX_" + fieldName.toUpperCase();
                    typeBuilder.addField(FieldSpec.builder(TypeName.INT, mandatoryFieldIndexName)
                            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                            .initializer("" + mandatoryFieldSize)
                            .build());

                    // Keep track of the information for later use. Since this is a linked list, we keep track of insert order.
                    mandatoryInfoMap.put(fieldName, new MandatoryFieldInfo(mandatoryFieldIndexName, info));
                }
            } else {
                // Recursive call, navigating further down the tree.
                addMandatoryFieldConstants(typeBuilder, (GsonFieldTree) treeObject);
            }
        }
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

                // If we have any mandatory fields, we need to keep track of what has been assigned.
                if (mandatoryInfoMap.size() > 0) {
                    codeBlock.addStatement("boolean[] mandatoryFieldsCheckList = new boolean[MANDATORY_FIELDS_SIZE]");
                }
            }

            @Override
            public void onFieldAssigned(String fieldName) {
                MandatoryFieldInfo mandatoryFieldInfo = mandatoryInfoMap.get(fieldName);

                // When a field has been assigned, if it is a mandatory value, we note this down.
                if (mandatoryFieldInfo != null) {
                    codeBlock.addStatement("mandatoryFieldsCheckList[$L] = true", mandatoryFieldInfo.indexVariableName);
                    codeBlock.add("\n");
                }
            }

            @Override
            public void onNodeEmpty() {
            }
        });

        // If we have any mandatory fields, we now check if any values have been missed. If they are, it will raise an exception here.
        if (mandatoryInfoMap.size() > 0) {
            codeBlock.add("\n// Mandatory object validation\n");
            codeBlock.beginControlFlow("for (int mandatoryFieldIndex = 0; mandatoryFieldIndex < MANDATORY_FIELDS_SIZE; mandatoryFieldIndex++)");

            codeBlock.add("\n// Check if a mandatory value is missing.\n");
            codeBlock.beginControlFlow("if (!mandatoryFieldsCheckList[mandatoryFieldIndex])");

            // The code must figure out the correct field name to insert into the error message.
            codeBlock.add("\n// Find the field name of the missing json value.\n");
            codeBlock.addStatement("String fieldName = null");
            codeBlock.beginControlFlow("switch (mandatoryFieldIndex)");

            for (String mandatoryKey : mandatoryInfoMap.keySet()) {
                MandatoryFieldInfo mandatoryFieldInfo = mandatoryInfoMap.get(mandatoryKey);
                codeBlock.add("case $L:\n", mandatoryFieldInfo.indexVariableName);
                codeBlock.indent();
                codeBlock.addStatement("fieldName = \"$L\"", mandatoryFieldInfo.fieldInfo.jsonPath);
                codeBlock.addStatement("break");
                codeBlock.unindent();
                codeBlock.add("\n");
            }

            codeBlock.endControlFlow(); // Switch
            codeBlock.addStatement("throw new gsonpath.JsonFieldMissingException(\"Mandatory JSON element '\" + fieldName + \"' was not found for class '$L'\")", elementClassName);
            codeBlock.endControlFlow(); // If
            codeBlock.endControlFlow(); // For
        }

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

    /**
     * Keeps track of mandatory json field metadata.
     */
    private static class MandatoryFieldInfo {
        final String indexVariableName;
        final FieldInfo fieldInfo;

        private MandatoryFieldInfo(String indexVariableName, FieldInfo fieldInfo) {
            this.indexVariableName = indexVariableName;
            this.fieldInfo = fieldInfo;
        }
    }

}
