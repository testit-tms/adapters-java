package ru.testit.services.core;

import org.slf4j.Logger;
import ru.testit.client.invoker.ApiException;
import ru.testit.client.model.TestRunV2ApiResult;
import ru.testit.clients.ClientConfiguration;
import ru.testit.clients.Converter;
import ru.testit.clients.ITmsApiClient;
import ru.testit.properties.AdapterConfig;
import ru.testit.services.HtmlEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                this.updateTestRunName();
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

    private void updateTestRunName() throws ApiException {
        String testRunName = this.clientConfiguration.getTestRunName();

        if (testRunName.isEmpty() || Objects.equals(this.clientConfiguration.getTestRunName(), "null")) {
            return;
        }

        TestRunV2ApiResult testRun = this.client.getTestRun(this.clientConfiguration.getTestRunId());

        if (testRun.getName().equals(testRunName)) {
            return;
        }

        testRun.setName(HtmlEscapeUtils.escapeHtmlTags(testRunName));
        this.client.updateTestRun(Converter.buildUpdateEmptyTestRunApiModel(testRun));
    }
}
