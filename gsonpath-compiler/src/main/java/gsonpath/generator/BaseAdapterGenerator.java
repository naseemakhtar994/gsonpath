package gsonpath.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import gsonpath.FlattenJson;
import gsonpath.ProcessingException;
import gsonpath.ProcessorUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.regex.Pattern;

public abstract class BaseAdapterGenerator extends Generator {
    protected static final String GSON_PACKAGE = "com.google.gson";
    protected static final String STRING_CLASS_PATH = "java.lang.String";

    protected static final Set<String> HANDLED_PRIMITIVES = new HashSet<>(Arrays.asList(
            "boolean", "int", "long", "double"
    ));

    protected static final Set<String> HANDLED_BOXED_PRIMITIVES = new HashSet<>(Arrays.asList(
            "java.lang.Boolean", "java.lang.Integer", "java.lang.Long", "java.lang.Double"
    ));

    protected static final Map<String, String> PRIMITIVE_TO_WRAPPER_MAP = new HashMap<>();

    static {
        PRIMITIVE_TO_WRAPPER_MAP.put("boolean", "java.lang.Boolean");
        PRIMITIVE_TO_WRAPPER_MAP.put("int", "java.lang.Integer");
        PRIMITIVE_TO_WRAPPER_MAP.put("long", "java.lang.Long");
        PRIMITIVE_TO_WRAPPER_MAP.put("double", "java.lang.Double");
    }

    // Used to avoid naming conflicts.
    protected int mCounterVariableCount;
    protected int mSafeVariableCount;

    public BaseAdapterGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    protected Map<String, Object> getElementsFromRoot(Map<String, Object> rootElements, String rootField, char delimiter) {
        if (rootField.length() > 0) {
            // Ensure that the delimiter is correctly escaped before attempting to split the string.
            String regexSafeDelimiter = Pattern.quote(String.valueOf(delimiter));
            String[] split = rootField.split(regexSafeDelimiter);

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
        void onInitialObjectNull();

        void onInitialise();

        void onNodeEmpty();
    }

    protected void createObjectParser(int fieldDepth, CodeBlock.Builder codeBlock, Map<String, Object> jsonMapping, ObjectParserCallback callback) throws ProcessingException {
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
            if (value instanceof Element) {
                Element field = (Element) value;

                // Make sure the field's annotations don't have any problems.
                validateFieldAnnotations(field);

                String gsonMethodType = ProcessorUtil.getElementType(field);

                //
                // Handle the primitive the same way as their wrapper class.
                // This ensures null safety is handled.
                //
                if (HANDLED_PRIMITIVES.contains(gsonMethodType)) {
                    gsonMethodType = PRIMITIVE_TO_WRAPPER_MAP.get(gsonMethodType);
                }

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
                codeBlock.endControlFlow(); // if

                mSafeVariableCount++;

            } else {
                Map<String, Object> nextLevelMap = (Map<String, Object>) value;
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

    protected void validateFieldAnnotations(Element field) throws ProcessingException {
        // Do nothing.
    }

    protected String getClassName(TypeElement element) {
        ClassName elementClassName = ProcessorUtil.getElementJavaPoetClassName(element);

        //
        // We need to ensure that nested classes are have include their parent class as part of the name.
        // Otherwise this could cause file name contention when other nested classes have the same name
        //
        String fileName = "";
        for (String name : elementClassName.simpleNames()) {
            fileName += name + "_";
        }

        // Make sure no '.' managed to sneak through!
        return fileName.replace(".", "_") + getClassNameSuffix();
    }

    protected abstract String getClassNameSuffix();

}
