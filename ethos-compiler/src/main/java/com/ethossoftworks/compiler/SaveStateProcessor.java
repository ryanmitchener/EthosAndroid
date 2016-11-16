package com.ethossoftworks.compiler;

import com.ethossoftworks.annotations.SaveState;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;


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

    private final Set<String> ignoredClasses = new HashSet<>();
    private final Set<String> annotatedClasses = new HashSet<>();


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.size() == 0) {
            return false;
        }

        Set<? extends Element> annotations = roundEnvironment.getElementsAnnotatedWith(SaveState.class);

        Element enclosingClass = annotations.iterator().next().getEnclosingElement();
        if (enclosingClass == null || !isEnclosingClassValid(enclosingClass)) {
            return false;
        }

        MethodSpec.Builder saveState = buildStateHandlerMethodSpec("saveState", enclosingClass);
        MethodSpec.Builder restoreState = buildStateHandlerMethodSpec("restoreState", enclosingClass);

        addParentStatementsForAnnotation(saveState, restoreState, getStateHandlerParent((TypeElement) enclosingClass));

        for (Element element : annotations) {
            if (!isAnnotatedFieldValid(element)) {
                return false;
            }
            addDataMapStatementsForAnnotation(saveState, restoreState, element.getSimpleName().toString());
        }

        return writeFile(enclosingClass, buildStateHandlerTypeSpec(enclosingClass, saveState, restoreState));
    }


    private void addParentStatementsForAnnotation(MethodSpec.Builder saveState, MethodSpec.Builder restoreState, String parent) {
        if (parent != null) {
            saveState.addStatement("new " + parent + FILE_SUFFIX + "().saveState(target, dataMap)");
            restoreState.addStatement("new " + parent + FILE_SUFFIX + "().restoreState(target, dataMap)");
        }
    }


    private void addDataMapStatementsForAnnotation(MethodSpec.Builder saveState, MethodSpec.Builder restoreState, String fieldName) {
        saveState.addStatement("dataMap.put($S, target.$L)", fieldName, fieldName);
        restoreState.addStatement("target.$L = dataMap.removeWithType($S)", fieldName, fieldName);
    }


    private MethodSpec.Builder buildStateHandlerMethodSpec(String name, Element enclosingClass) {
        return MethodSpec.methodBuilder(name).addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get((TypeElement) enclosingClass), "target")
                .addParameter(ClassName.get("com.ethossoftworks.ethos.StateSaver", "StateDataMap"), "dataMap");
    }


    private TypeSpec buildStateHandlerTypeSpec(Element enclosingClass, MethodSpec.Builder saveState, MethodSpec.Builder restoreState) {
        return TypeSpec.classBuilder(enclosingClass.getSimpleName().toString() + FILE_SUFFIX)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(ParameterizedTypeName.get(ClassName.get("com.ethossoftworks.ethos.StateSaver", "StateHandler"), ClassName.get((TypeElement) enclosingClass)))
            .addMethod(saveState.build())
            .addMethod(restoreState.build())
            .build();
    }


    private String getStateHandlerParent(TypeElement classType) {
        TypeMirror type;
        while (true) {
            type = classType.getSuperclass();
            if (type.getKind() == TypeKind.NONE) {
                return null;
            }
            classType = (TypeElement) ((DeclaredType) type).asElement();
            String className = classType.toString();

            if (ignoredClasses.contains(className)) {
                continue;
            }

            if (annotatedClasses.contains(className)) {
                return className;
            }

            if (isClassAnnotated(classType)) {
                annotatedClasses.add(className);
                return className;
            }

            ignoredClasses.add(className);
        }
    }


    private boolean isAnnotatedFieldValid(Element element) {
        boolean isInvalid = element.getModifiers().contains(Modifier.PRIVATE) ||
            element.getModifiers().contains(Modifier.STATIC) ||
            element.getModifiers().contains(Modifier.FINAL);
        if (isInvalid) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "StateSaverError: Field '" + element.getSimpleName() + "' in '" + ((TypeElement) element.getEnclosingElement()).getQualifiedName() + "' must not be private, static or final");
        }
        return !isInvalid;
    }


    private boolean isEnclosingClassValid(Element enclosingClass) {
        boolean isInvalid = enclosingClass.getModifiers().contains(Modifier.PRIVATE);
        if (isInvalid) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "StateSaverError: Enclosing class '" + ((TypeElement) enclosingClass).getQualifiedName() + "' must not be private");
        }
        return !isInvalid;
    }


    private boolean isClassAnnotated(TypeElement classType) {
        List<VariableElement> fields = ElementFilter.fieldsIn(classType.getEnclosedElements());
        for (Element e : fields) {
            for (AnnotationMirror am : e.getAnnotationMirrors()) {
                if (am.getAnnotationType().toString().equals(SaveState.class.getName())) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean writeFile(Element enclosingClass, TypeSpec typeSpec) {
        try {
            JavaFile javaFile = JavaFile.builder(getPackageName(enclosingClass), typeSpec).build();
            javaFile.writeTo(processingEnv.getFiler());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private String getPackageName(Element enclosingType) {
        String qualifiedName = ((TypeElement) enclosingType).getQualifiedName().toString();
        return qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
    }
}