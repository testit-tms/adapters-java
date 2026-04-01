package ru.testit.writers.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.client.invoker.ApiException;
import ru.testit.client.model.*;
import ru.testit.clients.ITmsApiClient;
import ru.testit.clients.ClientConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        List<AutoTestCreateApiModel> uniqueCreates = dedupeCreatesByExternalId(autotestsForCreate);
        if (uniqueCreates.size() < n) {
            LOGGER.info(
                    "Bulk createAutoTests: {} unique externalId(s), {} duplicate row(s) omitted for API (TMS requires unique externalIds per batch)",
                    uniqueCreates.size(),
                    n - uniqueCreates.size()
            );
        }
        LOGGER.info("Bulk createAutoTests + sendTestResults: {} autotest(s) in request, {} result row(s)", uniqueCreates.size(), n);
        apiClient.createAutoTests(uniqueCreates);
        apiClient.sendTestResults(config.getTestRunId(), resultsForAutotestsBeingCreated);

        autotestsForCreate.clear();
        resultsForAutotestsBeingCreated.clear();
    }

    private void bulkUpdate() throws ApiException {
        int n = autotestsForUpdate.size();
        List<AutoTestUpdateApiModel> uniqueUpdates = dedupeUpdatesByExternalId(autotestsForUpdate);
        if (uniqueUpdates.size() < n) {
            LOGGER.info(
                    "Bulk updateAutoTests: {} unique externalId(s), {} duplicate row(s) omitted for API (TMS requires unique externalIds per batch)",
                    uniqueUpdates.size(),
                    n - uniqueUpdates.size()
            );
        }
        LOGGER.info("Bulk updateAutoTests + sendTestResults: {} autotest(s) in request, {} result row(s)", uniqueUpdates.size(), n);
        apiClient.updateAutoTests(uniqueUpdates);
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

    /**
     * TMS bulk update rejects duplicate {@code externalId} in one request; parameterized / outline rows often share it.
     * Last model wins per id; {@link #resultsForAutotestsBeingUpdated} stays 1:1 with all executions.
     */
    private static List<AutoTestUpdateApiModel> dedupeUpdatesByExternalId(List<AutoTestUpdateApiModel> models) {
        Map<String, AutoTestUpdateApiModel> byKey = new LinkedHashMap<>();
        for (int i = 0; i < models.size(); i++) {
            AutoTestUpdateApiModel m = models.get(i);
            String ext = m.getExternalId();
            String key = (ext != null && !ext.isEmpty()) ? ext : "__noExternalId__" + i;
            byKey.put(key, m);
        }
        return new ArrayList<>(byKey.values());
    }

    private static List<AutoTestCreateApiModel> dedupeCreatesByExternalId(List<AutoTestCreateApiModel> models) {
        Map<String, AutoTestCreateApiModel> byKey = new LinkedHashMap<>();
        for (int i = 0; i < models.size(); i++) {
            AutoTestCreateApiModel m = models.get(i);
            String ext = m.getExternalId();
            String key = (ext != null && !ext.isEmpty()) ? ext : "__noExternalId__" + i;
            byKey.put(key, m);
        }
        return new ArrayList<>(byKey.values());
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
