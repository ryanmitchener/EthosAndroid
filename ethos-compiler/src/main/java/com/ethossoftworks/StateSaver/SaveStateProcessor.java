package com.ethossoftworks.statesaver;

import com.google.auto.service.AutoService;

import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.ethossoftworks.statesaver.SaveState")
public class SaveStateProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            for (TypeElement originatingElement : set) {
                for (Element element : roundEnvironment.getElementsAnnotatedWith(originatingElement)) {
                    JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(element.getEnclosingElement().getSimpleName() + "_", element);
                    Writer writer = sourceFile.openWriter();

                    writer.write(element.getSimpleName().toString());
                    writer.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}