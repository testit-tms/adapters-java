package ru.testit.listener;

import org.testng.ITestResult;
import ru.testit.models.TestMethod;
import java.lang.reflect.Method;

public class Converter {
    public static TestMethod ConvertMethod(Method method) {
        TestMethod testMethod = new TestMethod();

        testMethod.setExternalId(Utils.extractExternalID(method));
        testMethod.setDisplayName(Utils.extractDisplayName(method));
        testMethod.setClassName(method.getDeclaringClass().getSimpleName());
        testMethod.setSpaceName((method.getDeclaringClass().getPackage() == null)
                ? null : method.getDeclaringClass().getPackage().getName());
        testMethod.setWorkItemId(Utils.extractWorkItemId(method));
//        testMethod.setLinks(Utils.extractLinks(method));
//        testMethod.setLabels(Utils.extractLabels(method));
        testMethod.setTitle(Utils.extractTitle(method));
        testMethod.setDescription(Utils.extractDescription(method));

        return testMethod;
    }

    public static TestMethod ConvertTestResult(ITestResult testResult) {
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
        TestMethod testMethod = Converter.ConvertMethod(method);
        testMethod.setThrowable(testResult.getThrowable());

        return testMethod;
    }
}
