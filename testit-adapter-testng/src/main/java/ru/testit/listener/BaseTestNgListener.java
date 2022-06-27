package ru.testit.listener;

import org.testng.*;
import ru.testit.services.ItemStatus;
import ru.testit.models.TestMethod;
import ru.testit.services.TMSService;
import ru.testit.services.TestMethodType;

public class BaseTestNgListener implements IExecutionListener, ITestListener, IClassListener {
    private final TMSService tmsService;

    public BaseTestNgListener() {
        tmsService = new TMSService();
    }

    @Override
    public void onExecutionStart() {
        tmsService.startLaunch();
    }

    @Override
    public void onExecutionFinish() {
        tmsService.finishLaunch();
    }

    @Override
    public void onBeforeClass(ITestClass testClass) {
        for (final org.testng.ITestNGMethod method : testClass.getBeforeClassMethods()) {
            TestMethod testMethod = Converter.ConvertMethod(method.getConstructorOrMethod().getMethod());

            tmsService.startUtilMethod(TestMethodType.BEFORE_CLASS, testMethod);
            tmsService.finishUtilMethod(TestMethodType.BEFORE_CLASS, null);
        }
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        for (final org.testng.ITestNGMethod method : testClass.getAfterClassMethods()) {
            TestMethod testMethod = Converter.ConvertMethod(method.getConstructorOrMethod().getMethod());

            tmsService.startUtilMethod(TestMethodType.AFTER_CLASS, testMethod);
            tmsService.finishUtilMethod(TestMethodType.AFTER_CLASS, null);
        }
    }

    @Override
    public void onTestStart(ITestResult testResult) {
        TestMethod method = Converter.ConvertTestResult(testResult);

        tmsService.startTestMethod(method);
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
        tmsService.finishTestMethod(status, method);
    }
}
