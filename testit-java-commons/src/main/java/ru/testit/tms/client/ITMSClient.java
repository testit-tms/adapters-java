package ru.testit.tms.client;

import ru.testit.tms.models.request.CreateTestItemRequest;
import ru.testit.tms.models.request.TestResultsRequest;
import ru.testit.tms.models.response.GetTestItemResponse;

import java.util.Collection;

public interface ITMSClient {
    void startLaunch();
    void finishLaunch(final TestResultsRequest request);
    void createTestItem(final CreateTestItemRequest createTestItemRequest);
    void updatePostItem(final CreateTestItemRequest createTestItemRequest, final String testId);
    void sendTestItems(final Collection<CreateTestItemRequest> createTestRequests);
    GetTestItemResponse getTestItem(final CreateTestItemRequest createTestItemRequest);
}
