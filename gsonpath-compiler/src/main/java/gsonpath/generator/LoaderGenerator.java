package gsonpath.generator;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import gsonpath.GsonPathLoader;

/**
 * Created by Lachlan on 13/03/2016.
 */
public class LoaderGenerator extends Generator {

    public LoaderGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public boolean generate(List<HandleResult> generatedGsonAdapters) {
        if (generatedGsonAdapters.size() == 0) {
            return false;
        }

        // Create the GsonPathLoader which is used by the GsonPathTypeAdapterFactory class.
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder("GeneratedGsonPathLoader")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(GsonPathLoader.class);

        //
        // <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type);
        //
        MethodSpec.Builder createMethod = MethodSpec.methodBuilder("create")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeAdapter.class)
                .addParameter(Gson.class, "gson")
                .addParameter(TypeToken.class, "type");

        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.addStatement("Class rawType = type.getRawType()");

        int currentAdapterIndex = 0;
        for (HandleResult result : generatedGsonAdapters) {
            if (currentAdapterIndex == 0) {
                codeBlock.beginControlFlow("if (rawType.equals($L.class))", result.originalClassName.toString());
            } else {
                codeBlock.add("\n"); // New line for easier readability.
                codeBlock.nextControlFlow("else if (rawType.equals($L.class))", result.originalClassName.toString());
            }
            codeBlock.addStatement("return new $L(gson)", result.generatedClassName.toString());

            currentAdapterIndex++;
        }
        codeBlock.endControlFlow();
        codeBlock.add("\n");
        codeBlock.addStatement("return null");

        createMethod.addCode(codeBlock.build());
        typeBuilder.addMethod(createMethod.build());

        return writeFile("gsonpath", typeBuilder);
    }

}
