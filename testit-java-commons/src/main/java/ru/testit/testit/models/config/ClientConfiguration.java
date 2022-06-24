package ru.testit.testit.models.config;

public class ClientConfiguration {
    private String privateToken;
    private String projectId;
    private String url;
    private String configurationId;
    private String testRunId;

    public ClientConfiguration(String privateToken, String projectId, String url, String configurationId, String testRunId) {
        this.privateToken = privateToken;
        this.projectId = projectId;
        this.url = url;
        this.configurationId = configurationId;
        this.testRunId = testRunId;
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
