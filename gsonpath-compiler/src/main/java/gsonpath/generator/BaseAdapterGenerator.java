package gsonpath.generator;

import com.squareup.javapoet.CodeBlock;
import gsonpath.FlattenJson;
import gsonpath.ProcessingException;
import gsonpath.ProcessorUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.*;

/**
 * Created by Lachlan on 12/03/2016.
 */
public abstract class BaseAdapterGenerator extends Generator {
    private static final String ADAPTER_SUFFIX = "_GsonTypeAdapter";

    static final String GSON_PACKAGE = "com.google.gson";
    static final String STRING_CLASS_PATH = "java.lang.String";

    static final Set<String> HANDLED_PRIMITIVES = new HashSet<>(Arrays.asList(
            "boolean", "int", "long", "double"
    ));

    static final Set<String> HANDLED_BOXED_PRIMITIVES = new HashSet<>(Arrays.asList(
            "java.lang.Boolean", "java.lang.Integer", "java.lang.Long", "java.lang.Double"
    ));

    // Used to avoid naming conflicts.
    int mCounterVariableCount;
    int mSafeVariableCount;

    public BaseAdapterGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    Map<String, Object> getElementsFromRoot(Map<String, Object> rootElements, String rootField) {
        if (rootField.length() > 0) {
            String[] split = rootField.split("\\.");

            if (split.length > 0) {
                for (String field : split) {
                    Map<String, Object> mapWithRoot = new LinkedHashMap<>();
                    rootElements.put(field, mapWithRoot);
                    rootElements = mapWithRoot;
                }

                return rootElements;

            } else {
                Map<String, Object> mapWithRoot = new LinkedHashMap<>();
                rootElements.put(rootField, mapWithRoot);
                return mapWithRoot;
            }
        }

        return rootElements;
    }

    public interface ObjectParserCallback {
        void onNodeEmpty();
    }

    void createObjectParser(int fieldDepth, CodeBlock.Builder codeBlock, Map<String, Object> jsonMapping) throws ProcessingException {
        createObjectParser(fieldDepth, codeBlock, jsonMapping, null);
    }

    void createObjectParser(int fieldDepth, CodeBlock.Builder codeBlock, Map<String, Object> jsonMapping, ObjectParserCallback callback) throws ProcessingException {
        String counterVariableName = "jsonFieldCounter" + mCounterVariableCount;
        mCounterVariableCount++;

        //
        // Ensure a Json object exists begin attempting to read it.
        // Since we are within a switch statement, we need to break out.
        //
        if (fieldDepth > 0) {
            codeBlock.add("\n");
            codeBlock.add("// Ensure the object is not null.\n");
            codeBlock.beginControlFlow("if (!isValidValue(in))");
            codeBlock.addStatement("break");
            codeBlock.endControlFlow();
            codeBlock.add("\n");
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
        codeBlock.endControlFlow();
        codeBlock.add("\n");

        codeBlock.beginControlFlow("switch (in.nextName())");

        boolean addBreak = true;
        for (String key : jsonMapping.keySet()) {
            codeBlock.add("case \"$L\":\n", key);
            codeBlock.indent();

            // Increment the counter to ensure we track how many fields we have mapped.
            codeBlock.addStatement("$L++", counterVariableName);

            Object value = jsonMapping.get(key);
            if (value instanceof Element) {
                Element field = (Element) value;

                // Make sure the field's annotations don't have any problems.
                validateFieldAnnotations(field);

                String gsonMethodType = ProcessorUtil.getElementType(field);
                if (HANDLED_PRIMITIVES.contains(gsonMethodType)) {

                    // Handle primitives.
                    gsonMethodType = Character.toUpperCase(gsonMethodType.charAt(0)) + gsonMethodType.substring(1);
                    codeBlock.addStatement("result.$L = in.next$L()", field.getSimpleName().toString(), gsonMethodType);

                } else {
                    // Add a new line to improve readability for the multi-lined mapping.
                    codeBlock.add("\n");

                    boolean isStringType = gsonMethodType.equals(STRING_CLASS_PATH);
                    boolean callToString = false;

                    if (isStringType || HANDLED_BOXED_PRIMITIVES.contains(gsonMethodType)) {

                        gsonMethodType = gsonMethodType.replace("java.lang.", "");

                        // Special handling for strings.
                        boolean handled = false;
                        if (isStringType) {
                            FlattenJson annotation = field.getAnnotation(FlattenJson.class);
                            if (annotation != null) {
                                handled = true;
                                codeBlock.addStatement("com.google.gson.JsonElement safeValue$L = mGson.getAdapter(com.google.gson.JsonElement.class).read(in)", mSafeVariableCount);

                                callToString = true;
                            }
                        }

                        if (!handled) {
                            codeBlock.addStatement("$L safeValue$L = get$LSafely(in)", gsonMethodType, mSafeVariableCount, gsonMethodType);
                        }
                    } else {
                        String adapterName;

                        // TODO: Casting field to 'TypeElement' throws a cast exception, so we need to detect generics in a hacky way at the moment.
                        boolean isGenericField = gsonMethodType.contains("<");
                        if (isGenericField) {
                            // This is a generic type
                            adapterName = String.format("new com.google.gson.reflect.TypeToken<%s>(){}", gsonMethodType);

                        } else {
                            adapterName = gsonMethodType + ".class";
                        }

                        // Handle every other possible class by falling back onto the gson adapter.
                        codeBlock.addStatement("$L safeValue$L = mGson.getAdapter($L).read(in)", gsonMethodType, mSafeVariableCount, adapterName);
                    }

                    codeBlock.beginControlFlow("if (safeValue$L != null)", mSafeVariableCount);
                    codeBlock.addStatement("result.$L = safeValue$L$L", field.getSimpleName().toString(), mSafeVariableCount, callToString ? ".toString()" : "");
                    codeBlock.endControlFlow();

                    mSafeVariableCount++;
                }

            } else {
                Map<String, Object> nextLevelMap = (Map<String, Object>) value;
                if (nextLevelMap.size() == 0) {
                    if (callback != null) {
                        callback.onNodeEmpty();
                        addBreak = false;
                    }
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

        codeBlock.endControlFlow();
        codeBlock.endControlFlow();
        codeBlock.add("\n");

        codeBlock.add("\n");

        codeBlock.addStatement("in.endObject()");
    }

    void validateFieldAnnotations(Element field) throws ProcessingException {
        // Do nothing.
    }

    abstract String getClassNameSuffix();

}
