package ru.testit.writers.helpers;

import ru.testit.client.invoker.ApiException;
import ru.testit.client.model.*;
import ru.testit.clients.ITmsApiClient;
import ru.testit.clients.ClientConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkAutotestHelper {
    private final ITmsApiClient apiClient;
    private final ClientConfiguration config;
    private static final int MAX_TESTS_FOR_IMPORT = 100;
    private final List<AutoTestPostModel> autotestsForCreate;
    private final List<AutoTestPutModel> autotestsForUpdate;
    private final Map<String, List<String>> autotestLinksToWIForUpdate;
    private final List<AutoTestResultsForTestRunModel> resultsForAutotestsBeingCreated;
    private final List<AutoTestResultsForTestRunModel> resultsForAutotestsBeingUpdated;

    public BulkAutotestHelper(ITmsApiClient client, ClientConfiguration configuration) {
        config = configuration;
        apiClient = client;
        autotestsForCreate = new ArrayList<>();
        autotestsForUpdate = new ArrayList<>();
        autotestLinksToWIForUpdate = new HashMap<>();
        resultsForAutotestsBeingCreated = new ArrayList<>();
        resultsForAutotestsBeingUpdated = new ArrayList<>();
    }

    public void addForCreate(
            AutoTestPostModel createModel,
            AutoTestResultsForTestRunModel resultModel
    ) throws ApiException {
        autotestsForCreate.add(createModel);
        resultsForAutotestsBeingCreated.add(resultModel);

        if (autotestsForCreate.size() >= MAX_TESTS_FOR_IMPORT)
        {
            bulkCreate();
        }
    }

    public void addForUpdate(
            AutoTestPutModel updateModel,
            AutoTestResultsForTestRunModel resultModel,
            Map<String, List<String>> wiForUpdate) throws ApiException {
        autotestsForUpdate.add(updateModel);
        resultsForAutotestsBeingUpdated.add(resultModel);
        autotestLinksToWIForUpdate.putAll(wiForUpdate);

        if (autotestsForUpdate.size() >= MAX_TESTS_FOR_IMPORT)
        {
            bulkUpdate();
        }
    }

    public void teardown() throws ApiException {
        if (!autotestsForCreate.isEmpty())
        {
            bulkCreate();
        }

        if (!autotestsForUpdate.isEmpty())
        {
            bulkUpdate();
        }
    }

    private void bulkCreate() throws ApiException {
        apiClient.createAutoTests(autotestsForCreate);
        apiClient.sendTestResults(config.getTestRunId(), resultsForAutotestsBeingCreated);

        autotestsForCreate.clear();
        resultsForAutotestsBeingCreated.clear();
    }

    private void bulkUpdate() throws ApiException {
        apiClient.updateAutoTests(autotestsForUpdate);
        apiClient.sendTestResults(config.getTestRunId(), resultsForAutotestsBeingUpdated);

        autotestLinksToWIForUpdate.forEach((autotestId, workItemIds) ->
                {
                    try {
                        updateTestLinkToWorkItems(autotestId, workItemIds);
                    } catch (ApiException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        autotestsForUpdate.clear();
        resultsForAutotestsBeingUpdated.clear();
    }

    //TODO: delete after fix PUT/api/v2/autoTests
    private void updateTestLinkToWorkItems(String autoTestId, List<String> workItemIds) throws ApiException {
        List<WorkItemIdentifierModel> linkedWorkItems = apiClient.getWorkItemsLinkedToTest(autoTestId);

        for (WorkItemIdentifierModel linkedWorkItem : linkedWorkItems) {
            String linkedWorkItemId = linkedWorkItem.getGlobalId().toString();

            if (workItemIds.contains(linkedWorkItemId)) {
                workItemIds.remove(linkedWorkItemId);

                continue;
            }

            if (config.shouldAutomaticUpdationLinksToTestCases()) {
                apiClient.unlinkAutoTestToWorkItem(autoTestId, linkedWorkItemId);
            }
        }

        apiClient.linkAutoTestToWorkItems(autoTestId, workItemIds);
    }
}
