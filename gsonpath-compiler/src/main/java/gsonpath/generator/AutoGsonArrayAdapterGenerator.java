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
import java.util.List;

/**
 * Created by Lachlan on 12/03/2016.
 */
public class AutoGsonArrayAdapterGenerator extends BaseAdapterGenerator {

    public AutoGsonArrayAdapterGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public void handle(TypeElement element) throws ProcessingException {
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
            return;
        }

        // Get the actual argument used for json parsing from the interface generics.
        List<TypeMirror> typeGenericArguments = ProcessorUtil.getTypeGenericArguments(arrayTypeAdapterElement);
        TypeMirror typeMirror = typeGenericArguments.get(0);

        ClassName elementClassName = ProcessorUtil.getElementJavaPoetClassName(processingEnv.getTypeUtils().asElement(typeMirror));

        String adapterClassName = element.getSimpleName() + getClassNameSuffix();
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(adapterClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(AbstractArrayTypeAdapter.class), elementClassName))
                .addSuperinterface(ProcessorUtil.getElementJavaPoetClassName(element));

        // getArray
        MethodSpec.Builder getArrayJsonReader = createBasicBuilder("getArray", ArrayTypeName.of(elementClassName));
        getArrayJsonReader.addStatement("return gson.fromJson(jsonReader, $T[].class)", elementClassName);
        typeBuilder.addMethod(getArrayJsonReader.build());

        // getList
        MethodSpec.Builder getListJsonReader = createBasicBuilder("getList", ParameterizedTypeName.get(ClassName.get(List.class), elementClassName));
        getListJsonReader.addStatement("return gson.fromJson(jsonReader, new com.google.gson.reflect.TypeToken<List<$T>>() {}.getType())", elementClassName);
        typeBuilder.addMethod(getListJsonReader.build());

        // Stream results, one at a time - json reader.
        MethodSpec.Builder streamOneJsonReader = createBasicBuilder("streamArray", null);

        streamOneJsonReader.addParameter(ParameterizedTypeName.get(ClassName.get(StreamCallback.class), elementClassName), "callback", Modifier.FINAL);

        streamOneJsonReader.beginControlFlow("streamArraySegmented(gson, jsonReader, 1, new StreamCallback<$T[]>()", elementClassName);
        streamOneJsonReader.addCode("@Override\n");
        streamOneJsonReader.beginControlFlow("public void onValueParsed($T[] value, int length, StreamHandler response)", elementClassName);
        streamOneJsonReader.addStatement("callback.onValueParsed(value[0], length, response)");
        streamOneJsonReader.endControlFlow();
        streamOneJsonReader.endControlFlow(")");

        typeBuilder.addMethod(streamOneJsonReader.build());

        // Stream results, multiple - json reader.
        MethodSpec.Builder streamMultipleJsonReader = createBasicBuilder("streamArraySegmented", null);
        streamMultipleJsonReader.addParameter(TypeName.INT, "streamSize");
        streamMultipleJsonReader.addParameter(ParameterizedTypeName.get(ClassName.get(StreamCallback.class), ArrayTypeName.of(elementClassName)), "callback");


        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.addStatement("$T[] results = new $T[streamSize]", elementClassName, elementClassName);
        codeBlock.addStatement("StreamCallback.StreamHandler callbackResponse = new StreamCallback.StreamHandler()");
        codeBlock.addStatement("int resultIndex = -1");
        codeBlock.add("\n");
        codeBlock.beginControlFlow("try");
        codeBlock.addStatement("jsonReader.beginArray()");
        codeBlock.beginControlFlow("while (jsonReader.hasNext())");
        codeBlock.addStatement("results[++resultIndex] = gson.fromJson(jsonReader, $T.class)", elementClassName);
        codeBlock.add("\n");
        codeBlock.add("// Once we reach the requested stream size, we should return the results.\n");
        codeBlock.beginControlFlow("if (resultIndex == streamSize - 1)");
        codeBlock.addStatement("callback.onValueParsed(results, streamSize, callbackResponse)");
        codeBlock.add("\n");
        codeBlock.beginControlFlow("if (callbackResponse.isStreamStopped())");
        codeBlock.add("// Since we stop the stream mid-way, we cannot call end-array safely.\n");
        codeBlock.addStatement("return");
        codeBlock.endControlFlow();
        codeBlock.add("\n");
        codeBlock.addStatement("resultIndex = -1");
        codeBlock.endControlFlow();
        codeBlock.endControlFlow();
        codeBlock.addStatement("jsonReader.endArray()");
        codeBlock.add("\n");
        codeBlock.nextControlFlow("catch ($T e)", ClassName.get(IOException.class));
        codeBlock.addStatement("throw new $T(e)", ClassName.get(JsonSyntaxException.class));
        codeBlock.endControlFlow();
        codeBlock.add("\n");
        codeBlock.add("// We have left over results to send back.\n");
        codeBlock.beginControlFlow("if (resultIndex >= 0)");
        codeBlock.add("// To avoid creating a new array, we will simply remove the invalid results at the end.\n");
        codeBlock.beginControlFlow("for (int i = resultIndex + 1; i < streamSize; i++)");
        codeBlock.addStatement("results[i] = null");
        codeBlock.endControlFlow();
        codeBlock.add("\n");
        codeBlock.addStatement("callback.onValueParsed(results, resultIndex + 1, callbackResponse)");
        codeBlock.endControlFlow();

        streamMultipleJsonReader.addCode(codeBlock.build());
        typeBuilder.addMethod(streamMultipleJsonReader.build());

        if (!writeFile(ProcessorUtil.getElementPackage(element), typeBuilder)) {
            throw new ProcessingException();
        }
    }

    private MethodSpec.Builder createBasicBuilder(String name, TypeName returnTypeName) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(name)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson")
                .addParameter(JsonReader.class, "jsonReader")
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
        return "_ArrayTypeAdapter";
    }

}
