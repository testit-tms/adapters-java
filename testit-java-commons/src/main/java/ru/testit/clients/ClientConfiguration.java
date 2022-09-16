package ru.testit.clients;

import ru.testit.properties.AppProperties;
import ru.testit.services.Utils;

import java.util.Properties;

public class ClientConfiguration {
    private String privateToken;
    private String projectId;
    private String url;
    private String configurationId;
    private String testRunId;
    private String testRunName;

    public ClientConfiguration(Properties properties) {
        this.privateToken = String.valueOf(properties.get(AppProperties.PRIVATE_TOKEN));
        this.projectId = String.valueOf(properties.get(AppProperties.PROJECT_ID));
        this.url = Utils.urlTrim(String.valueOf(properties.get(AppProperties.URL)));
        this.configurationId = String.valueOf(properties.get(AppProperties.CONFIGURATION_ID));
        this.testRunId = String.valueOf(properties.get(AppProperties.TEST_RUN_ID));
        this.testRunName = String.valueOf(properties.get(AppProperties.TEST_RUN_NAME));
    }

    public String getPrivateToken() {
        return privateToken;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getUrl() {
        return url;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public String getTestRunId() {
        return testRunId;
    }

    public void setTestRunId(String id) {
        this.testRunId = id;
    }

    public String getTestRunName() {
        return testRunName;
    }
}
