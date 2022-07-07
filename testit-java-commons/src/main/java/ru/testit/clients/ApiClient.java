package ru.testit.clients;

import ru.testit.invoker.ApiException;
import ru.testit.model.*;

import java.util.List;

public interface ApiClient {
    TestRunV2GetModel createTestRun(TestRunV2PostShortModel model) throws ApiException;
    void completeTestRun(String uuid) throws ApiException;
    void updateAutoTest(AutoTestPutModel model) throws ApiException;
    void createAutoTest(AutoTestPostModel model) throws ApiException;
    AutoTestModel getAutoTestByExternalId(String projectId,String externalId) throws ApiException;
    void sendTestResults(String testRunUuid, List<AutoTestResultsForTestRunModel> models) throws ApiException;
}
