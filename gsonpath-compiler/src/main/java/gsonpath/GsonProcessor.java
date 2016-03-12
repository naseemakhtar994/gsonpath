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

import gsonpath.generator.AutoGsonAdapterGenerator;
import gsonpath.generator.HandleResult;
import gsonpath.generator.LoaderGenerator;

/**
 * Created by Lachlan on 1/03/2016.
 */
@AutoService(Processor.class)
public class GsonProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Set<? extends Element> generatedAdapters = env.getElementsAnnotatedWith(AutoGsonAdapter.class);

        List<HandleResult> autoGsonAdapterResults = new ArrayList<>();
        AutoGsonAdapterGenerator generator = new AutoGsonAdapterGenerator(processingEnv);
        for (Element element : generatedAdapters) {
            System.out.println("Handling element: " + element.getSimpleName());

            try {
                autoGsonAdapterResults.add(generator.handle((TypeElement) element));
            } catch (ProcessingException e) {
                return false;
            }

        }

        return new LoaderGenerator(processingEnv).generate(autoGsonAdapterResults);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedTypes = new LinkedHashSet<>();
        supportedTypes.add(AutoGsonAdapter.class.getCanonicalName());
        supportedTypes.add(GsonPathField.class.getCanonicalName());
        return supportedTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}