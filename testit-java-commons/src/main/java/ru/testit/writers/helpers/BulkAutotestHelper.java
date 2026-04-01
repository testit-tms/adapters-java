package ru.testit.writers.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.client.invoker.ApiException;
import ru.testit.client.model.*;
import ru.testit.clients.ITmsApiClient;
import ru.testit.clients.ClientConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkAutotestHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkAutotestHelper.class);
    private final ITmsApiClient apiClient;
    private final ClientConfiguration config;
    private static final int MAX_TESTS_FOR_IMPORT = 100;
    private final List<AutoTestCreateApiModel> autotestsForCreate;
    private final List<AutoTestUpdateApiModel> autotestsForUpdate;
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
            AutoTestCreateApiModel createModel,
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
            AutoTestUpdateApiModel updateModel,
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
        int n = autotestsForCreate.size();
        LOGGER.info("Sequential createAutoTest ({} row(s)) then sendTestResults", n);
        for (AutoTestCreateApiModel model : autotestsForCreate) {
            apiClient.createAutoTest(model);
        }
        apiClient.sendTestResults(config.getTestRunId(), resultsForAutotestsBeingCreated);

        autotestsForCreate.clear();
        resultsForAutotestsBeingCreated.clear();
    }

    private void bulkUpdate() throws ApiException {
        int n = autotestsForUpdate.size();
        // Sequential updates: TMS rejects bulk updateMultiple under concurrency (DbUpdateConcurrencyException).
        LOGGER.info("Sequential updateAutoTest ({} row(s)) then sendTestResults", n);
        for (AutoTestUpdateApiModel model : autotestsForUpdate) {
            apiClient.updateAutoTest(model);
        }
        apiClient.sendTestResults(config.getTestRunId(), resultsForAutotestsBeingUpdated);

        Map<String, List<String>> wiBatch = new HashMap<>(autotestLinksToWIForUpdate);
        autotestLinksToWIForUpdate.clear();

        wiBatch.forEach((autotestId, workItemIds) ->
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
        List<String> wi = new ArrayList<>(workItemIds);
        List<AutoTestWorkItemIdentifierApiResult> linkedWorkItems = apiClient.getWorkItemsLinkedToTest(autoTestId);

        for (AutoTestWorkItemIdentifierApiResult linkedWorkItem : linkedWorkItems) {
            String linkedWorkItemId = linkedWorkItem.getGlobalId().toString();

            if (wi.contains(linkedWorkItemId)) {
                wi.remove(linkedWorkItemId);

                continue;
            }

            if (config.shouldAutomaticUpdationLinksToTestCases()) {
                apiClient.unlinkAutoTestToWorkItem(autoTestId, linkedWorkItemId);
            }
        }

        apiClient.linkAutoTestToWorkItems(autoTestId, wi);
    }
}
