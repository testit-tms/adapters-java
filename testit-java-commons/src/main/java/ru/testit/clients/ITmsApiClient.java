package ru.testit.clients;

import ru.testit.adaptersapi.invoker.ApiException;
import ru.testit.adaptersapi.model.*;

import java.util.List;
import java.util.UUID;

public interface ITmsApiClient {
    TestRunApiResult createTestRun() throws ApiException;
    TestRunApiResult getTestRun(String uuid) throws ApiException;
    void updateTestRun(UpdateEmptyTestRunApiModel testRun) throws ApiException;
    void completeTestRun(String uuid) throws ApiException;
    void updateAutoTest(AutoTestUpdateApiModel model) throws ApiException;
    String createAutoTest(AutoTestCreateApiModel model) throws ApiException;
    void updateAutoTests(List<AutoTestUpdateApiModel> models) throws ApiException;
    List<AutoTestApiResult> createAutoTests(List<AutoTestCreateApiModel> models) throws ApiException;
    List<UUID> getWorkItemUuidsByIds(Iterable<String> workItemIds);
    AutoTestApiResult getAutoTestByExternalId(String externalId) throws ApiException;
    void linkAutoTestToWorkItems(String id, Iterable<String> workItemIds);
    void unlinkAutoTestToWorkItem(String id, String workItemId);
    List<AutoTestWorkItemIdentifierApiResult> getWorkItemsLinkedToTest(String id) throws ApiException;
    List<UUID> sendTestResults(String testRunUuid, List<AutoTestResultsForTestRunModel> models) throws ApiException;
    String addAttachment(String path) throws ApiException;
    List<String> getTestFromTestRun(String testRunUuid, String configurationId) throws ApiException;
    List<String> getAutotestExternalIdsFromTestRun() throws ApiException;
    /** Existing InProgress result for externalId; prefers one bound to a test point. */
    UUID findInProgressTestResultId(String externalId) throws ApiException;
    TestResultResponse getTestResult(UUID uuid) throws ApiException;
    void updateTestResult(UUID uuid, TestResultUpdateRequest model) throws ApiException;
}
