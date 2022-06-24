package ru.testit.models;

import org.aspectj.lang.reflect.MethodSignature;
import ru.testit.annotations.Description;
import ru.testit.annotations.Title;

public class StepUtils
{
    public static StepNode makeStepNode(final MethodSignature signature, final StepNode currentStep) {
        final StepNode node = new StepNode();
        node.setTitle(extractTitle(signature));
        node.setDescription(extractDescription(signature));
        node.setParent(currentStep);
        return node;
    }
    
    private static String extractTitle(final MethodSignature signature) {
        final Title title = signature.getMethod().getAnnotation(Title.class);
        return (title != null) ? title.value() : null;
    }
    
    private static String extractDescription(final MethodSignature signature) {
        final Description description = signature.getMethod().getAnnotation(Description.class);
        return (description != null) ? description.value() : null;
    }
}
