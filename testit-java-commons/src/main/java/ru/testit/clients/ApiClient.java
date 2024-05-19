package ru.testit.clients;

import ru.testit.client.invoker.ApiException;
import ru.testit.client.model.*;

import java.util.List;
import java.util.UUID;

public interface ApiClient {
    TestRunV2GetModel createTestRun() throws ApiException;
    TestRunV2GetModel getTestRun(String uuid) throws ApiException;
    void completeTestRun(String uuid) throws ApiException;
    void updateAutoTest(AutoTestPutModel model) throws ApiException;
    String createAutoTest(AutoTestPostModel model) throws ApiException;
    AutoTestModel getAutoTestByExternalId(String externalId) throws ApiException;
    boolean tryLinkAutoTestToWorkItem(String id, Iterable<String> workItemId);
    List<UUID> sendTestResults(String testRunUuid, List<AutoTestResultsForTestRunModel> models) throws ApiException;
    String addAttachment(String path) throws ApiException;
    List<String> getTestFromTestRun(String testRunUuid, String configurationId) throws ApiException;
    TestResultModel getTestResult(UUID uuid) throws ApiException;
    void updateTestResult(UUID uuid, TestResultUpdateModel model) throws ApiException;
}
