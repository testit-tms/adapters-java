package ru.testit.properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class AppProperties {
    public static final String URL = "url";
    public static final String PRIVATE_TOKEN = "privateToken";
    public static final String PROJECT_ID = "projectId";
    public static final String CONFIGURATION_ID = "configurationId";
    public static final String TEST_RUN_ID = "testRunId";
    public static final String TEST_RUN_NAME = "testRunName";
    public static final String ADAPTER_MODE = "adapterMode";
    public static final String AUTOMATIC_CREATION_TEST_CASES = "automaticCreationTestCases";
    public static final String CERT_VALIDATION = "certValidation";
    public static final String TMS_INTEGRATION = "testIt";

    private static final String ENV_PREFIX = "TMS";
    private static final String CONFIG_FILE = "CONFIG_FILE";
    private static final String PROPERTIES_FILE = "testit.properties";
    private static final Logger log = LoggerFactory.getLogger(AppProperties.class);

    public AppProperties() {
    }

    public static Properties loadProperties() {
        String configFile = getConfigFileName();

        Properties properties = new Properties();
        properties.put(ADAPTER_MODE, "0");
        properties.put(AUTOMATIC_CREATION_TEST_CASES, "false");
        properties.put(CERT_VALIDATION, "true");
        properties.put(TMS_INTEGRATION, "true");

        loadPropertiesFrom(Thread.currentThread().getContextClassLoader(), properties, configFile);
        loadPropertiesFrom(ClassLoader.getSystemClassLoader(), properties, configFile);

        if (!String.valueOf(properties.get(PRIVATE_TOKEN)).equals("null")) {
            log.warn("The configuration file specifies a private token. It is not safe. Use TMS_PRIVATE_TOKEN environment variable");
        }

        properties.putAll(loadPropertiesFromEnv());
        properties.putAll(loadPropertiesFromCli());

        return properties;
    }

    private static void loadPropertiesFrom(final ClassLoader classLoader, final Properties properties, String fileName) {
        Properties newProps = new Properties();

        try (InputStream stream = classLoader.getResourceAsStream(fileName)) {
            if (stream != null) {
                newProps.load(stream);

                for (String key : newProps.stringPropertyNames()) {
                    String value = newProps.getProperty(key);

                    if (value != null && !value.isEmpty()) {
                        properties.setProperty(key, value);
                    }
                }

            }
        } catch (IOException e) {
            log.error("Exception while read properties: {}", e.getMessage());
        }
    }

    private static Map<String, String> loadPropertiesFromEnv() {
        Map<String, String> map = new HashMap<>();


        try {
            String url = System.getenv(String.format("%s_URL", ENV_PREFIX));
            URI ignored = new java.net.URL(url).toURI();
            map.put(URL, url);
        } catch (MalformedURLException | URISyntaxException ignored) {
        }

        String token = System.getenv(String.format("%s_PRIVATE_TOKEN", ENV_PREFIX));
        if (token != null && !token.isEmpty()) {
            map.put(PRIVATE_TOKEN, token);
        }

        try {
            String projectId = System.getenv(String.format("%s_PROJECT_ID", ENV_PREFIX));
            java.util.UUID ignored = java.util.UUID.fromString(projectId);
            map.put(PROJECT_ID, projectId);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            String configurationId = System.getenv(String.format("%s_CONFIGURATION_ID", ENV_PREFIX));
            java.util.UUID ignored = java.util.UUID.fromString(configurationId);
            map.put(CONFIGURATION_ID, configurationId);

        } catch (IllegalArgumentException ignored) {
        }

        try {
            String testRunId = System.getenv(String.format("%s_TEST_RUN_ID", ENV_PREFIX));
            java.util.UUID ignored = java.util.UUID.fromString(testRunId);
            map.put(TEST_RUN_ID, testRunId);
        } catch (IllegalArgumentException ignored) {
        }

        String testRunName = System.getenv(String.format("%s_TEST_RUN_NAME", ENV_PREFIX));
        if (testRunName != null && !testRunName.isEmpty()) {
            map.put(TEST_RUN_NAME, testRunName);
        }

        try {
            String adapterMode = System.getenv(String.format("%s_ADAPTER_MODE", ENV_PREFIX));
            int mode = Integer.parseInt(adapterMode);

            if (0 < mode && mode <= 2) {
                map.put(ADAPTER_MODE, adapterMode);
            }
        } catch (NumberFormatException | NullPointerException ignored) {
        }

        String createTestCases = System.getenv(String.format("%s_AUTOMATIC_CREATION_TEST_CASES", ENV_PREFIX));
        if (Boolean.parseBoolean(createTestCases)) {
            map.put(AUTOMATIC_CREATION_TEST_CASES, createTestCases);
        }

        String certValidation = System.getenv(String.format("%s_CERT_VALIDATION", ENV_PREFIX.toLowerCase()));
        if (!Boolean.parseBoolean(certValidation)) {
            map.put(CERT_VALIDATION, certValidation);
        }

        String tmsIntegration = System.getenv(String.format("%s_TEST_IT", ENV_PREFIX.toLowerCase()));
        if (!Boolean.parseBoolean(tmsIntegration)) {
            map.put(TMS_INTEGRATION, tmsIntegration);
        }

        return map;
    }

    private static Map<String, String> loadPropertiesFromCli() {
        Map<String, String> map = new HashMap<>();
        Properties systemProperties = System.getProperties();

        try {
            String url = systemProperties.getProperty(String.format("%sUrl", ENV_PREFIX.toLowerCase()));
            URI ignored = new java.net.URL(url).toURI();
            map.put(URL, url);
        } catch (MalformedURLException | URISyntaxException ignored) {
        }

        String token = systemProperties.getProperty(String.format("%sPrivateToken", ENV_PREFIX.toLowerCase()));
        if (token != null && !token.isEmpty()) {
            map.put(PRIVATE_TOKEN, token);
        }

        try {
            String projectId = systemProperties.getProperty(String.format("%sProjectId", ENV_PREFIX.toLowerCase()));
            java.util.UUID ignored = java.util.UUID.fromString(projectId);
            map.put(PROJECT_ID, projectId);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            String configurationId = systemProperties.getProperty(String.format("%sConfigurationId", ENV_PREFIX.toLowerCase()));
            java.util.UUID ignored = java.util.UUID.fromString(configurationId);
            map.put(CONFIGURATION_ID, configurationId);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            String testRunId = systemProperties.getProperty(String.format("%sTestRunId", ENV_PREFIX.toLowerCase()));
            java.util.UUID ignored = java.util.UUID.fromString(testRunId);
            map.put(TEST_RUN_ID, testRunId);
        } catch (IllegalArgumentException ignored) {
        }

        String testRunName = systemProperties.getProperty(String.format("%sTestRunName", ENV_PREFIX.toLowerCase()));
        if (testRunName != null && !testRunName.isEmpty()) {
            map.put(TEST_RUN_NAME, testRunName);
        }

        try {
            String adapterMode = systemProperties.getProperty(String.format("%sAdapterMode", ENV_PREFIX.toLowerCase()));
            int mode = Integer.parseInt(adapterMode);

            if (0 < mode && mode <= 2) {
                map.put(ADAPTER_MODE, adapterMode);
            }
        } catch (NumberFormatException | NullPointerException ignored) {
        }

        String createTestCases = systemProperties.getProperty(String.format("%sAutomaticCreationTestCases", ENV_PREFIX.toLowerCase()));
        if (Boolean.parseBoolean(createTestCases)) {
            map.put(AUTOMATIC_CREATION_TEST_CASES, createTestCases);
        }

        String certValidation = systemProperties.getProperty(String.format("%sCertValidation", ENV_PREFIX.toLowerCase()));
        if (!Boolean.parseBoolean(certValidation)) {
            map.put(CERT_VALIDATION, certValidation);
        }

        String tmsIntegration = systemProperties.getProperty(String.format("%sTestIt", ENV_PREFIX.toLowerCase()));
        if (!Boolean.parseBoolean(tmsIntegration)) {
            map.put(TMS_INTEGRATION, tmsIntegration);
        }

        return map;
    }

    private static String getConfigFileName() {
        Properties systemProperties = System.getProperties();
        String fileNameFromCli = systemProperties.getProperty(String.format("%sConfigFile", ENV_PREFIX.toLowerCase()));
        if (fileNameFromCli != null && !fileNameFromCli.isEmpty()) {
            return fileNameFromCli;
        }

        String fileNameFromEnv = System.getenv(String.format("%s%s", ENV_PREFIX, CONFIG_FILE.toUpperCase(Locale.getDefault())));
        if (fileNameFromEnv != null && !fileNameFromEnv.isEmpty()) {
            return fileNameFromEnv;
        }

        return PROPERTIES_FILE;
    }
}
