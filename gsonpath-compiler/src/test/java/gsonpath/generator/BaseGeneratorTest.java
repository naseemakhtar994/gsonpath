package gsonpath.generator;

import com.google.testing.compile.JavaFileObjects;
import gsonpath.GsonProcessor;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Created by Lachlan on 2/03/2016.
 */
public abstract class BaseGeneratorTest {
    protected static final String STANDARD_PACKAGE_NAME = "package com.test;";

    public void assertGeneratedContent(String annotatedClassName, String annotatedClassSource,
                                       String typeAdapterClassName, String typeAdapterSource) {
        assertAbout(javaSource())
                .that(JavaFileObjects.forSourceString(annotatedClassName, annotatedClassSource))
                .processedWith(new GsonProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forSourceString(typeAdapterClassName, typeAdapterSource));
    }

    public void assertGeneratedContent(String annotatedClassSource, String typeAdapterSource) {
        assertGeneratedContent("test.Test", annotatedClassSource, "test.Test_GsonTypeAdapter", typeAdapterSource);
    }

}
