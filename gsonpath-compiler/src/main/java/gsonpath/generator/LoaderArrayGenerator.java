package gsonpath.generator;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.squareup.javapoet.*;
import gsonpath.ArrayTypeAdapter;
import gsonpath.internal.TypeAdapterArrayLoader;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * Created by Lachlan on 13/03/2016.
 */
public class LoaderArrayGenerator extends Generator {

    public LoaderArrayGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public boolean generate(List<HandleResult> generatedGsonArrayAdapters) {
        if (generatedGsonArrayAdapters.size() == 0) {
            return false;
        }

        // Create the GsonPathLoader which is used by the GsonPathTypeAdapterFactory class.
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder("GeneratedTypeAdapterArrayLoader")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(TypeAdapterArrayLoader.class);

        //
        // <T extends ArrayTypeAdapter> T get(Class<T> type);
        //
        TypeVariableName typeVariableName = TypeVariableName.get("T", ArrayTypeAdapter.class);
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
