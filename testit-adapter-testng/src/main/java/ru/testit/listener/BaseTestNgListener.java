package ru.testit.listener;

import org.testng.*;
import ru.testit.services.ItemStatus;
import ru.testit.services.TestItService;
import ru.testit.services.TestMethodType;

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
            testItService.startUtilMethod(TestMethodType.BEFORE_CLASS, testMethod.getConstructorOrMethod().getMethod());
            testItService.finishUtilMethod(TestMethodType.BEFORE_CLASS, null);
        }
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        for (final org.testng.ITestNGMethod testMethod : testClass.getAfterClassMethods()) {
            testItService.startUtilMethod(TestMethodType.AFTER_CLASS, testMethod.getConstructorOrMethod().getMethod());
            testItService.finishUtilMethod(TestMethodType.AFTER_CLASS, null);
        }
    }

    @Override
    public void onTestStart(ITestResult testResult) {
        testItService.startTestMethod(testResult);
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
        testItService.finishTestMethod(ItemStatus.PASSED, testResult);
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        testItService.finishTestMethod(ItemStatus.FAILED, testResult);
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        testItService.finishTestMethod(ItemStatus.SKIPPED, testResult);
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
}
