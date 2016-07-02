package gsonpath.generator.streamer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.squareup.javapoet.*;
import gsonpath.*;
import gsonpath.generator.BaseAdapterGenerator;
import gsonpath.generator.GsonFieldTree;
import gsonpath.generator.HandleResult;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.List;

public class GsonArrayStreamerGenerator extends BaseAdapterGenerator {

    public GsonArrayStreamerGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public HandleResult handle(TypeElement streamerElement) throws ProcessingException {
        // The class must implement the GsonArrayStreamer interface!
        ClassName streamerClassName = ClassName.get(streamerElement);
        ClassName outputClassName = ClassName.get(streamerClassName.packageName(), generateClassName(streamerClassName));

        TypeMirror gsonArrayStreamerElement = null;
        for (TypeMirror typeMirror : streamerElement.getInterfaces()) {
            TypeElement interfaceElement = (TypeElement) processingEnv.getTypeUtils().asElement(typeMirror);

            if (ClassName.get(interfaceElement).equals(ClassName.get(GsonArrayStreamer.class))) {
                gsonArrayStreamerElement = typeMirror;
            }
        }

        if (gsonArrayStreamerElement == null) {
            throw new ProcessingException("Class must extend " + GsonArrayStreamer.class.getName());
        }

        // Get the actual argument used for json parsing from the interface generics.
        ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) TypeName.get(gsonArrayStreamerElement);

        final TypeName gsonModelClassName = parameterizedTypeName.typeArguments.get(0);
        ClassName originalAdapterInterface = ClassName.get(streamerElement);

        TypeSpec.Builder streamerTypeBuilder = TypeSpec.classBuilder(outputClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(AbstractGsonArrayStreamer.class), gsonModelClassName))
                .addSuperinterface(originalAdapterInterface);

        AutoGsonArrayStreamer autoGsonArrayAnnotation = streamerElement.getAnnotation(AutoGsonArrayStreamer.class);
        String rootField = autoGsonArrayAnnotation.rootField();
        char flattenDelimiter = autoGsonArrayAnnotation.flattenDelimiter();

        boolean isRootFieldSpecified = rootField.length() > 0;

        // This flag is only valid if the rootField value is populated, since it only affects the behaviour of rootField.
        final boolean consumeReaderFully = autoGsonArrayAnnotation.consumeReaderFully() && isRootFieldSpecified;

        GsonFieldTree rootElements = new GsonFieldTree();
        if (isRootFieldSpecified) {
            createGsonTreeFromRootField(rootElements, rootField, flattenDelimiter);
        }

        // getArray
        ArrayTypeName elementArrayType = ArrayTypeName.of(gsonModelClassName);
        MethodSpec.Builder getArrayJsonReader = createBasicBuilder("getArray", elementArrayType);

        final CodeBlock.Builder getArrayBlock = CodeBlock.builder();

        // If we are reading the whole object, we need to a variable declared outside the try/catch
        if (consumeReaderFully) {
            getArrayBlock.addStatement("$T result = null", elementArrayType);
        }

        if (rootElements.size() > 0) {
            // The code must navigate to the correct root field.
            addToSimpleCodeBlock(getArrayBlock, rootElements, new ObjectParserCallback() {
                @Override
                public void onInitialObjectNull() {
                    getArrayBlock.addStatement("return null");
                }

                @Override
                public void onInitialise() {

                }

                @Override
                public void onFieldAssigned(String fieldName) {

                }

                @Override
                public void onNodeEmpty() {
                    if (consumeReaderFully) {
                        // Since we read the json entirely, we cannot return here.
                        getArrayBlock.addStatement("result = gson.fromJson(in, $T[].class)", gsonModelClassName);
                        getArrayBlock.addStatement("break");

                    } else {
                        getArrayBlock.addStatement("return gson.fromJson(in, $T[].class)", gsonModelClassName);
                    }
                }
            });

            // Since we may not read the entire object, we won't return a result at the end of the code block.
            if (consumeReaderFully) {
                getArrayBlock.addStatement("return result");
            } else {
                getArrayBlock.addStatement("return null");
            }

        } else {
            // There is no custom root field specified, therefore this is the root field.
            if (consumeReaderFully) {
                // Since we read the json entirely, we cannot return here.
                getArrayBlock.addStatement("result = gson.fromJson(in, $T[].class)", gsonModelClassName);

            } else {
                getArrayBlock.addStatement("return gson.fromJson(in, $T[].class)", gsonModelClassName);
            }
        }
        getArrayJsonReader.addCode(getArrayBlock.build());
        streamerTypeBuilder.addMethod(getArrayJsonReader.build());

        // getList
        ParameterizedTypeName elementListType = ParameterizedTypeName.get(ClassName.get(List.class), gsonModelClassName);
        MethodSpec.Builder getListJsonReader = createBasicBuilder("getList", elementListType);

        final CodeBlock.Builder getListBlock = CodeBlock.builder();

        // If we are reading the whole object, we need to a variable declared outside the try/catch
        if (consumeReaderFully) {
            getListBlock.addStatement("$T result = null", elementListType);
        }

        if (rootElements.size() > 0) {
            // The code must navigate to the correct root field.
            addToSimpleCodeBlock(getListBlock, rootElements, new ObjectParserCallback() {
                @Override
                public void onInitialObjectNull() {
                    getListBlock.addStatement("return null");
                }

                @Override
                public void onInitialise() {

                }

                @Override
                public void onFieldAssigned(String fieldName) {

                }

                @Override
                public void onNodeEmpty() {
                    if (consumeReaderFully) {
                        // Sine we read the json entirely, we cannot return here.
                        getListBlock.addStatement("result = gson.fromJson(in, new com.google.gson.reflect.TypeToken<List<$T>>() {}.getType())", gsonModelClassName);
                        getListBlock.addStatement("break");

                    } else {
                        getListBlock.addStatement("return gson.fromJson(in, new com.google.gson.reflect.TypeToken<List<$T>>() {}.getType())", gsonModelClassName);
                    }
                }
            });

            // Since we may not read the entire object, we won't return a result at the end of the code block.
            if (consumeReaderFully) {
                getListBlock.addStatement("return result");
            } else {
                getListBlock.addStatement("return null");
            }

        } else {
            // There is no custom root field specified, therefore this is the root field.
            if (consumeReaderFully) {
                // Sine we read the json entirely, we cannot return here.
                getListBlock.addStatement("result = gson.fromJson(in, new com.google.gson.reflect.TypeToken<List<$T>>() {}.getType())", gsonModelClassName);

            } else {
                getListBlock.addStatement("return gson.fromJson(in, new com.google.gson.reflect.TypeToken<List<$T>>() {}.getType())", gsonModelClassName);
            }
        }
        getListJsonReader.addCode(getListBlock.build());
        streamerTypeBuilder.addMethod(getListJsonReader.build());

        // Stream results, multiple - json reader.
        MethodSpec.Builder streamMultipleJsonReader = createBasicBuilder("streamArraySegmented", null);
        streamMultipleJsonReader.addParameter(TypeName.INT, "streamSize");
        streamMultipleJsonReader.addParameter(ParameterizedTypeName.get(ClassName.get(GsonArrayStreamer.StreamCallback.class), elementArrayType), "callback");

        final CodeBlock.Builder streamCodeBlock = CodeBlock.builder();
        streamCodeBlock.addStatement("$T[] results", gsonModelClassName);
        streamCodeBlock.addStatement("StreamCallback.StreamHandler callbackResponse");
        streamCodeBlock.addStatement("int resultIndex");
        streamCodeBlock.add("\n");

        if (rootElements.size() > 0) {
            // The code must navigate to the correct root field.
            addToSimpleCodeBlock(streamCodeBlock, rootElements, new ObjectParserCallback() {
                @Override
                public void onInitialObjectNull() {
                    streamCodeBlock.addStatement("return");
                }

                @Override
                public void onInitialise() {
                    addStreamInitializerToCodeBlock(streamCodeBlock, gsonModelClassName);
                }

                @Override
                public void onFieldAssigned(String fieldName) {

                }

                @Override
                public void onNodeEmpty() {
                    addStreamCodeBlock(streamCodeBlock, gsonModelClassName);
                }
            });
        } else {
            // There is no custom root field specified, therefore this is the root field.
            streamCodeBlock.beginControlFlow("try");

            // Ensure that the array actually exists before attempting to read it.
            streamCodeBlock.add("// Ensure the array is not null.\n");
            streamCodeBlock.beginControlFlow("if (!isValidValue(in))");
            streamCodeBlock.addStatement("return");
            streamCodeBlock.endControlFlow();
            streamCodeBlock.add("\n");

            addStreamInitializerToCodeBlock(streamCodeBlock, gsonModelClassName);
            addStreamCodeBlock(streamCodeBlock, gsonModelClassName);

            streamCodeBlock.nextControlFlow("catch ($T e)", ClassName.get(IOException.class));
            streamCodeBlock.addStatement("throw new $T(e)", ClassName.get(JsonSyntaxException.class));
            streamCodeBlock.endControlFlow();
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
        streamerTypeBuilder.addMethod(streamMultipleJsonReader.build());

        if (writeFile(outputClassName.packageName(), streamerTypeBuilder)) {
            return new HandleResult(originalAdapterInterface, outputClassName);
        }
        throw new ProcessingException("Failed to write generated file: " + outputClassName.simpleName());
    }

    private void addStreamCodeBlock(CodeBlock.Builder builder, TypeName elementClassName) {
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

    private void addToSimpleCodeBlock(CodeBlock.Builder builder, GsonFieldTree rootElements, ObjectParserCallback callback) throws ProcessingException {
        builder.beginControlFlow("try");
        if (rootElements.size() > 0) {
            mCounterVariableCount = 0;

            createObjectParser(0, builder, rootElements, callback);
        }
        builder.nextControlFlow("catch ($T e)", ClassName.get(IOException.class));
        builder.addStatement("throw new $T(e)", ClassName.get(JsonSyntaxException.class));
        builder.endControlFlow();
    }

    private void addStreamInitializerToCodeBlock(CodeBlock.Builder builder, TypeName elementClassName) {
        builder.addStatement("results = new $T[streamSize]", elementClassName);
        builder.addStatement("callbackResponse = new StreamCallback.StreamHandler()");
        builder.addStatement("resultIndex = -1");
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
    protected String getClassNameSuffix() {
        return "GsonArrayStreamer";
    }

}
