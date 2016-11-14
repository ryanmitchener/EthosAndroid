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

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.ethossoftworks.annotations.SaveState")
public class SaveStateProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            for (Element element : roundEnvironment.getElementsAnnotatedWith(SaveState.class)) {
                TypeElement test = (TypeElement) element.getEnclosingElement();
                String testString = test.getQualifiedName().toString();

                MethodSpec main = MethodSpec.methodBuilder("main")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(String[].class, "args")
                        .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                        .build();


                TypeSpec typeSpec = TypeSpec.classBuilder(test.getSimpleName().toString())
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethod(main)
                        .build();

                JavaFile javaFile = JavaFile.builder(testString + "_SaveState", typeSpec).build();
                javaFile.writeTo(processingEnv.getFiler());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private String getFileName(Element field) {
        return field.getEnclosingElement().getSimpleName().toString();
    }


    private String getPackageName(Element field) {
        StringBuilder packageName = new StringBuilder();

        while (field != null) {
            packageName.insert(0, field.getSimpleName() + "/");
            field = field.getEnclosingElement();
        }

        return packageName.toString();
    }
}