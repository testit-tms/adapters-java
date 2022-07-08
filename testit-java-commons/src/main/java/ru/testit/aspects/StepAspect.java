package ru.testit.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import ru.testit.annotations.Step;
import ru.testit.models.*;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.Utils;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
public class StepAspect {
    private static final InheritableThreadLocal<AdapterManager> adapterManager
            = new InheritableThreadLocal<AdapterManager>() {
        @Override
        protected AdapterManager initialValue() {
            return Adapter.getAdapterManager();
        }
    };

    @Pointcut("@annotation(step)")
    public void withStepAnnotation(final Step step) {
    }

    @Pointcut("execution(* *.*(..))")
    public void anyMethod() {
    }

    @Before("anyMethod() && withStepAnnotation(step)")
    public void startStep(final JoinPoint joinPoint, Step step) {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final String uuid = UUID.randomUUID().toString();
        Method method = signature.getMethod();

        final StepResult result = new StepResult()
                .setName(Utils.extractTitle(method))
                .setDescription(Utils.extractDescription(method));

        getManager().startStep(uuid, result);
    }

    @AfterReturning(value = "anyMethod() && withStepAnnotation(step)")
    public void finishStep(Step step) {
        getManager().updateStep(s -> s.setItemStatus(ItemStatus.PASSED));
        getManager().stopStep();
    }

    @AfterThrowing(value = "anyMethod() && withStepAnnotation(step)", throwing = "throwable")
    public void failedStep(final Throwable throwable, Step step) {
        getManager().updateStep(s ->
                s.setItemStatus(ItemStatus.FAILED)
                        .setThrowable(throwable)
        );
        getManager().stopStep();
    }

    private AdapterManager getManager(){
        return adapterManager.get();
    }
}
