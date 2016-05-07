package gsonpath.generator;

import com.google.testing.compile.CompileTester;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.ProcessedCompileTesterFactory;
import gsonpath.GsonProcessor;

import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public abstract class BaseGeneratorTest {

    public void assertGeneratedContent(TestCriteria criteria) {
        ProcessedCompileTesterFactory testerFactory;

        // Add all the required 'source' files.
        int sourceFilesSize = criteria.getSourceFilesSize();
        if (sourceFilesSize == 1) {
            testerFactory = assertAbout(javaSource()).that(criteria.getSourceFileObject(0));

        } else {
            // Since we have multiple sources, we need to use a slightly different assert.
            ArrayList<JavaFileObject> sources = new ArrayList<>();
            for (int i = 0; i < sourceFilesSize; i++) {
                sources.add(criteria.getSourceFileObject(i));
            }

            testerFactory = assertAbout(javaSources()).that(sources);
        }

        CompileTester.GeneratedPredicateClause predicateClause = testerFactory.processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and();

        // Add all the required 'generated' files based off the input source files.
        int generatedFilesSize = criteria.getGeneratedFilesSize();
        JavaFileObject[] generatedSources = new JavaFileObject[generatedFilesSize];
        for (int i = 0; i < generatedFilesSize; i++) {
            generatedSources[i] = criteria.getGeneratedFileObject(i);
        }

        if (generatedSources.length == 1) {
            predicateClause.generatesSources(generatedSources[0]);

        } else {
            predicateClause.generatesSources(generatedSources[0], Arrays.copyOfRange(generatedSources, 1, generatedSources.length - 1));
        }
    }

    public static class TestCriteria {
        private final String resourcePath;
        private final List<String> relativeSourceNames;
        private final List<String> relativeGeneratedNames;
        private final List<String> absoluteSourceNames;
        private final List<String> absoluteGeneratedNames;

        public TestCriteria(String resourcePath) {
            this.resourcePath = resourcePath;
            this.relativeSourceNames = new ArrayList<>();
            this.relativeGeneratedNames = new ArrayList<>();
            this.absoluteSourceNames = new ArrayList<>();
            this.absoluteGeneratedNames = new ArrayList<>();
        }

        public TestCriteria addRelativeSource(String fileName) {
            this.relativeSourceNames.add(fileName);
            return this;
        }

        public TestCriteria addRelativeGenerated(String fileName) {
            this.relativeGeneratedNames.add(fileName);
            return this;
        }

        public TestCriteria addAbsoluteSource(String fileName) {
            this.absoluteSourceNames.add(fileName);
            return this;
        }

        public TestCriteria addAbsoluteGenerated(String fileName) {
            this.absoluteGeneratedNames.add(fileName);
            return this;
        }

        public int getSourceFilesSize() {
            return relativeSourceNames.size() + absoluteSourceNames.size();
        }

        public int getGeneratedFilesSize() {
            return relativeGeneratedNames.size() + absoluteGeneratedNames.size();
        }

        public JavaFileObject getSourceFileObject(int index) {
            int relativeSize = relativeSourceNames.size();
            if (index < relativeSize) {
                return JavaFileObjects.forResource(resourcePath + "/" + relativeSourceNames.get(index));
            }
            return JavaFileObjects.forResource(absoluteSourceNames.get(index - relativeSize));
        }

        public JavaFileObject getGeneratedFileObject(int index) {
            int relativeSize = relativeGeneratedNames.size();
            if (index < relativeSize) {
                return JavaFileObjects.forResource(resourcePath + "/" + relativeGeneratedNames.get(index));
            }
            return JavaFileObjects.forResource(absoluteGeneratedNames.get(index - relativeSize));
        }

    }

}
