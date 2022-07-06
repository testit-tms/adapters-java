package ru.testit.tms.client;

import ru.testit.tms.models.request.CreateTestItemRequest;
import ru.testit.tms.models.request.InnerItem;
import ru.testit.tms.models.request.TestResultsRequest;
import ru.testit.tms.models.response.GetTestItemResponse;

import java.util.Collection;
import java.util.List;

public interface ITMSClient {
    String startLaunch();
    void finishLaunch(final TestResultsRequest request);
    void createTestItem(final CreateTestItemRequest createTestItemRequest);
    void updatePostItem(final CreateTestItemRequest createTestItemRequest, final String testId);
    void sendTestItems(final Collection<CreateTestItemRequest> createTestRequests);
    void sendTestItem(final CreateTestItemRequest createTestRequest);
    GetTestItemResponse getTestItem(final String externalId);
    void sendCompleteTestRun();
    List<String> sendTestResult(final TestResultsRequest request);
}
