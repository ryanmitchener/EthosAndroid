package com.ethossoftworks.compiler;

import com.ethossoftworks.annotations.SaveState;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
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
 * 3. Enable annotation processing in IDE: For Android Studio: File -> Other Settings -> Default Settings ->  Build, Execution, Deployment -> Compiler -> Annotation Processors
 *    and click "Enable Annotation Processing"
 *
 * To Test:
 * 1. In the terminal, run the following: sh gradlew --no-daemon -Dorg.gradle.debug=true :ethos:clean :uitest:clean :uitest:compileDebugJavaWithJavac
 * 2. Click debug in android
 */

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.ethossoftworks.annotations.SaveState")
public class SaveStateProcessor extends AbstractProcessor {
    private final String FILE_SUFFIX = "_SaveState";

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            if (roundEnvironment.getElementsAnnotatedWith(SaveState.class).size() == 0) {
                return false;
            }

            MethodSpec.Builder saveState = MethodSpec.methodBuilder("saveState").addModifiers(Modifier.PUBLIC);
            MethodSpec.Builder restoreState = MethodSpec.methodBuilder("restoreState").addModifiers(Modifier.PUBLIC);
            Element enclosingType = null;

            for (Element element : roundEnvironment.getElementsAnnotatedWith(SaveState.class)) {
                if (enclosingType == null) {
                    enclosingType = element.getEnclosingElement();

                    // TODO: Get public fields from superclass
//                    TypeElement test = (TypeElement) (((DeclaredType) ((TypeElement) enclosingType).getSuperclass()).asElement());
//                    test.getAnnotationsByType(SaveState.class);
//                    String testName = test.getQualifiedName().toString();
//                    System.out.println(test.getQualifiedName());
////                    saveState.addStatement("private final ");
                }
                addMethodStatements(saveState, restoreState, element.getSimpleName().toString());
            }

            saveState.addParameter(ClassName.get((TypeElement) enclosingType), "target");
            saveState.addParameter(ClassName.get("com.ethossoftworks.ethos.StateSaver", "StateDataMap"), "dataMap");
            restoreState.addParameter(ClassName.get((TypeElement) enclosingType), "target");
            restoreState.addParameter(ClassName.get("com.ethossoftworks.ethos.StateSaver", "StateDataMap"), "dataMap");

            TypeSpec typeSpec = TypeSpec.classBuilder(getClassName(enclosingType) + FILE_SUFFIX)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.get("com.ethossoftworks.ethos.StateSaver", "StateHandler"), ClassName.get((TypeElement) enclosingType)))
                    .addMethod(saveState.build())
                    .addMethod(restoreState.build())
                    .build();

            JavaFile javaFile = JavaFile.builder(getPackageName(enclosingType), typeSpec).build();
            javaFile.writeTo(processingEnv.getFiler());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private void addMethodStatements(MethodSpec.Builder saveState, MethodSpec.Builder restoreState, String fieldName) {
        saveState.addStatement("dataMap.put($S, target.$L)", fieldName, fieldName);
        restoreState.addStatement("target.$L = dataMap.removeWithType($S)", fieldName, fieldName);
    }


    private String getClassName(Element enclosingType) {
        return enclosingType.getSimpleName().toString();
    }


    private String getPackageName(Element enclosingType) {
        String qualifiedName = ((TypeElement) enclosingType).getQualifiedName().toString();
        return qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
    }
}