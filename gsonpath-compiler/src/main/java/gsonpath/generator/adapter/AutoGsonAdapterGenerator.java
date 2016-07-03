package gsonpath.generator.adapter;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.*;
import gsonpath.*;
import gsonpath.generator.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

public class AutoGsonAdapterGenerator extends BaseAdapterGenerator {
    private int mSafeVariableCount = 0;

    public AutoGsonAdapterGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public HandleResult handle(TypeElement modelElement) throws ProcessingException {
        ClassName modelClassName = ClassName.get(modelElement);
        ClassName adapterClassName = ClassName.get(modelClassName.packageName(), generateClassName(modelClassName));

        TypeSpec.Builder adapterTypeBuilder = TypeSpec.classBuilder(adapterClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), modelClassName))
                .addField(Gson.class, "mGson", Modifier.PRIVATE, Modifier.FINAL);

        // Add the constructor which takes a gson instance for future use.
        adapterTypeBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson")
                .addStatement("this.$N = $N", "mGson", "gson")
                .build());

        AutoGsonAdapter autoGsonAnnotation = modelElement.getAnnotation(AutoGsonAdapter.class);

        boolean fieldsRequireAnnotation = autoGsonAnnotation.ignoreNonAnnotatedFields().booleanValue;
        boolean serializeNulls = autoGsonAnnotation.serializeNulls().booleanValue;
        char flattenDelimiter = autoGsonAnnotation.flattenDelimiter().value();
        GsonFieldValidationType gsonFieldValidationType = autoGsonAnnotation.fieldValidationType();

        // We want to translate the Gson Path 'GsonPathFieldNamingPolicy' enum into the standard Gson version.
        FieldNamingPolicy gsonFieldNamingPolicy = getGsonFieldNamingPolicy(autoGsonAnnotation.fieldNamingPolicy());

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
                throw new ProcessingException("Defaults property must point to a class which uses the @GsonPathDefaultConfiguration annotation");
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

        ClassName concreteClassName;
        List<FieldInfo> fieldInfoList;
        boolean isModelInterface = modelElement.getKind().isInterface();
        if (!isModelInterface) {
            concreteClassName = modelClassName;

            fieldInfoList = getModelFieldsFromElement(modelElement, fieldsRequireAnnotation);

        } else {
            ModelInterfaceGenerator.InterfaceInfo interfaceInfo = new ModelInterfaceGenerator(processingEnv).handle(modelElement);
            concreteClassName = interfaceInfo.parentClassName;

            fieldInfoList = getModelFieldsFromInterface(interfaceInfo);

            // Model interfaces must use field validation to prevent issues with primitives.
            switch (gsonFieldValidationType) {
                case NO_VALIDATION:
                case NO_VALIDATION_OR_INHERIT_DEFAULT_IF_AVAILABLE:
                    gsonFieldValidationType = GsonFieldValidationType.VALIDATE_EXPLICIT_NON_NULL;
                    break;
            }
        }

        GsonFieldTree fieldTree = createFieldTree(fieldInfoList, autoGsonAnnotation.rootField(),
                flattenDelimiter, gsonFieldNamingPolicy, gsonFieldValidationType);

        // Adds the mandatory field index constants and also populates the mandatoryInfoMap values.
        Map<String, MandatoryFieldInfo> mandatoryInfoMap = new LinkedHashMap<>();
        createMandatoryFieldConstants(mandatoryInfoMap, adapterTypeBuilder, fieldTree);

        int mandatoryFieldSize = mandatoryInfoMap.size();
        if (mandatoryFieldSize > 0) {
            adapterTypeBuilder.addField(FieldSpec.builder(TypeName.INT, "MANDATORY_FIELDS_SIZE")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("" + mandatoryFieldSize)
                    .build());
        }

        adapterTypeBuilder.addMethod(createReadMethod(modelClassName, concreteClassName, mandatoryInfoMap, fieldTree));

        if (!isModelInterface) {
            adapterTypeBuilder.addMethod(createWriteMethod(modelClassName, fieldTree, serializeNulls));

        } else {
            adapterTypeBuilder.addMethod(createEmptyWriteMethod(modelClassName));
        }

        if (writeFile(adapterClassName.packageName(), adapterTypeBuilder)) {
            return new HandleResult(modelClassName, adapterClassName);
        }

        throw new ProcessingException("Failed to write generated file: " + adapterClassName.simpleName());
    }

    private List<FieldInfo> getModelFieldsFromElement(TypeElement modelElement, boolean fieldsRequireAnnotation) {
        List<FieldInfo> fieldInfoList = new ArrayList<>();

        // Obtain all possible elements contained within the annotated class, including inherited fields.
        for (final Element memberElement : processingEnv.getElementUtils().getAllMembers(modelElement)) {

            // Ignore modelElement that are not fields.
            if (memberElement.getKind() != ElementKind.FIELD) {
                continue;
            }

            // Ignore final, static and transient fields.
            Set<Modifier> modifiers = memberElement.getModifiers();
            if (modifiers.contains(Modifier.FINAL) || modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.TRANSIENT)) {
                continue;
            }

            if (fieldsRequireAnnotation && (memberElement.getAnnotation(SerializedName.class) == null)) {
                continue;
            }

            // Ignore any excluded fields
            if (memberElement.getAnnotation(ExcludeField.class) != null) {
                continue;
            }

            fieldInfoList.add(new FieldInfo() {
                @Override
                public TypeName getTypeName() {
                    return TypeName.get(memberElement.asType());
                }

                @Override
                public String getParentClassName() {
                    return memberElement.getEnclosingElement().toString();
                }

                @Override
                public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
                    return memberElement.getAnnotation(annotationClass);
                }

                @Override
                public String getFieldName() {
                    return memberElement.getSimpleName().toString();
                }

                @Override
                public String[] getAnnotationNames() {
                    List<? extends AnnotationMirror> annotationMirrors = memberElement.getAnnotationMirrors();

                    String[] annotationNames = new String[annotationMirrors.size()];

                    for (int i = 0; i < annotationMirrors.size(); i++) {
                        AnnotationMirror annotationMirror = annotationMirrors.get(i);
                        Element annotationElement = annotationMirror.getAnnotationType().asElement();

                        annotationNames[i] = annotationElement.getSimpleName().toString();
                    }
                    return annotationNames;
                }

                @Override
                public Element getElement() {
                    return memberElement;
                }
            });
        }
        return fieldInfoList;
    }

    private List<FieldInfo> getModelFieldsFromInterface(final ModelInterfaceGenerator.InterfaceInfo interfaceInfo) {
        List<FieldInfo> fieldInfoList = new ArrayList<>();

        for (final ModelInterfaceGenerator.InterfaceFieldInfo fieldSpec : interfaceInfo.fieldInfo) {

            fieldInfoList.add(new FieldInfo() {
                @Override
                public TypeName getTypeName() {
                    return fieldSpec.typeName;
                }

                @Override
                public String getParentClassName() {
                    return interfaceInfo.parentClassName.toString();
                }

                @Override
                public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
                    return fieldSpec.methodElement.getAnnotation(annotationClass);
                }

                @Override
                public String getFieldName() {
                    return fieldSpec.fieldName;
                }

                @Override
                public String[] getAnnotationNames() {
                    List<? extends AnnotationMirror> annotationMirrors = fieldSpec.methodElement.getAnnotationMirrors();

                    String[] annotationNames = new String[annotationMirrors.size()];

                    for (int i = 0; i < annotationMirrors.size(); i++) {
                        AnnotationMirror annotationMirror = annotationMirrors.get(i);
                        Element annotationElement = annotationMirror.getAnnotationType().asElement();

                        annotationNames[i] = annotationElement.getSimpleName().toString();
                    }
                    return annotationNames;
                }

                @Override
                public Element getElement() {
                    return fieldSpec.methodElement;
                }
            });
        }
        return fieldInfoList;
    }

    private GsonFieldTree createFieldTree(List<FieldInfo> fieldInfoList,
                                          String rootField,
                                          char flattenDelimiter,
                                          FieldNamingPolicy gsonFieldNamingPolicy,
                                          GsonFieldValidationType gsonFieldValidationType) throws ProcessingException {

        // Obtain the correct mapping structure beforehand.
        GsonFieldTree absoluteRootFieldTree = new GsonFieldTree();
        GsonFieldTree gsonPathFieldTree = absoluteRootFieldTree;

        if (rootField.length() > 0) {
            gsonPathFieldTree = createGsonTreeFromRootField(gsonPathFieldTree, rootField, flattenDelimiter);

        } else {
            gsonPathFieldTree = absoluteRootFieldTree;
        }

        String regexSafeDelimiter = Pattern.quote(String.valueOf(flattenDelimiter));

        for (FieldInfo fieldInfo : fieldInfoList) {
            TypeName fieldTypeName = fieldInfo.getTypeName();

            if (fieldTypeName.equals(TypeName.OBJECT)) {
                throw new ProcessingException("Invalid field type: " + fieldTypeName, fieldInfo.getElement());
            }

            SerializedName serializedNameAnnotation = fieldInfo.getAnnotation(SerializedName.class);
            String fieldName = fieldInfo.getFieldName();
            String jsonFieldPath;

            if (serializedNameAnnotation != null && serializedNameAnnotation.value().length() > 0) {
                jsonFieldPath = serializedNameAnnotation.value();

            } else {
                // Since the serialized annotation wasn't specified, we need to apply the naming policy instead.
                jsonFieldPath = applyFieldNamingPolicy(gsonFieldNamingPolicy, fieldName);
            }

            boolean isMandatory = false;
            boolean isOptional = false;

            // Attempt to find a Nullable or NonNull annotation type.
            for (String annotationName : fieldInfo.getAnnotationNames()) {
                switch (annotationName) {
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
                throw new ProcessingException("Field cannot have both Mandatory and Optional annotations", fieldInfo.getElement());
            }

            // Primitives should not use either annotation.
            boolean isPrimitive = fieldTypeName.isPrimitive();
            if (isPrimitive && (isMandatory || isOptional)) {
                throw new ProcessingException("Primitives should not use NonNull or Nullable annotations", fieldInfo.getElement());
            }

            boolean isRequired = isMandatory;

            switch (gsonFieldValidationType) {
                case VALIDATE_ALL_EXCEPT_NULLABLE:
                    // Using this policy everything is mandatory except for optionals.
                    isRequired = true;
                    break;

                case VALIDATE_EXPLICIT_NON_NULL:
                    // Primitives are treated as non-null implicitly.
                    if (isPrimitive) {
                        isRequired = true;
                    }
                    break;
            }

            // Optionals will never fail regardless of the policy.
            if (isOptional || gsonFieldValidationType == GsonFieldValidationType.NO_VALIDATION) {
                isRequired = false;
            }

            if (jsonFieldPath.contains(String.valueOf(flattenDelimiter))) {
                //
                // When the last character is a delimiter, we should append the variable name to
                // the end of the field name, as this may reduce annotation repetition.
                //
                if (jsonFieldPath.charAt(jsonFieldPath.length() - 1) == flattenDelimiter) {
                    jsonFieldPath += fieldName;
                }

                // Ensure that the delimiter is correctly escaped before attempting to split the string.
                String[] split = jsonFieldPath.split(regexSafeDelimiter);
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
                                throwDuplicateFieldException(fieldInfo.getElement(), currentKey);
                            }
                        }

                    } else {
                        // We have reached the end of this branch, add the field at the end.
                        if (!currentFieldTree.containsKey(currentKey)) {
                            currentFieldTree.addField(currentKey, new FieldPathInfo(fieldInfo, jsonFieldPath, isRequired));

                        } else {
                            throwDuplicateFieldException(fieldInfo.getElement(), currentKey);
                        }
                    }
                }

            } else {
                if (!gsonPathFieldTree.containsKey(jsonFieldPath)) {
                    gsonPathFieldTree.addField(jsonFieldPath, new FieldPathInfo(fieldInfo, jsonFieldPath, isRequired));

                } else {
                    throwDuplicateFieldException(fieldInfo.getElement(), jsonFieldPath);
                }
            }

        }
        return absoluteRootFieldTree;
    }

    private void throwDuplicateFieldException(Element field, String jsonKey) throws ProcessingException {
        throw new ProcessingException("Unexpected duplicate field '" + jsonKey +
                "' found. Each tree branch must use a unique value!", field);
    }

    /**
     * Add any mandatory field indexes as constants. This is done for code readability.
     * We will obtain the values using a depth-first recursion.
     */
    private void createMandatoryFieldConstants(Map<String, MandatoryFieldInfo> mandatoryInfoMap,
                                               TypeSpec.Builder typeBuilder,
                                               GsonFieldTree gsonFieldTree) {

        for (String branchKey : gsonFieldTree.keySet()) {
            Object treeObject = gsonFieldTree.get(branchKey);

            if (treeObject instanceof FieldPathInfo) {
                FieldPathInfo info = (FieldPathInfo) treeObject;

                //
                // For all required fields we add an index field so we can easily check whether the
                // value has been assigned after the json has been parsed.
                //
                if (info.isRequired) {
                    int mandatoryFieldSize = mandatoryInfoMap.size();
                    String fieldName = info.fieldInfo.getFieldName();

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
                createMandatoryFieldConstants(mandatoryInfoMap, typeBuilder, (GsonFieldTree) treeObject);
            }
        }
    }

    /**
     * public ImageSizes read(JsonReader in) throws IOException {
     */
    private MethodSpec createReadMethod(final ClassName baseElement,
                                        final ClassName concreteElement,
                                        final Map<String, MandatoryFieldInfo> mandatoryInfoMap,
                                        final GsonFieldTree rootElements) throws ProcessingException {

        // Create a flat list of the variables
        final List<FieldPathInfo> flattenedFields = new ArrayList<>();
        getFlattenedFields(rootElements, flattenedFields);

        MethodSpec.Builder readMethod = MethodSpec.methodBuilder("read")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(baseElement)
                .addParameter(JsonReader.class, "in")
                .addException(IOException.class);

        final CodeBlock.Builder codeBlock = CodeBlock.builder();

        mCounterVariableCount = 0;

        final boolean modelAlreadyCreated = baseElement.equals(concreteElement);
        createObjectParser(modelAlreadyCreated, 0, codeBlock, rootElements, new ObjectParserCallback() {
            @Override
            public void onInitialObjectNull() {
                codeBlock.addStatement("return null");
            }

            @Override
            public void onInitialise() {
                if (modelAlreadyCreated) {
                    codeBlock.addStatement("$T result = new $T()", concreteElement, concreteElement);

                } else {
                    for (FieldPathInfo fieldPathInfo : flattenedFields) {
                        FieldInfo fieldInfo = fieldPathInfo.fieldInfo;

                        // Don't initialise primitives, we rely on validation to throw an exception if the value does not exist.
                        TypeName typeName = fieldInfo.getTypeName();
                        String variableDeclaration = String.format("%s %s", typeName, fieldPathInfo.getVariableName());

                        String defaultValue = "null";
                        if (typeName.isPrimitive()) {
                            if (typeName == TypeName.INT || typeName == TypeName.BYTE || typeName == TypeName.SHORT) {
                                defaultValue = "0";

                            } else if (typeName == TypeName.LONG) {
                                defaultValue = "0L";

                            } else if (typeName == TypeName.FLOAT) {
                                defaultValue = "0f";

                            } else if (typeName == TypeName.DOUBLE) {
                                defaultValue = "0d";

                            } else if (typeName == TypeName.CHAR) {
                                defaultValue = "'\\0'";

                            } else if (typeName == TypeName.BOOLEAN) {
                                defaultValue = "false";
                            }
                        }

                        codeBlock.addStatement(variableDeclaration + " = " + defaultValue,
                                typeName,
                                fieldPathInfo.getVariableName());
                    }
                }

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
            codeBlock.beginControlFlow("for (int mandatoryFieldIndex = 0; " +
                    "mandatoryFieldIndex < MANDATORY_FIELDS_SIZE; mandatoryFieldIndex++)");

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
                codeBlock.addStatement("fieldName = \"$L\"", mandatoryFieldInfo.fieldPathInfo.jsonPath);
                codeBlock.addStatement("break");
                codeBlock.unindent();
                codeBlock.add("\n");
            }

            codeBlock.endControlFlow(); // Switch
            codeBlock.addStatement("throw new gsonpath.JsonFieldMissingException(\"Mandatory JSON " +
                    "element '\" + fieldName + \"' was not found for class '$L'\")", concreteElement);
            codeBlock.endControlFlow(); // If
            codeBlock.endControlFlow(); // For
        }

        if (modelAlreadyCreated) {
            codeBlock.addStatement("return result");

        } else {
            CodeBlock.Builder returnCodeBlock = CodeBlock.builder();
            returnCodeBlock.add("return new $T(\n", concreteElement);
            returnCodeBlock.indent();

            for (int i = 0; i < flattenedFields.size(); i++) {
                returnCodeBlock.add(flattenedFields.get(i).getVariableName());

                if (i < flattenedFields.size() - 1) {
                    returnCodeBlock.add(",");
                }

                returnCodeBlock.add("\n");
            }

            returnCodeBlock.unindent();
            returnCodeBlock.add(");\n");
            codeBlock.add(returnCodeBlock.build());

        }
        readMethod.addCode(codeBlock.build());

        return readMethod.build();
    }

    private void getFlattenedFields(GsonFieldTree currentTree, List<FieldPathInfo> flattenedFields) {
        for (String key : currentTree.keySet()) {
            Object value = currentTree.get(key);
            if (value instanceof FieldPathInfo) {
                flattenedFields.add((FieldPathInfo) value);

            } else {
                GsonFieldTree nextLevelMap = (GsonFieldTree) value;
                if (nextLevelMap.size() > 0) {
                    getFlattenedFields(nextLevelMap, flattenedFields);
                }
            }
        }
    }

    private MethodSpec createEmptyWriteMethod(ClassName elementClassName) throws ProcessingException {
        MethodSpec.Builder writeMethod = MethodSpec.methodBuilder("write")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(JsonWriter.class, "out")
                .addParameter(elementClassName, "value")
                .addException(IOException.class);

        return writeMethod.build();
    }

    /**
     * public void write(JsonWriter out, ImageSizes value) throws IOException {
     */
    private MethodSpec createWriteMethod(ClassName elementClassName,
                                         GsonFieldTree rootElements,
                                         boolean serializeNulls) throws ProcessingException {

        MethodSpec.Builder writeMethod = MethodSpec.methodBuilder("write")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(JsonWriter.class, "out")
                .addParameter(elementClassName, "value")
                .addException(IOException.class);

        CodeBlock.Builder codeBlock = CodeBlock.builder();

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

    private void writeObject(int fieldDepth,
                             CodeBlock.Builder codeBlock,
                             GsonFieldTree jsonMapping,
                             String currentPath,
                             boolean serializeNulls) throws ProcessingException {

        codeBlock.addStatement("out.beginObject()");

        for (String key : jsonMapping.keySet()) {
            Object value = jsonMapping.get(key);
            if (value instanceof FieldPathInfo) {
                FieldPathInfo fieldPathInfo = (FieldPathInfo) value;
                FieldInfo fieldInfo = fieldPathInfo.fieldInfo;

                // Make sure the field's annotations don't have any problems.
                validateFieldAnnotations(fieldInfo);

                TypeName fieldTypeName = fieldInfo.getTypeName();
                boolean isPrimitive = fieldTypeName.isPrimitive();

                String objectName = "obj" + mSafeVariableCount;
                mSafeVariableCount++;

                codeBlock.addStatement("$T $L = value.$L", fieldTypeName, objectName, fieldInfo.getFieldName());

                // If we aren't serializing nulls, we need to prevent the 'out.name' code being executed.
                if (!isPrimitive && !serializeNulls) {
                    codeBlock.beginControlFlow("if ($L != null)", objectName);
                }
                codeBlock.addStatement("out.name(\"$L\")", key);

                // Since we are serializing nulls, we defer the if-statement until after the name is written.
                if (!isPrimitive && serializeNulls) {
                    codeBlock.beginControlFlow("if ($L != null)", objectName);
                }

                if (isPrimitive || GSON_SUPPORTED_CLASSES.contains(fieldTypeName)) {

                    codeBlock.addStatement("out.value($L)", objectName);

                } else {
                    String adapterName;

                    if (fieldTypeName instanceof ParameterizedTypeName) {
                        // This is a generic type
                        adapterName = String.format("new com.google.gson.reflect.TypeToken<%s>(){}", fieldTypeName);

                    } else {
                        adapterName = fieldTypeName + ".class";
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
    protected void validateFieldAnnotations(FieldInfo fieldInfo) throws ProcessingException {
        // For now, we only ensure that the flatten annotation is only added to a String.
        if (fieldInfo.getAnnotation(FlattenJson.class) == null) {
            return;
        }

        if (!fieldInfo.getTypeName().equals(CLASS_NAME_STRING)) {
            throw new ProcessingException("FlattenObject can only be used on String variables", fieldInfo.getElement());
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
    @SuppressWarnings("unchecked")
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
            throw new ProcessingException("Error while creating 'fake' field for naming policy.");
        }

        // Applies the naming transformation on the input field name.
        return fieldNamingPolicy.translateName(fakeField);
    }

    private FieldNamingPolicy getGsonFieldNamingPolicy(GsonPathFieldNamingPolicy gsonPathFieldNamingPolicy) {
        switch (gsonPathFieldNamingPolicy) {
            case IDENTITY:
            case IDENTITY_OR_INHERIT_DEFAULT_IF_AVAILABLE:
                return FieldNamingPolicy.IDENTITY;

            case LOWER_CASE_WITH_DASHES:
                return FieldNamingPolicy.LOWER_CASE_WITH_DASHES;

            case LOWER_CASE_WITH_UNDERSCORES:
                return FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

            case UPPER_CAMEL_CASE:
                return FieldNamingPolicy.UPPER_CAMEL_CASE;

            case UPPER_CAMEL_CASE_WITH_SPACES:
                return FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES;
        }
        return null;
    }

    /**
     * Keeps track of mandatory json field metadata.
     */
    private static class MandatoryFieldInfo {
        final String indexVariableName;
        final FieldPathInfo fieldPathInfo;

        private MandatoryFieldInfo(String indexVariableName, FieldPathInfo fieldPathInfo) {
            this.indexVariableName = indexVariableName;
            this.fieldPathInfo = fieldPathInfo;
        }
    }

}
