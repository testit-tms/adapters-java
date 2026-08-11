package ru.testit.clients;

import ru.testit.models.LinkItem;
import ru.testit.properties.AppProperties;
import ru.testit.properties.TestRunMetaParser;
import ru.testit.services.Utils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class ClientConfiguration implements Serializable {
    private String privateToken;
    private String projectId;
    private String url;
    private String configurationId;
    private String testRunId;
    private String testRunName;
    private List<String> testRunTags;
    private List<LinkItem> testRunLinks;
    private Boolean certValidation;
    private boolean automaticUpdationLinksToTestCases;
    private boolean tmsImportRealtime;

    public ClientConfiguration(Properties properties) {
        this.privateToken = String.valueOf(properties.get(AppProperties.PRIVATE_TOKEN));
        this.projectId = String.valueOf(properties.get(AppProperties.PROJECT_ID));
        this.url = Utils.urlTrim(String.valueOf(properties.get(AppProperties.URL)));
        this.configurationId = String.valueOf(properties.get(AppProperties.CONFIGURATION_ID));
        this.testRunId = String.valueOf(properties.get(AppProperties.TEST_RUN_ID));
        this.testRunName = String.valueOf(properties.get(AppProperties.TEST_RUN_NAME));
        this.testRunTags = TestRunMetaParser.parseTags(stringOrNull(properties.get(AppProperties.TEST_RUN_TAGS)));
        this.testRunLinks = TestRunMetaParser.parseLinks(stringOrNull(properties.get(AppProperties.TEST_RUN_LINKS)));

        String validationCert = String.valueOf(
                properties.get(AppProperties.CERT_VALIDATION));
        if (validationCert.equals("null")) {
            validationCert = "true";
        }

        try {
            String automaticUpdationLinksToTestCasesValue = String.valueOf(properties.get(AppProperties.AUTOMATIC_UPDATION_LINKS_TO_TEST_CASES));
            this.automaticUpdationLinksToTestCases = Objects.equals(automaticUpdationLinksToTestCasesValue, "true");
        } catch (NullPointerException ignored) {
            this.automaticUpdationLinksToTestCases = false;
        }

        try {
            String importRealtime = String.valueOf(properties.get(AppProperties.TMS_IMPORT_REALTIME));
            this.tmsImportRealtime = Objects.equals(importRealtime, "true");
        } catch (NullPointerException ignored) {
            this.tmsImportRealtime = false;
        }

        this.certValidation = Boolean.parseBoolean(validationCert);
    }

    private static String stringOrNull(Object value) {
        if (value == null) {
            return null;
        }
        String s = String.valueOf(value);
        return "null".equals(s) ? null : s;
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

    public List<String> getTestRunTags() {
        return testRunTags == null ? Collections.emptyList() : testRunTags;
    }

    public List<LinkItem> getTestRunLinks() {
        return testRunLinks == null ? Collections.emptyList() : testRunLinks;
    }

    public boolean hasTestRunMeta() {
        return !getTestRunTags().isEmpty() || !getTestRunLinks().isEmpty();
    }

    public Boolean getCertValidation() {
        return certValidation;
    }

    public boolean shouldAutomaticUpdationLinksToTestCases() {
        return automaticUpdationLinksToTestCases;
    }

    public boolean shouldImportRealtime() {
        return tmsImportRealtime;
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
        sb.append("    testRunTags: ").append(Utils.toIndentedString(this.testRunTags)).append("\n");
        sb.append("    testRunLinks: ").append(Utils.toIndentedString(this.testRunLinks)).append("\n");
        sb.append("    certValidation: ").append(Utils.toIndentedString(this.certValidation)).append("\n");
        sb.append("    automaticUpdationLinksToTestCases: ").append(Utils.toIndentedString(this.automaticUpdationLinksToTestCases)).append("\n");
        sb.append("    tmsImportRealtime: ").append(Utils.toIndentedString(this.tmsImportRealtime)).append("\n");
        sb.append("}");

        return sb.toString();
    }
}
