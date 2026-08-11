package ru.testit.services.core;

import org.slf4j.Logger;
import ru.testit.adaptersapi.invoker.ApiException;
import ru.testit.adaptersapi.model.LinkApiResult;
import ru.testit.adaptersapi.model.TestRunApiResult;
import ru.testit.adaptersapi.model.UpdateEmptyTestRunApiModel;
import ru.testit.adaptersapi.model.UpdateLinkApiModel;
import ru.testit.clients.ClientConfiguration;
import ru.testit.clients.Converter;
import ru.testit.clients.ITmsApiClient;
import ru.testit.models.LinkItem;
import ru.testit.properties.AdapterConfig;
import ru.testit.services.HtmlEscapeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AdapterStartupHelper {

    private final AdapterConfig adapterConfig;
    private final ClientConfiguration clientConfiguration;
    private final ITmsApiClient client;
    private final Logger logger;

    public AdapterStartupHelper(
            AdapterConfig adapterConfig,
            ClientConfiguration clientConfiguration,
            ITmsApiClient client,
            Logger logger
    ) {
        this.adapterConfig = adapterConfig;
        this.clientConfiguration = clientConfiguration;
        this.client = client;
        this.logger = logger;
    }

    public void startTests() {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        logger.debug("Start launch");

        synchronized (this.clientConfiguration) {
            if (Objects.equals(this.clientConfiguration.getTestRunId(), "null")) {
                return;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Test run is exist.");
            }

            try {
                this.updateTestRunMetadata();
            } catch (ApiException e) {
                logger.error("Can not update the launch: ".concat(e.getMessage()));
            }
        }
    }

    public List<String> getTestFromTestRun() {
        if (adapterConfig.shouldEnableTmsIntegration()) {
            try {
                List<String> externalIds = client.getAutotestExternalIdsFromTestRun();

                if (logger.isDebugEnabled()) {
                    logger.debug("List of tests from test run: {}", externalIds);
                }

                return externalIds;
            } catch (ApiException e) {
                logger.error("Could not get tests from test run", e);
            }
        }
        return new ArrayList<>();
    }

    /**
     * Early update: name, tags and links (merge) so CI job URL is visible while run is In progress.
     */
    private void updateTestRunMetadata() throws ApiException {
        String testRunName = this.clientConfiguration.getTestRunName();
        boolean rename = testRunName != null
                && !testRunName.isEmpty()
                && !Objects.equals(testRunName, "null");
        boolean meta = clientConfiguration.hasTestRunMeta();

        if (!rename && !meta) {
            return;
        }

        TestRunApiResult testRun = this.client.getTestRun(this.clientConfiguration.getTestRunId());
        UpdateEmptyTestRunApiModel model = Converter.buildUpdateEmptyTestRunApiModel(testRun);
        boolean changed = false;

        if (rename) {
            String escaped = HtmlEscapeUtils.escapeHtmlTags(testRunName);
            if (!Objects.equals(testRun.getName(), escaped)) {
                model.setName(escaped);
                changed = true;
            }
        }

        if (meta) {
            if (mergeTags(model, testRun.getTags(), clientConfiguration.getTestRunTags())) {
                changed = true;
            }
            List<LinkApiResult> existingLinks = testRun.getLinks() == null
                    ? Collections.emptyList()
                    : testRun.getLinks();
            if (mergeLinks(
                    model,
                    Converter.buildUpdateLinkApiModels(existingLinks),
                    clientConfiguration.getTestRunLinks())) {
                changed = true;
            }
        }

        if (!changed) {
            return;
        }

        this.client.updateTestRun(model);
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Updated test run metadata: id={}, tags={}, links={}",
                    testRun.getId(),
                    model.getTags(),
                    model.getLinks() != null ? model.getLinks().size() : 0
            );
        }
    }

    private static boolean mergeTags(
            UpdateEmptyTestRunApiModel model,
            List<String> existing,
            List<String> configured
    ) {
        if (configured == null || configured.isEmpty()) {
            return false;
        }
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        if (existing != null) {
            tags.addAll(existing);
        }
        boolean added = false;
        for (String tag : configured) {
            if (tags.add(tag)) {
                added = true;
            }
        }
        if (!added) {
            return false;
        }
        model.setTags(new ArrayList<>(tags));
        return true;
    }

    private static boolean mergeLinks(
            UpdateEmptyTestRunApiModel model,
            List<UpdateLinkApiModel> existing,
            List<LinkItem> configured
    ) {
        if (configured == null || configured.isEmpty()) {
            return false;
        }
        List<UpdateLinkApiModel> links = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
        Set<String> urls = new LinkedHashSet<>();
        for (UpdateLinkApiModel link : links) {
            if (link.getUrl() != null) {
                urls.add(link.getUrl());
            }
        }
        boolean added = false;
        for (UpdateLinkApiModel link : Converter.convertTestRunUpdateLinks(configured)) {
            if (link.getUrl() != null && urls.add(link.getUrl())) {
                links.add(link);
                added = true;
            }
        }
        if (!added) {
            return false;
        }
        model.setLinks(links);
        return true;
    }
}
