package gsonpath.generator;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.squareup.javapoet.*;
import gsonpath.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lachlan on 12/03/2016.
 */
public class AutoGsonArrayAdapterGenerator extends BaseAdapterGenerator {

    public AutoGsonArrayAdapterGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public HandleResult handle(TypeElement element) throws ProcessingException {
        // The class must implement the ArrayTypeAdapter interface!

        TypeMirror arrayTypeAdapterElement = null;
        for (TypeMirror typeMirror : element.getInterfaces()) {
            TypeElement interfaceElement = (TypeElement) processingEnv.getTypeUtils().asElement(typeMirror);

            if (ProcessorUtil.getElementJavaPoetClassName(interfaceElement).equals(ClassName.get(ArrayTypeAdapter.class))) {
                arrayTypeAdapterElement = typeMirror;
            }
        }

        if (arrayTypeAdapterElement == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Class must extend " + ArrayTypeAdapter.class.getName());
            throw new ProcessingException();
        }

        // Get the actual argument used for json parsing from the interface generics.
        List<TypeMirror> typeGenericArguments = ProcessorUtil.getTypeGenericArguments(arrayTypeAdapterElement);
        TypeMirror typeMirror = typeGenericArguments.get(0);

        final ClassName elementClassName = ProcessorUtil.getElementJavaPoetClassName(processingEnv.getTypeUtils().asElement(typeMirror));
        ClassName originalAdapterInterface = ProcessorUtil.getElementJavaPoetClassName(element);

        String adapterClassName = getClassName(element);
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(adapterClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(AbstractArrayTypeAdapter.class), elementClassName))
                .addSuperinterface(originalAdapterInterface);

        AutoGsonArrayAdapter autoGsonArrayAnnotation = element.getAnnotation(AutoGsonArrayAdapter.class);
        String rootField = autoGsonArrayAnnotation.rootField();

        Map<String, Object> rootElements = new LinkedHashMap<>();
        if (rootField.length() > 0) {
            getElementsFromRoot(rootElements, rootField);
        }

        // getArray
        MethodSpec.Builder getArrayJsonReader = createBasicBuilder("getArray", ArrayTypeName.of(elementClassName));

        final CodeBlock.Builder getArrayBlock = CodeBlock.builder();

        if (rootElements.size() > 0) {
            addToSimpleCodeBlock(getArrayBlock, rootElements, new ObjectParserCallback() {
                @Override
                public void onNodeEmpty() {
                    addArrayCodeBlock(getArrayBlock, elementClassName);
                }
            });
            getArrayBlock.addStatement("return null");
        } else {
            addArrayCodeBlock(getArrayBlock, elementClassName);
        }
        getArrayJsonReader.addCode(getArrayBlock.build());
        typeBuilder.addMethod(getArrayJsonReader.build());

        // getList
        MethodSpec.Builder getListJsonReader = createBasicBuilder("getList", ParameterizedTypeName.get(ClassName.get(List.class), elementClassName));

        final CodeBlock.Builder getListBlock = CodeBlock.builder();

        if (rootElements.size() > 0) {
            addToSimpleCodeBlock(getListBlock, rootElements, new ObjectParserCallback() {
                @Override
                public void onNodeEmpty() {
                    addListCodeBlock(getListBlock, elementClassName);
                }
            });
            getListBlock.addStatement("return null");
        } else {
            addListCodeBlock(getListBlock, elementClassName);
        }
        getListJsonReader.addCode(getListBlock.build());
        typeBuilder.addMethod(getListJsonReader.build());

        // Stream results, multiple - json reader.
        MethodSpec.Builder streamMultipleJsonReader = createBasicBuilder("streamArraySegmented", null);
        streamMultipleJsonReader.addParameter(TypeName.INT, "streamSize");
        streamMultipleJsonReader.addParameter(ParameterizedTypeName.get(ClassName.get(StreamCallback.class), ArrayTypeName.of(elementClassName)), "callback");

        final CodeBlock.Builder streamCodeBlock = CodeBlock.builder();
        streamCodeBlock.addStatement("$T[] results = new $T[streamSize]", elementClassName, elementClassName);
        streamCodeBlock.addStatement("StreamCallback.StreamHandler callbackResponse = new StreamCallback.StreamHandler()");
        streamCodeBlock.addStatement("int resultIndex = -1");
        streamCodeBlock.add("\n");

        if (rootElements.size() > 0) {
            addToSimpleCodeBlock(streamCodeBlock, rootElements, new ObjectParserCallback() {
                @Override
                public void onNodeEmpty() {
                    addStreamCodeBlock(streamCodeBlock, elementClassName);
                }
            });
        } else {
            addStreamCodeBlock(streamCodeBlock, elementClassName);
        }

        streamCodeBlock.add("\n");
        streamCodeBlock.add("// We have left over results to send back.\n");
        streamCodeBlock.beginControlFlow("if (resultIndex >= 0)");
        streamCodeBlock.add("// To avoid creating a new array, we will simply remove the invalid results at the end.\n");
        streamCodeBlock.beginControlFlow("for (int i = resultIndex + 1; i < streamSize; i++)");
        streamCodeBlock.addStatement("results[i] = null");
        streamCodeBlock.endControlFlow();
        streamCodeBlock.add("\n");
        streamCodeBlock.addStatement("callback.onValueParsed(results, resultIndex + 1, callbackResponse)");
        streamCodeBlock.endControlFlow();

        streamMultipleJsonReader.addCode(streamCodeBlock.build());
        typeBuilder.addMethod(streamMultipleJsonReader.build());

        String elementPackagePath = ProcessorUtil.getElementPackage(element);
        if (writeFile(elementPackagePath, typeBuilder)) {
            return new HandleResult(originalAdapterInterface, ClassName.get(elementPackagePath, adapterClassName));
        }
        throw new ProcessingException();
    }

    private void addArrayCodeBlock(CodeBlock.Builder builder, ClassName elementClassName) {
        builder.addStatement("return gson.fromJson(in, $T[].class)", elementClassName);
    }

    private void addListCodeBlock(CodeBlock.Builder builder, ClassName elementClassName) {
        builder.addStatement("return gson.fromJson(in, new com.google.gson.reflect.TypeToken<List<$T>>() {}.getType())", elementClassName);
    }

    private void addStreamCodeBlock(CodeBlock.Builder builder, ClassName elementClassName) {
        builder.addStatement("in.beginArray()");
        builder.beginControlFlow("while (in.hasNext())");
        builder.addStatement("results[++resultIndex] = gson.fromJson(in, $T.class)", elementClassName);
        builder.add("\n");
        builder.add("// Once we reach the requested stream size, we should return the results.\n");
        builder.beginControlFlow("if (resultIndex == streamSize - 1)");
        builder.addStatement("callback.onValueParsed(results, streamSize, callbackResponse)");
        builder.add("\n");
        builder.beginControlFlow("if (callbackResponse.isStreamStopped())");
        builder.add("// Since we stop the stream mid-way, we cannot call end-array safely.\n");
        builder.addStatement("return");
        builder.endControlFlow();
        builder.add("\n");
        builder.addStatement("resultIndex = -1");
        builder.endControlFlow();
        builder.endControlFlow();
        builder.addStatement("in.endArray()");
    }

    private void addToSimpleCodeBlock(CodeBlock.Builder builder, Map<String, Object> rootElements, ObjectParserCallback callback) throws ProcessingException {
        builder.beginControlFlow("try");
        if (rootElements.size() > 0) {
            mCounterVariableCount = 0;
            mSafeVariableCount = 0;

            createObjectParser(0, builder, rootElements, callback);
        }
        builder.nextControlFlow("catch ($T e)", ClassName.get(IOException.class));
        builder.addStatement("throw new $T(e)", ClassName.get(JsonSyntaxException.class));
        builder.endControlFlow();
    }

    private MethodSpec.Builder createBasicBuilder(String name, TypeName returnTypeName) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(name)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson")
                .addParameter(JsonReader.class, "in")
                .addException(JsonSyntaxException.class);

        if (returnTypeName != null) {
            methodBuilder.returns(returnTypeName);
        }

        return methodBuilder;
    }

    @Override
    public void onJavaFileBuilt(JavaFile.Builder builder) {
        builder.addStaticImport(GsonUtil.class, "*");
    }

    @Override
    String getClassNameSuffix() {
        return "ArrayTypeAdapter";
    }

}