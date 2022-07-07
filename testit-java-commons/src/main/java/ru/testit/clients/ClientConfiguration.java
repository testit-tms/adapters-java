package ru.testit.clients;

import ru.testit.services.Utils;

import java.util.Properties;

public class ClientConfiguration {
    private String privateToken;
    private String projectId;
    private String url;
    private String configurationId;
    private String testRunId;

    public ClientConfiguration(Properties properties) {
        this.privateToken = String.valueOf(properties.get("PrivateToken"));
        this.projectId = String.valueOf(properties.get("ProjectId"));
        this.url = Utils.urlTrim(String.valueOf(properties.get("URL")));
        this.configurationId = String.valueOf(properties.get("ConfigurationId"));
        this.testRunId = String.valueOf(properties.get("TestRunId"));
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
}
