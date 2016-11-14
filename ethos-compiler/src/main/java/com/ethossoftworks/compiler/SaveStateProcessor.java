package com.ethossoftworks.compiler;

import com.ethossoftworks.annotations.SaveState;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;


/**
 * To Setup:
 * 1. Click Run -> Edit Configurations
 * 2. Add a remote configuration named "Annotation Processor". Host: localhost, Port: 5005
 *
 * To Test:
 * 1. In the terminal, run the following: sh gradlew --no-daemon -Dorg.gradle.debug=true :ethos:clean :uitest:clean :uitest:compileDebugJavaWithJavac
 * 2. Click debug in android
 */

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.ethossoftworks.annotations.SaveState")
public class SaveStateProcessor extends AbstractProcessor {
    public final String FILE_SUFFIX = "_SaveState";

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            for (Element element : roundEnvironment.getElementsAnnotatedWith(SaveState.class)) {
                MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                        .addParameter(String[].class, "args")
                        .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!");

                MethodSpec.Builder saveState = MethodSpec.methodBuilder("saveState")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String[].class, "args")
                        .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!");

                MethodSpec.Builder restoreState = MethodSpec.methodBuilder("restoreState")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String[].class, "args")
                        .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!");

                TypeSpec typeSpec = TypeSpec.classBuilder(getClassName(element) + FILE_SUFFIX)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethod(constructor.build())
                        .addMethod(saveState.build())
                        .addMethod(restoreState.build())
                        .build();

                JavaFile javaFile = JavaFile.builder(getPackageName(element), typeSpec).build();
                javaFile.writeTo(processingEnv.getFiler());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private String getClassName(Element annotation) {
        return annotation.getEnclosingElement().getSimpleName().toString();
    }


    private String getPackageName(Element annotation) {
        String qualifiedName = ((TypeElement) annotation.getEnclosingElement()).getQualifiedName().toString();
        return qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
    }
}