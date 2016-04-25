package gsonpath.generator.streamer;

import com.squareup.javapoet.*;
import gsonpath.GsonArrayStreamer;
import gsonpath.generator.Generator;
import gsonpath.generator.HandleResult;
import gsonpath.internal.GsonArrayStreamerLoader;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.List;

public class StreamArrayLoaderGenerator extends Generator {

    public StreamArrayLoaderGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public boolean generate(List<HandleResult> generatedGsonArrayAdapters) {
        if (generatedGsonArrayAdapters.size() == 0) {
            return false;
        }

        // Create the GsonPathLoader which is used by the GsonPathTypeAdapterFactory class.
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder("GeneratedGsonArrayStreamerLoader")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(GsonArrayStreamerLoader.class);

        //
        // <T extends GsonArrayStreamer> T get(Class<T> type);
        //
        TypeVariableName typeVariableName = TypeVariableName.get("T", GsonArrayStreamer.class);
        MethodSpec.Builder getMethod = MethodSpec.methodBuilder("get")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(typeVariableName)
                .returns(typeVariableName)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), typeVariableName), "type");

        CodeBlock.Builder codeBlock = CodeBlock.builder();

        int currentAdapterIndex = 0;
        for (HandleResult result : generatedGsonArrayAdapters) {
            if (currentAdapterIndex == 0) {
                codeBlock.beginControlFlow("if (type.equals($L.class))", result.originalClassName.toString());
            } else {
                codeBlock.add("\n"); // New line for easier readability.
                codeBlock.nextControlFlow("else if (type.equals($L.class))", result.originalClassName.toString());
            }
            codeBlock.addStatement("return (T) new $L()", result.generatedClassName.toString());

            currentAdapterIndex++;
        }
        codeBlock.endControlFlow();
        codeBlock.add("\n");
        codeBlock.addStatement("return null");

        getMethod.addCode(codeBlock.build());
        typeBuilder.addMethod(getMethod.build());

        return writeFile("gsonpath", typeBuilder);
    }

}
