package ru.testit.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import ru.testit.models.ItemStatus;
import ru.testit.models.StepResult;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
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

    @Pointcut("@annotation(ru.testit.annotations.Step)")
    public void withStepAnnotation() {
    }

    @Pointcut("execution(* *(..))")
    public void anyMethod() {
    }

   @Before("anyMethod() && withStepAnnotation()")
    public void startStep(final JoinPoint joinPoint) {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final String uuid = UUID.randomUUID().toString();
        Method method = signature.getMethod();

        Parameter[] parameters = method.getParameters();
        Map<String, String> stepParameters = new HashMap<>();

        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];

            String name = parameter.getName();
            String value = joinPoint.getArgs()[i].toString();

            stepParameters.put(name, value);
        }

        final StepResult result = new StepResult()
                .setName(Utils.extractTitle(method, stepParameters))
                .setDescription(Utils.extractDescription(method, stepParameters))
                .setParameters(stepParameters);

        getManager().startStep(uuid, result);
    }

    @AfterReturning(value = "anyMethod() && withStepAnnotation()")
    public void finishStep() {
        getManager().updateStep(s -> s.setItemStatus(ItemStatus.PASSED));
        getManager().stopStep();
    }

    @AfterThrowing(value = "anyMethod() && withStepAnnotation()", throwing = "throwable")
    public void failedStep(final Throwable throwable) {
        getManager().updateStep(s ->
                s.setItemStatus(ItemStatus.FAILED)
                        .setThrowable(throwable)
        );
        getManager().stopStep();
    }

    private AdapterManager getManager() {
        return adapterManager.get();
    }
}
