package gsonpath.generator;

import com.google.gson.JsonElement;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import gsonpath.FlattenJson;
import gsonpath.ProcessingException;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.*;
import java.util.regex.Pattern;

public abstract class BaseAdapterGenerator extends Generator {
    private static final Set<TypeName> GSON_SUPPORTED_PRIMITIVE = new HashSet<>(Arrays.asList(
            TypeName.BOOLEAN,
            TypeName.INT,
            TypeName.LONG,
            TypeName.DOUBLE
    ));

    protected static final Set<TypeName> GSON_SUPPORTED_CLASSES = new HashSet<>(Arrays.asList(
            TypeName.get(Boolean.class),
            TypeName.get(Integer.class),
            TypeName.get(Long.class),
            TypeName.get(Double.class),
            TypeName.get(String.class)
    ));

    protected static final ClassName CLASS_NAME_STRING = ClassName.get(String.class);
    private static final ClassName CLASS_NAME_JSON_ELEMENT = ClassName.get(JsonElement.class);

    // Used to avoid naming conflicts.
    protected int mCounterVariableCount;

    protected BaseAdapterGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    protected GsonFieldTree createGsonTreeFromRootField(GsonFieldTree rootFieldTree, String rootField, char delimiter) {
        if (rootField.length() == 0) {
            return rootFieldTree;
        }

        // Ensure that the delimiter is correctly escaped before attempting to split the string.
        String regexSafeDelimiter = Pattern.quote(String.valueOf(delimiter));
        String[] split = rootField.split(regexSafeDelimiter);

        if (split.length > 0) {
            // Keep adding branches to the tree and switching our root to the new branch.
            for (String field : split) {
                GsonFieldTree currentTree = new GsonFieldTree();
                rootFieldTree.addTreeBranch(field, currentTree);
                rootFieldTree = currentTree;
            }

            return rootFieldTree;

        } else {
            // Add a single branch to the tree and return the new branch.
            GsonFieldTree mapWithRoot = new GsonFieldTree();
            rootFieldTree.addTreeBranch(rootField, mapWithRoot);
            return mapWithRoot;
        }
    }

    protected void createObjectParser(int fieldDepth,
                                      CodeBlock.Builder codeBlock,
                                      GsonFieldTree jsonMapping,
                                      ObjectParserCallback callback) throws ProcessingException {

        String counterVariableName = "jsonFieldCounter" + mCounterVariableCount;
        mCounterVariableCount++;

        //
        // Ensure a Json object exists begin attempting to read it.
        //
        codeBlock.add("\n");
        codeBlock.add("// Ensure the object is not null.\n");
        codeBlock.beginControlFlow("if (!isValidValue(in))");

        if (fieldDepth == 0) {
            // Allow the calling method to inject different logic. Typically this would be to return.
            callback.onInitialObjectNull();

        } else {
            codeBlock.addStatement("break");
        }

        codeBlock.endControlFlow(); // if

        // This is the first block of code to fire after the object is valid.
        if (fieldDepth == 0) {
            callback.onInitialise();
            codeBlock.add("\n");
        }

        if (jsonMapping.size() == 0) {
            return;
        }

        codeBlock.addStatement("int $L = 0", counterVariableName);
        codeBlock.addStatement("in.beginObject()");
        codeBlock.add("\n");
        codeBlock.beginControlFlow("while (in.hasNext())");

        //
        // Since all the required fields have been mapped, we can avoid calling 'nextName'.
        // This ends up yielding performance improvements on large datasets depending on
        // the ordering of the fields within the JSON.
        //
        codeBlock.beginControlFlow("if ($L == $L)", counterVariableName, jsonMapping.size());
        codeBlock.addStatement("in.skipValue()");
        codeBlock.addStatement("continue");
        codeBlock.endControlFlow(); // if
        codeBlock.add("\n");

        codeBlock.beginControlFlow("switch (in.nextName())");

        boolean addBreak = true;
        for (String key : jsonMapping.keySet()) {
            codeBlock.add("case \"$L\":\n", key);
            codeBlock.indent();

            // Increment the counter to ensure we track how many fields we have mapped.
            codeBlock.addStatement("$L++", counterVariableName);

            Object value = jsonMapping.get(key);
            if (value instanceof FieldPathInfo) {
                FieldPathInfo fieldPathInfo = (FieldPathInfo) value;
                FieldInfo fieldInfo = fieldPathInfo.fieldInfo;

                // Make sure the field's annotations don't have any problems.
                validateFieldAnnotations(fieldInfo);

                TypeName fieldTypeName = fieldInfo.getTypeName();

                // Add a new line to improve readability for the multi-lined mapping.
                codeBlock.add("\n");

                String safeVariableName = fieldPathInfo.getSafeVariableName();

                boolean callToString = false;

                // If the field type is primitive, ensure that it is a supported primitive.
                if (fieldTypeName.isPrimitive() && !GSON_SUPPORTED_PRIMITIVE.contains(fieldTypeName)) {
                    throw new ProcessingException("Unsupported primitive type found. Only boolean, int, double and long can be used.", fieldInfo.getElement());
                }

                if (GSON_SUPPORTED_CLASSES.contains(fieldTypeName.box())) {
                    ClassName fieldClassName = (ClassName) fieldTypeName.box();

                    // Special handling for strings.
                    boolean handled = false;
                    if (fieldTypeName.equals(CLASS_NAME_STRING)) {
                        FlattenJson annotation = fieldInfo.getAnnotation(FlattenJson.class);
                        if (annotation != null) {
                            handled = true;
                            codeBlock.addStatement("$T $L = mGson.getAdapter($T.class).read(in)",
                                    CLASS_NAME_JSON_ELEMENT,
                                    safeVariableName,
                                    CLASS_NAME_JSON_ELEMENT);

                            callToString = true;
                        }
                    }

                    if (!handled) {
                        codeBlock.addStatement("$L $L = get$LSafely(in)",
                                fieldClassName.simpleName(),
                                safeVariableName,
                                fieldClassName.simpleName());
                    }
                } else {
                    String adapterName;

                    if (fieldTypeName instanceof ParameterizedTypeName) {
                        // This is a generic type
                        adapterName = String.format("new com.google.gson.reflect.TypeToken<%s>(){}", fieldTypeName);

                    } else {
                        adapterName = fieldTypeName + ".class";
                    }

                    // Handle every other possible class by falling back onto the gson adapter.
                    codeBlock.addStatement("$L $L = mGson.getAdapter($L).read(in)",
                            fieldTypeName, safeVariableName, adapterName);
                }

                codeBlock.beginControlFlow("if ($L != null)", safeVariableName);
                codeBlock.addStatement("result.$L = $L$L",
                        fieldInfo.getFieldName(),
                        safeVariableName,
                        callToString ? ".toString()" : "");

                // Inform the callback in case it wishes to add any further code.
                callback.onFieldAssigned(fieldInfo.getFieldName());

                if (fieldPathInfo.isRequired) {
                    codeBlock.nextControlFlow("else");
                    codeBlock.addStatement("throw new gsonpath.JsonFieldMissingException(\"Mandatory " +
                                    "JSON element '$L' was null for class '$L'\")",
                            fieldPathInfo.jsonPath,
                            fieldInfo.getParentClassName());
                }

                codeBlock.endControlFlow(); // if

            } else {
                GsonFieldTree nextLevelMap = (GsonFieldTree) value;
                if (nextLevelMap.size() == 0) {
                    callback.onNodeEmpty();
                    addBreak = false;
                } else {
                    createObjectParser(fieldDepth + 1, codeBlock, nextLevelMap, callback);
                }
            }

            if (addBreak) {
                codeBlock.addStatement("break");
            }

            codeBlock.add("\n");
            codeBlock.unindent();
        }

        codeBlock.add("default:\n");
        codeBlock.indent();
        codeBlock.addStatement("in.skipValue()");
        codeBlock.addStatement("break");
        codeBlock.unindent();

        codeBlock.endControlFlow(); // switch
        codeBlock.endControlFlow(); // while
        codeBlock.add("\n");

        codeBlock.add("\n");

        codeBlock.addStatement("in.endObject()");
    }

    protected void validateFieldAnnotations(FieldInfo fieldInfo) throws ProcessingException {
        // Do nothing.
    }

    protected String generateClassName(ClassName className) {
        //
        // We need to ensure that nested classes are have include their parent class as part of the name.
        // Otherwise this could cause file name contention when other nested classes have the same name
        //
        String fileName = "";
        for (String name : className.simpleNames()) {
            fileName += name + "_";
        }

        // Make sure no '.' managed to sneak through!
        return fileName.replace(".", "_") + getClassNameSuffix();
    }

    protected abstract String getClassNameSuffix();

    public interface ObjectParserCallback {
        void onInitialObjectNull();

        void onInitialise();

        void onFieldAssigned(String fieldName);

        void onNodeEmpty();
    }
}
