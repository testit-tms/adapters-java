package ru.testit.listener;

import org.junit.jupiter.api.extension.*;
import ru.testit.models.TestMethod;
import ru.testit.services.TMSService;
import ru.testit.services.TestMethodType;
import ru.testit.models.ItemStatus;

import java.lang.reflect.*;

public class BaseJunit5Listener implements Extension, BeforeAllCallback, AfterAllCallback, InvocationInterceptor, TestWatcher {
    private final TMSService tmsService;

    public BaseJunit5Listener() {
        tmsService = new TMSService();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        tmsService.startLaunch();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        tmsService.finishLaunch();
    }

    @Override
    public void interceptBeforeAllMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
        startUtilMethod(TestMethodType.BEFORE_CLASS, invocationContext);
        finishUtilMethod(TestMethodType.BEFORE_CLASS, invocation);
    }

    @Override
    public void interceptBeforeEachMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
        startUtilMethod(TestMethodType.BEFORE_METHOD, invocationContext);
        finishUtilMethod(TestMethodType.BEFORE_METHOD, invocation);
    }

    @Override
    public void interceptTestMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) throws Exception {
        TestMethod method = Converter.ConvertMethod(extensionContext.getRequiredTestMethod());
        tmsService.startTestMethod(method);
        try {
            invocation.proceed();
        }
        catch (Throwable throwable) {
            finishTestMethod(ItemStatus.FAILED, extensionContext, throwable);
            throw new Exception(throwable.getMessage());
        }
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        finishTestMethod(ItemStatus.PASSED, context, null);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        finishTestMethod(ItemStatus.SKIPPED, context, cause);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        finishTestMethod(ItemStatus.FAILED, context, cause);
    }

    @Override
    public void interceptAfterEachMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
        startUtilMethod(TestMethodType.AFTER_METHOD, invocationContext);
        finishUtilMethod(TestMethodType.AFTER_METHOD, invocation);
    }

    @Override
    public void interceptAfterAllMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
        startUtilMethod(TestMethodType.AFTER_CLASS, invocationContext);
        finishUtilMethod(TestMethodType.AFTER_CLASS, invocation);
    }

    private void startUtilMethod(
            TestMethodType methodType,
            ReflectiveInvocationContext<Method> context
    ) {
        TestMethod method = Converter.ConvertMethod(context.getExecutable());
        tmsService.startUtilMethod(methodType, method);
    }

    private void finishUtilMethod(
            TestMethodType methodType,
            InvocationInterceptor.Invocation<Void> invocation
    ) {
        try {
            invocation.proceed();
            tmsService.finishUtilMethod(methodType, null);
        }
        catch (Throwable throwable) {
            tmsService.finishUtilMethod(methodType, throwable);
        }
    }

    private void finishTestMethod(ItemStatus status, ExtensionContext testResult, Throwable throwable) {
        TestMethod method = Converter.ConvertTestResult(testResult, throwable);
        tmsService.finishTestMethod(status, method);
    }
}
