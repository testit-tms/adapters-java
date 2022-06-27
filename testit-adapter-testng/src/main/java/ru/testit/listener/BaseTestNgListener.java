package ru.testit.listener;

import org.testng.*;
import ru.testit.services.ItemStatus;
import ru.testit.models.TestMethod;
import ru.testit.services.TestItService;
import ru.testit.services.TestMethodType;

import java.lang.reflect.Method;

public class BaseTestNgListener implements IExecutionListener, ITestListener, IClassListener {
    private final TestItService testItService;

    public BaseTestNgListener() {
        testItService = new TestItService();
    }

    @Override
    public void onExecutionStart() {
        testItService.startLaunch();
    }

    @Override
    public void onExecutionFinish() {
        testItService.finishLaunch();
    }

    @Override
    public void onBeforeClass(ITestClass testClass) {
        for (final org.testng.ITestNGMethod testMethod : testClass.getBeforeClassMethods()) {
            Method method = testMethod.getConstructorOrMethod().getMethod();
            TestMethod testItMethod = Converter.ConvertMethod(method);

            testItService.startUtilMethod(TestMethodType.BEFORE_CLASS, testItMethod);
            testItService.finishUtilMethod(TestMethodType.BEFORE_CLASS, null);
        }
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        for (final org.testng.ITestNGMethod testMethod : testClass.getAfterClassMethods()) {
            Method method = testMethod.getConstructorOrMethod().getMethod();
            TestMethod testItMethod = Converter.ConvertMethod(method);

            testItService.startUtilMethod(TestMethodType.AFTER_CLASS, testItMethod);
            testItService.finishUtilMethod(TestMethodType.AFTER_CLASS, null);
        }
    }

    @Override
    public void onTestStart(ITestResult testResult) {
        TestMethod method = Converter.ConvertTestResult(testResult);

        testItService.startTestMethod(method);
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
        finishTestMethod(ItemStatus.PASSED, testResult);
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        finishTestMethod(ItemStatus.FAILED, testResult);
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        finishTestMethod(ItemStatus.SKIPPED, testResult);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        onTestFailure(result);
    }

    // not implemented
    @Override
    public void onStart(ITestContext testContext) {
    }

    // not implemented
    @Override
    public void onFinish(ITestContext testContext) {
    }

    private void finishTestMethod(ItemStatus status, ITestResult testResult) {
        TestMethod method = Converter.ConvertTestResult(testResult);
        testItService.finishTestMethod(status, method);
    }
}
