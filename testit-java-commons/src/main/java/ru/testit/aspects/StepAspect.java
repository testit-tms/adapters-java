package ru.testit.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import ru.testit.annotations.AddLink;
import ru.testit.annotations.Step;
import ru.testit.models.LinkItem;
import ru.testit.models.Outcome;
import ru.testit.models.StepNode;
import ru.testit.models.StepUtils;

import java.util.Date;

@Aspect
public class StepAspect
{
    private static final InheritableThreadLocal<StepNode> currentStep;
    private static final InheritableThreadLocal<StepNode> previousStep;

    @Pointcut("@annotation(step)")
    public void withStepAnnotation(final Step step) {
    }
    
    @Pointcut("execution(* *.*(..))")
    public void anyMethod() {
    }
    
    @Before("anyMethod() && withStepAnnotation(step)")
    public void startNestedStep(final JoinPoint joinPoint, final Step step) {
        final MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        final StepNode currStep = StepAspect.currentStep.get();
        if (currStep != null) {
            final StepNode newStep = StepUtils.makeStepNode(signature, currStep);
            newStep.setStartedOn(new Date());
            currStep.getChildren().add(newStep);
            StepAspect.currentStep.set(newStep);
        }
    }
    
    @AfterReturning(value = "anyMethod() && withStepAnnotation(step)", argNames = "step")
    public void finishNestedStep(final Step step) {
        final StepNode currStep = StepAspect.currentStep.get();
        if (currStep != null) {
            currStep.setCompletedOn(new Date());
            currStep.setOutcome(Outcome.PASSED.getValue());
            StepAspect.currentStep.set(currStep.getParent());
        }
    }
    
    @AfterThrowing(value = "anyMethod() && withStepAnnotation(step)", throwing = "throwable", argNames = "step,throwable")
    public void failedNestedStep(final Step step, final Throwable throwable) {
        final StepNode currStep = StepAspect.currentStep.get();
        if (currStep != null) {
            currStep.setCompletedOn(new Date());
            currStep.setOutcome(Outcome.FAILED.getValue());
            StepAspect.currentStep.set(currStep.getParent());
        }
    }

    @Pointcut("@annotation(addLink)")
    public void withAddLinkAnnotation(final AddLink addLink) {
    }

    @Pointcut("args(linkItem)")
    public void hasLinkArg(final LinkItem linkItem) {
    }

    @Before(value = "withAddLinkAnnotation(addLink) && hasLinkArg(linkItem) && anyMethod()", argNames = "addLink,linkItem")
    public void startAddLink(final AddLink addLink, final LinkItem linkItem) {
        final StepNode stepNode = StepAspect.currentStep.get();
        if (stepNode == null) {return;}
        stepNode.getLinkItems().add(linkItem);
    }

    public static void setStepNodes(final StepNode parentNode) {
        StepAspect.previousStep.set(StepAspect.currentStep.get());
        StepAspect.currentStep.set(parentNode);
    }

    public static void returnStepNode() {
        StepAspect.currentStep.set(StepAspect.previousStep.get());
        StepAspect.previousStep.set(StepAspect.currentStep.get());
    }

    static {
        currentStep = new InheritableThreadLocal<StepNode>();
        previousStep = new InheritableThreadLocal<StepNode>();
    }
}
