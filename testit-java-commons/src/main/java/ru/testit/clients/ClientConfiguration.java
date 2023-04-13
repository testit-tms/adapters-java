package ru.testit.clients;

import ru.testit.properties.AppProperties;
import ru.testit.services.Utils;

import java.io.Serializable;
import java.util.Properties;

public class ClientConfiguration implements Serializable {
    private String privateToken;
    private String projectId;
    private String url;
    private String configurationId;
    private String testRunId;
    private String testRunName;
    private Boolean certValidation;

    public ClientConfiguration(Properties properties) {
        this.privateToken = String.valueOf(properties.get(AppProperties.PRIVATE_TOKEN));
        this.projectId = String.valueOf(properties.get(AppProperties.PROJECT_ID));
        this.url = Utils.urlTrim(String.valueOf(properties.get(AppProperties.URL)));
        this.configurationId = String.valueOf(properties.get(AppProperties.CONFIGURATION_ID));
        this.testRunId = String.valueOf(properties.get(AppProperties.TEST_RUN_ID));
        this.testRunName = String.valueOf(properties.get(AppProperties.TEST_RUN_NAME));

        String validationCert = String.valueOf(
                properties.get(AppProperties.CERT_VALIDATION));
        if (validationCert.equals("null")) {
            validationCert = "true";
        }
        this.certValidation = Boolean.parseBoolean(validationCert);
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

    public Boolean getCertValidation() {
        return certValidation;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ClientConfiguration {\n");
        sb.append("    url: ").append(Utils.toIndentedString(this.url)).append("\n");
        sb.append("    privateToken: **********").append("\n");
        sb.append("    projectId: ").append(Utils.toIndentedString(this.projectId)).append("\n");
        sb.append("    configurationId: ").append(Utils.toIndentedString(this.configurationId)).append("\n");
        sb.append("    testRunId: ").append(Utils.toIndentedString(this.testRunId)).append("\n");
        sb.append("    testRunName: ").append(Utils.toIndentedString(this.testRunName)).append("\n");
        sb.append("    certValidation: ").append(Utils.toIndentedString(this.certValidation)).append("\n");
        sb.append("}");

        return sb.toString();
    }
}
