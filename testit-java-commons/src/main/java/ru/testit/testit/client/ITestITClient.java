package ru.testit.testit.client;

import ru.testit.testit.models.request.CreateTestItemRequest;
import ru.testit.testit.models.request.TestResultsRequest;
import ru.testit.testit.models.response.GetTestItemResponse;

import java.util.Collection;

public interface ITestITClient {
    void startLaunch();
    void finishLaunch(final TestResultsRequest request);
    void createTestItem(final CreateTestItemRequest createTestItemRequest);
    void updatePostItem(final CreateTestItemRequest createTestItemRequest, final String testId);
    void sendTestItems(final Collection<CreateTestItemRequest> createTestRequests);
    GetTestItemResponse getTestItem(final CreateTestItemRequest createTestItemRequest);
}
