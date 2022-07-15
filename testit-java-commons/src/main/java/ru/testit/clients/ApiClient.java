package ru.testit.clients;

import ru.testit.client.invoker.ApiException;
import ru.testit.client.model.*;

import java.util.List;

public interface ApiClient {
    TestRunV2GetModel createTestRun(TestRunV2PostShortModel model) throws ApiException;
    TestRunV2GetModel getTestRun(String uuid) throws ApiException;
    void completeTestRun(String uuid) throws ApiException;
    void updateAutoTest(AutoTestPutModel model) throws ApiException;
    String createAutoTest(AutoTestPostModel model) throws ApiException;
    AutoTestModel getAutoTestByExternalId(String projectId,String externalId) throws ApiException;
    void linkAutoTestToWorkItem(String id, String workItemId) throws ApiException;
    void sendTestResults(String testRunUuid, List<AutoTestResultsForTestRunModel> models) throws ApiException;
}
