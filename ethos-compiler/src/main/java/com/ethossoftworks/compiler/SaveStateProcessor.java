package com.ethossoftworks.compiler;

import com.ethossoftworks.annotations.SaveState;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
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

import static com.sun.org.apache.xalan.internal.xsltc.compiler.sym.error;


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
        try {
            if (roundEnvironment.getElementsAnnotatedWith(SaveState.class).size() == 0) {
                return false;
            }

            MethodSpec.Builder saveState = MethodSpec.methodBuilder("saveState").addModifiers(Modifier.PUBLIC);
            MethodSpec.Builder restoreState = MethodSpec.methodBuilder("restoreState").addModifiers(Modifier.PUBLIC);
            Element enclosingType = null;

            for (Element element : roundEnvironment.getElementsAnnotatedWith(SaveState.class)) {
                if (!isAnnotatedFieldValid(element)) {
                    return false;
                }
                if (enclosingType == null) {
                    enclosingType = element.getEnclosingElement();
                    if (!isEnclosingClassValid(enclosingType)) {
                        return false;
                    }

                    String parent = getParent((TypeElement) enclosingType);
                    if (parent != null) {
                        saveState.addStatement("new " + parent + FILE_SUFFIX + "().saveState(target, dataMap)");
                        restoreState.addStatement("new " + parent + FILE_SUFFIX + "().restoreState(target, dataMap)");
                    }
                }
                addMethodStatements(saveState, restoreState, element.getSimpleName().toString());
            }

            saveState.addParameter(ClassName.get((TypeElement) enclosingType), "target");
            saveState.addParameter(ClassName.get("com.ethossoftworks.ethos.StateSaver", "StateDataMap"), "dataMap");
            restoreState.addParameter(ClassName.get((TypeElement) enclosingType), "target");
            restoreState.addParameter(ClassName.get("com.ethossoftworks.ethos.StateSaver", "StateDataMap"), "dataMap");

            TypeSpec typeSpec = TypeSpec.classBuilder(enclosingType.getSimpleName().toString() + FILE_SUFFIX)
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


    private String getParent(TypeElement classType) {
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


    private void addMethodStatements(MethodSpec.Builder saveState, MethodSpec.Builder restoreState, String fieldName) {
        saveState.addStatement("dataMap.put($S, target.$L)", fieldName, fieldName);
        restoreState.addStatement("target.$L = dataMap.removeWithType($S)", fieldName, fieldName);
    }


    private String getPackageName(Element enclosingType) {
        String qualifiedName = ((TypeElement) enclosingType).getQualifiedName().toString();
        return qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
    }
}