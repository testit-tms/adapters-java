package ru.testit.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import ru.testit.annotations.AddLink;
import ru.testit.annotations.Step;
import ru.testit.models.*;
import ru.testit.services.TmsFactory;
import ru.testit.services.TmsManager;
import ru.testit.services.Utils;

import java.util.UUID;

@Aspect
public class StepAspect {
    private static final InheritableThreadLocal<TmsManager> tmsService
            = new InheritableThreadLocal<TmsManager>() {
        @Override
        protected TmsManager initialValue() {
            return TmsFactory.getTmsManager();
        }
    };

    @Pointcut("@annotation(step)")
    public void withStepAnnotation(final Step step) {
    }

    @Pointcut("execution(* *.*(..))")
    public void anyMethod() {
    }

    @Before("anyMethod() && withStepAnnotation(step)")
    public void startNestedStep(final JoinPoint joinPoint, Step step) {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final String uuid = UUID.randomUUID().toString();

        final StepResult result = new StepResult()
                .setName(Utils.extractTitle(signature.getMethod()))
                .setDescription(Utils.extractDescription(signature.getMethod()));

        tmsService.get().startStep(uuid, result);
    }

    @AfterReturning(value = "anyMethod() && withStepAnnotation(step)")
    public void finishNestedStep(Step step) {
        tmsService.get().updateStep(s -> s.setItemStatus(ItemStatus.PASSED));
        tmsService.get().stopStep();
    }

    @AfterThrowing(value = "anyMethod() && withStepAnnotation(step)", throwing = "throwable")
    public void failedNestedStep(final Throwable throwable, Step step) {
        tmsService.get().updateStep(s ->
                s.setItemStatus(ItemStatus.FAILED)
                        .setThrowable(throwable)
        );
        tmsService.get().stopStep();
    }

//    @Pointcut("@annotation(addLink)")
//    public void withAddLinkAnnotation(final AddLink addLink) {
//    }
//
//    @Pointcut("args(linkItem)")
//    public void hasLinkArg(final LinkItem linkItem) {
//    }
//
//    @Before(value = "withAddLinkAnnotation(addLink) && hasLinkArg(linkItem) && anyMethod()", argNames = "addLink,linkItem")
//    public void startAddLink(final AddLink addLink, final LinkItem linkItem) {
//        final StepNode stepNode = StepAspect.currentStep.get();
//        if (stepNode == null) {
//            return;
//        }
//        stepNode.getLinkItems().add(linkItem);
//    }
//
//    public static void setStepNodes(final StepNode parentNode) {
//        StepAspect.previousStep.set(StepAspect.currentStep.get());
//        StepAspect.currentStep.set(parentNode);
//    }
//
//    public static void returnStepNode() {
//        StepAspect.currentStep.set(StepAspect.previousStep.get());
//        StepAspect.previousStep.set(StepAspect.currentStep.get());
//    }
//@AddLink
//public static void addLink(final LinkItem linkItem) {
//}
}
