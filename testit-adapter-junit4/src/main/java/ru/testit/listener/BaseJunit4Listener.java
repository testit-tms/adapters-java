package ru.testit.listener;

import org.junit.runner.notification.RunListener;
import org.junit.runners.model.*;
import com.nordstrom.automation.junit.*;
import org.junit.internal.*;
import org.junit.internal.runners.model.*;
import ru.testit.models.TestMethod;
import ru.testit.services.TMSService;
import ru.testit.services.TestMethodType;
import ru.testit.services.ItemStatus;

public class BaseJunit4Listener implements RunnerWatcher, RunWatcher<FrameworkMethod>, MethodWatcher<FrameworkMethod>
{
    private final TMSService tmsService;

    public BaseJunit4Listener() {
        tmsService = new TMSService();
    }

    public void runStarted(Object runner) {
        tmsService.startLaunch();
    }

    public void runFinished(Object runner) {
        tmsService.finishLaunch();
    }

    public void testStarted(AtomicTest<FrameworkMethod> atomicTest) {
        TestMethod method = Converter.ConvertMethod(atomicTest.getIdentity().getMethod());
        tmsService.startTestMethod(method);
    }

    public void testFailure(AtomicTest<FrameworkMethod> atomicTest, Throwable thrown) {
        finishTestMethod(ItemStatus.FAILED, atomicTest);
    }

    public void testAssumptionFailure(
            AtomicTest<FrameworkMethod> atomicTest,
            AssumptionViolatedException thrown
    ) {
        finishTestMethod(ItemStatus.FAILED, atomicTest);
    }

    public void testIgnored(AtomicTest<FrameworkMethod> atomicTest) {
        finishTestMethod(ItemStatus.SKIPPED, atomicTest);
    }

    public void testFinished(AtomicTest<FrameworkMethod> atomicTest) {
        finishTestMethod(ItemStatus.PASSED, atomicTest);
    }

    public void beforeInvocation(
            Object runner,
            FrameworkMethod method,
            ReflectiveCallable callable
    ) {
        TestMethod m = Converter.ConvertMethod(method.getMethod());
        tmsService.startUtilMethod(TestMethodType.BEFORE_METHOD, m);
    }

    public void afterInvocation(
            Object runner,
            FrameworkMethod method,
            ReflectiveCallable callable,
            Throwable thrown) {
        tmsService.finishUtilMethod(TestMethodType.AFTER_METHOD, thrown);
    }

    public Class<FrameworkMethod> supportedType() {
        return FrameworkMethod.class;
    }

//    public TestMethodType isJunitMethod(final FrameworkMethod method) {
//        for (TestMethodType type : TestMethodType.values()) {
//            if (method.getAnnotation(type.getDeclaringClass()) != null)
//                return type;
//        }
//        return null;
//    }

    private void finishTestMethod(ItemStatus status, AtomicTest<FrameworkMethod> testResult) {
        TestMethod method = Converter.ConvertTestResult(testResult);
        tmsService.finishTestMethod(status, method);
    }
}