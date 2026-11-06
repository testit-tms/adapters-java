package ru.testit.writers.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.adaptersapi.invoker.ApiException;
import ru.testit.adaptersapi.model.*;
import ru.testit.clients.ITmsApiClient;
import ru.testit.clients.ClientConfiguration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class BulkAutotestHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkAutotestHelper.class);
    private final ITmsApiClient apiClient;
    private final ClientConfiguration config;
    private static final int MAX_TESTS_FOR_IMPORT = 100;
    /** Cap parallel TMS autoTest create/update calls to reduce DbUpdateConcurrencyException noise. */
    private static final int MAX_PARALLEL_AUTO_TEST_OPS = 16;
    private final List<AutoTestCreateApiModel> autotestsForCreate;
    private final List<AutoTestUpdateApiModel> autotestsForUpdate;
    /** externalId -> work item globalIds to link after create. */
    private final Map<String, List<String>> autotestLinksToWIForCreate;
    /** autoTest UUID -> work item globalIds to link after update / result-only flush. */
    private final Map<String, List<String>> autotestLinksToWIForUpdate;
    private final List<AutoTestResultsForTestRunModel> resultsForAutotestsBeingCreated;
    private final List<AutoTestResultsForTestRunModel> resultsForAutotestsBeingUpdated;

    public BulkAutotestHelper(ITmsApiClient client, ClientConfiguration configuration) {
        config = configuration;
        apiClient = client;
        autotestsForCreate = new ArrayList<>();
        autotestsForUpdate = new ArrayList<>();
        autotestLinksToWIForCreate = new HashMap<>();
        autotestLinksToWIForUpdate = new HashMap<>();
        resultsForAutotestsBeingCreated = new ArrayList<>();
        resultsForAutotestsBeingUpdated = new ArrayList<>();
    }

    public void addForCreate(
            AutoTestCreateApiModel createModel,
            AutoTestResultsForTestRunModel resultModel,
            List<String> workItemIds
    ) throws ApiException {
        autotestsForCreate.add(createModel);
        resultsForAutotestsBeingCreated.add(resultModel);
        if (workItemIds != null && !workItemIds.isEmpty()) {
            autotestLinksToWIForCreate.put(
                    externalIdKey(createModel.getExternalId()),
                    new ArrayList<>(workItemIds)
            );
        }

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
        queueWorkItemLinks(wiForUpdate);

        if (autotestsForUpdate.size() >= MAX_TESTS_FOR_IMPORT)
        {
            bulkUpdate();
        }
    }

    /** Autotest metadata unchanged; only {@link #sendTestResults} rows (after create/update batches). */
    public void addTestRunResultOnly(AutoTestResultsForTestRunModel resultModel) throws ApiException {
        resultsForAutotestsBeingUpdated.add(resultModel);
        if (resultsForAutotestsBeingUpdated.size() >= MAX_TESTS_FOR_IMPORT
                && autotestsForCreate.isEmpty()
                && autotestsForUpdate.isEmpty()) {
            flushPendingResultsOnly();
        }
    }

    /**
     * Queue WI links by autotest UUID (e.g. when metadata PUT is skipped but links still need reconcile).
     */
    public void queueWorkItemLinks(String autoTestUuid, List<String> workItemIds) {
        if (autoTestUuid == null || workItemIds == null || workItemIds.isEmpty()) {
            return;
        }
        autotestLinksToWIForUpdate.put(autoTestUuid, new ArrayList<>(workItemIds));
    }

    private void queueWorkItemLinks(Map<String, List<String>> wiForUpdate) {
        if (wiForUpdate == null || wiForUpdate.isEmpty()) {
            return;
        }
        for (Map.Entry<String, List<String>> e : wiForUpdate.entrySet()) {
            queueWorkItemLinks(e.getKey(), e.getValue());
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

        if (!resultsForAutotestsBeingUpdated.isEmpty()) {
            flushPendingResultsOnly();
        }

        flushWorkItemLinks();
    }

    private void flushPendingResultsOnly() throws ApiException {
        int n = resultsForAutotestsBeingUpdated.size();
        if (n == 0) {
            return;
        }
        List<List<AutoTestResultsForTestRunModel>> batches = partitionResultsUniqueAutotestPerBatch(resultsForAutotestsBeingUpdated);
        LOGGER.info("sendTestResults (autotest unchanged): {} batch(es), {} result row(s)", batches.size(), n);
        for (List<AutoTestResultsForTestRunModel> batch : batches) {
            retrySend(batch);
        }
        resultsForAutotestsBeingUpdated.clear();
    }

    private void flushWorkItemLinks() throws ApiException {
        if (autotestLinksToWIForUpdate.isEmpty()) {
            return;
        }
        Map<String, List<String>> wiBatch = new HashMap<>(autotestLinksToWIForUpdate);
        autotestLinksToWIForUpdate.clear();
        for (Map.Entry<String, List<String>> e : wiBatch.entrySet()) {
            retryApi(() -> updateTestLinkToWorkItems(e.getKey(), e.getValue()));
        }
    }

    private void bulkCreate() throws ApiException {
        int n = autotestsForCreate.size();
        List<AutoTestCreateApiModel> unique = dedupeCreatesLastWins(autotestsForCreate);
        LOGGER.info(
                "Parallel createAutoTest ({} unique / {} row(s)), then sendTestResults batched (no duplicate autotest per request)",
                unique.size(),
                n
        );
        Map<String, String> createdIdsByExternalId = new ConcurrentHashMap<>();
        parallelAutoTestOps(unique, m -> () -> {
            String id = apiClient.createAutoTest(m);
            if (id != null && !id.isEmpty()) {
                createdIdsByExternalId.put(externalIdKey(m.getExternalId()), id);
            }
        });
        List<List<AutoTestResultsForTestRunModel>> batches = partitionResultsUniqueAutotestPerBatch(resultsForAutotestsBeingCreated);
        LOGGER.info("sendTestResults: {} batch(es), {} result row(s)", batches.size(), n);
        for (List<AutoTestResultsForTestRunModel> batch : batches) {
            retrySend(batch);
        }

        Map<String, List<String>> wiForCreate = new HashMap<>(autotestLinksToWIForCreate);
        autotestLinksToWIForCreate.clear();
        for (Map.Entry<String, List<String>> e : wiForCreate.entrySet()) {
            String uuid = createdIdsByExternalId.get(e.getKey());
            if (uuid == null || e.getValue() == null || e.getValue().isEmpty()) {
                continue;
            }
            retryApi(() -> updateTestLinkToWorkItems(uuid, e.getValue()));
        }

        autotestsForCreate.clear();
        resultsForAutotestsBeingCreated.clear();
    }

    private void bulkUpdate() throws ApiException {
        int n = autotestsForUpdate.size();
        List<AutoTestUpdateApiModel> unique = dedupeUpdatesLastWins(autotestsForUpdate);
        LOGGER.info(
                "Parallel updateAutoTest ({} unique / {} row(s)), then sendTestResults batched (no duplicate autotest per request)",
                unique.size(),
                n
        );
        parallelAutoTestOps(unique, m -> () -> apiClient.updateAutoTest(m));
        List<List<AutoTestResultsForTestRunModel>> batches = partitionResultsUniqueAutotestPerBatch(resultsForAutotestsBeingUpdated);
        LOGGER.info("sendTestResults: {} batch(es), {} result row(s)", batches.size(), n);
        for (List<AutoTestResultsForTestRunModel> batch : batches) {
            retrySend(batch);
        }

        autotestsForUpdate.clear();
        resultsForAutotestsBeingUpdated.clear();
    }

    /**
     * TMS: do not put two results for the same autotest in one {@code sendTestResults} call.
     * Groups by {@link AutoTestResultsForTestRunModel#getAutoTestExternalId()}, then round-robin so each batch has at most one row per autotest.
     */
    static List<List<AutoTestResultsForTestRunModel>> partitionResultsUniqueAutotestPerBatch(
            List<AutoTestResultsForTestRunModel> all
    ) {
        Map<String, ArrayDeque<AutoTestResultsForTestRunModel>> byAutotest = new LinkedHashMap<>();
        for (AutoTestResultsForTestRunModel m : all) {
            String key = autotestKey(m);
            byAutotest.computeIfAbsent(key, k -> new ArrayDeque<>()).addLast(m);
        }
        List<List<AutoTestResultsForTestRunModel>> batches = new ArrayList<>();
        while (true) {
            List<AutoTestResultsForTestRunModel> batch = new ArrayList<>();
            for (ArrayDeque<AutoTestResultsForTestRunModel> q : byAutotest.values()) {
                if (!q.isEmpty()) {
                    batch.add(q.pollFirst());
                }
            }
            if (batch.isEmpty()) {
                break;
            }
            batches.add(batch);
        }
        return batches;
    }

    private static String externalIdKey(String id) {
        return id != null && !id.isEmpty() ? id : "\0null";
    }

    private static String autotestKey(AutoTestResultsForTestRunModel m) {
        return externalIdKey(m.getAutoTestExternalId());
    }

    /** One API call per externalId; last row wins (same metadata as sequential tail). */
    private static List<AutoTestCreateApiModel> dedupeCreatesLastWins(List<AutoTestCreateApiModel> list) {
        Map<String, AutoTestCreateApiModel> m = new LinkedHashMap<>();
        for (AutoTestCreateApiModel x : list) {
            m.put(externalIdKey(x.getExternalId()), x);
        }
        return new ArrayList<>(m.values());
    }

    private static List<AutoTestUpdateApiModel> dedupeUpdatesLastWins(List<AutoTestUpdateApiModel> list) {
        Map<String, AutoTestUpdateApiModel> m = new LinkedHashMap<>();
        for (AutoTestUpdateApiModel x : list) {
            m.put(externalIdKey(x.getExternalId()), x);
        }
        return new ArrayList<>(m.values());
    }

    private <T> void parallelAutoTestOps(List<T> unique, Function<T, ApiVoid> toCall) throws ApiException {
        int u = unique.size();
        if (u == 0) {
            return;
        }
        int poolSize = Math.min(MAX_PARALLEL_AUTO_TEST_OPS, u);
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        List<Future<?>> futures = new ArrayList<>(u);
        try {
            for (T item : unique) {
                ApiVoid call = toCall.apply(item);
                futures.add(pool.submit(() -> {
                    retryApi(call);
                    return null;
                }));
            }
            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ApiException(e);
                } catch (ExecutionException e) {
                    Throwable c = e.getCause();
                    if (c instanceof ApiException) {
                        throw (ApiException) c;
                    }
                    throw new ApiException(e);
                }
            }
        } finally {
            pool.shutdownNow();
        }
    }

    private static boolean isDbConcurrency(ApiException e) {
        String m = e.getMessage();
        return m != null && m.contains("DbUpdateConcurrencyException");
    }

    private static void sleepBackoff(int attempt) throws ApiException {
        try {
            Thread.sleep(80L * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ApiException(ie);
        }
    }

    @FunctionalInterface
    private interface ApiVoid {
        void run() throws ApiException;
    }

    private void retryApi(ApiVoid call) throws ApiException {
        final int maxAttempts = 6;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                call.run();
                return;
            } catch (ApiException e) {
                if (attempt < maxAttempts && isDbConcurrency(e)) {
                    LOGGER.warn("TMS API concurrency, retry {}/{}: {}", attempt, maxAttempts, e.getMessage());
                    sleepBackoff(attempt);
                } else {
                    throw e;
                }
            }
        }
    }

    private void retrySend(List<AutoTestResultsForTestRunModel> models) throws ApiException {
        final int maxAttempts = 6;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                apiClient.sendTestResults(config.getTestRunId(), models);
                return;
            } catch (ApiException e) {
                if (attempt < maxAttempts && isDbConcurrency(e)) {
                    LOGGER.warn("sendTestResults concurrency, retry {}/{}: {}", attempt, maxAttempts, e.getMessage());
                    sleepBackoff(attempt);
                } else {
                    throw e;
                }
            }
        }
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
