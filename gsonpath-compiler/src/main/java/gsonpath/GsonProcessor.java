package gsonpath;

import com.google.auto.service.AutoService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import gsonpath.generator.*;
import gsonpath.generator.adapter.AutoGsonAdapterGenerator;
import gsonpath.generator.adapter.TypeAdapterLoaderGenerator;
import gsonpath.generator.streamer.GsonArrayStreamerGenerator;
import gsonpath.generator.streamer.StreamArrayLoaderGenerator;

@AutoService(Processor.class)
public class GsonProcessor extends AbstractProcessor {
    private static final String LOG_PREFIX = "Gson Path: ";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (annotations == null || annotations.size() == 0) {
            return false;
        }

        System.out.println();
        printMessage("Started annotation processing");
        Set<? extends Element> generatedAdapters = env.getElementsAnnotatedWith(AutoGsonAdapter.class);

        // Handle the standard type adapters.
        List<HandleResult> autoGsonAdapterResults = new ArrayList<>();
        AutoGsonAdapterGenerator adapterGenerator = new AutoGsonAdapterGenerator(processingEnv);
        for (Element element : generatedAdapters) {
            printMessage(String.format("Generating TypeAdapter (%s)", element));

            try {
                autoGsonAdapterResults.add(adapterGenerator.handle((TypeElement) element));
            } catch (ProcessingException e) {
                printError("Error while generating TypeAdapter", element);
                return false;
            }

        }

        if (autoGsonAdapterResults.size() > 0) {
            if (!new TypeAdapterLoaderGenerator(processingEnv).generate(autoGsonAdapterResults)) {
                printError("Error while generating TypeAdapterFactory");
                return false;
            }
        }

        // Handle the array adapters.
        Set<? extends Element> generatedArrayAdapters = env.getElementsAnnotatedWith(AutoGsonArrayStreamer.class);

        List<HandleResult> AutoGsonArrayStreamerResults = new ArrayList<>();
        GsonArrayStreamerGenerator arrayAdapterGenerator = new GsonArrayStreamerGenerator(processingEnv);
        for (Element element : generatedArrayAdapters) {
            printMessage(String.format("Generating StreamAdapter (%s)", element));

            try {
                AutoGsonArrayStreamerResults.add(arrayAdapterGenerator.handle((TypeElement) element));
            } catch (ProcessingException e) {
                printError("Error while generating StreamAdapter", element);
                return false;
            }

        }

        if (AutoGsonArrayStreamerResults.size() > 0) {
            if (!new StreamArrayLoaderGenerator(processingEnv).generate(AutoGsonArrayStreamerResults)) {
                printError("Error while generating StreamAdapterFactory");
                return false;
            }
        }
        printMessage("Finished annotation processing");
        System.out.println();

        return false;
    }

    private void printMessage(String message) {
        System.out.println(LOG_PREFIX + message);
    }

    private void printError(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, LOG_PREFIX + message);
    }

    private void printError(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, LOG_PREFIX + message, element);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedTypes = new LinkedHashSet<>();
        supportedTypes.add(AutoGsonAdapter.class.getCanonicalName());
        supportedTypes.add(AutoGsonArrayStreamer.class.getCanonicalName());
        return supportedTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}