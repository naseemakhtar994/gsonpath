package gsonpath;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by Lachlan on 1/03/2016.
 */
@AutoService(Processor.class)
public class GsonProcessor extends AbstractProcessor {
    private static final ClassName IO_EXCEPTION_TYPE = ClassName.get(IOException.class);

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(GsonPathClass.class)) {
            System.out.println("Handling element: " + element.getSimpleName());
            jsonPathClassHandler(element);
        }
        return true;
    }

    private void jsonPathClassHandler(Element element) {
        String packagePath = getElementPackage(element);
        ClassName jsonPathType = getElementClassName(element);
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get("com.google.gson", "TypeAdapter"), jsonPathType);

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder("Test_Adapter")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(parameterizedTypeName);
        //
        //@Override
        //public ImageSizes read(JsonReader in) throws IOException {
        //
        MethodSpec.Builder readMethod = MethodSpec.methodBuilder("read")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(jsonPathType)
                .addParameter(ClassName.get("com.google.gson.stream", "JsonReader"), "in")
                .addException(IO_EXCEPTION_TYPE);

        CodeBlock codeBlock = CodeBlock.builder()
                .addStatement("$T result = new $T()", jsonPathType, jsonPathType)
                .addStatement("return result")
                .build();

        readMethod.addCode(codeBlock);

        typeBuilder.addMethod(readMethod.build());

        //
        //@Override
        //public void write(JsonWriter out, ImageSizes value) throws IOException {
        //
        MethodSpec.Builder writeMethod = MethodSpec.methodBuilder("write")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("com.google.gson.stream", "JsonWriter"), "out")
                .addParameter(jsonPathType, "value")
                .addException(IO_EXCEPTION_TYPE)
                .addStatement("// GsonPath does not support writing at this stage.");

        typeBuilder.addMethod(writeMethod.build());

        TypeSpec typeSpec = typeBuilder.build();

        try {
            JavaFile.builder(packagePath, typeSpec)
                    .build().writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            System.out.println("ERROR! " + e.getMessage());
        }
    }

    private String getElementPackage(Element element) {
        return element.getEnclosingElement().asType().toString();
    }

    private ClassName getElementClassName(Element element) {
        return ClassName.get(getElementPackage(element), element.getSimpleName().toString());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedTypes = new LinkedHashSet<>();
        supportedTypes.add(GsonPathClass.class.getCanonicalName());
        supportedTypes.add(GsonPathElement.class.getCanonicalName());
        return supportedTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}