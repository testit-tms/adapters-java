package ru.testit.properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class AppProperties {
    public static final String URL = "url";
    public static final String PRIVATE_TOKEN = "privateToken";
    public static final String PROJECT_ID = "projectId";
    public static final String CONFIGURATION_ID = "configurationId";
    public static final String TEST_RUN_ID = "testRunId";
    public static final String TEST_RUN_NAME = "testRunName";
    public static final String ADAPTER_MODE = "adapterMode";
    public static final String AUTOMATIC_CREATION_TEST_CASES = "automaticCreationTestCases";
    public static final String AUTOMATIC_UPDATION_LINKS_TO_TEST_CASES = "automaticUpdationLinksToTestCases";
    public static final String CERT_VALIDATION = "certValidation";
    public static final String TMS_INTEGRATION = "testIt";
    public static final String TMS_IMPORT_REALTIME = "importRealtime";

    private static final String PROPERTIES_FILE = "testit.properties";
    private static final Logger log = LoggerFactory.getLogger(AppProperties.class);
    private static final HashMap<String, HashMap<String, String>> envVarsNames  = new HashMap<String, HashMap<String, String>>() {{
        put("env", new HashMap<String, String>() {{
            put(URL, "TMS_URL");
            put(PRIVATE_TOKEN, "TMS_PRIVATE_TOKEN");
            put(PROJECT_ID, "TMS_PROJECT_ID");
            put(CONFIGURATION_ID, "TMS_CONFIGURATION_ID");
            put(TEST_RUN_ID, "TMS_TEST_RUN_ID");
            put(TEST_RUN_NAME, "TMS_TEST_RUN_NAME");
            put(ADAPTER_MODE, "TMS_ADAPTER_MODE");
            put(AUTOMATIC_CREATION_TEST_CASES, "TMS_AUTOMATIC_CREATION_TEST_CASES");
            put(AUTOMATIC_UPDATION_LINKS_TO_TEST_CASES, "TMS_AUTOMATIC_UPDATION_LINKS_TO_TEST_CASES");
            put(CERT_VALIDATION, "TMS_CERT_VALIDATION");
            put(TMS_INTEGRATION, "TMS_TEST_IT");
            put(TMS_IMPORT_REALTIME, "TMS_IMPORT_REALTIME");
        }});
        put("cli", new HashMap<String, String>() {{
            put(URL, "tmsUrl");
            put(PRIVATE_TOKEN, "tmsPrivateToken");
            put(PROJECT_ID, "tmsProjectId");
            put(CONFIGURATION_ID, "tmsConfigurationId");
            put(TEST_RUN_ID, "tmsTestRunId");
            put(TEST_RUN_NAME, "tmsTestRunName");
            put(ADAPTER_MODE, "tmsAdapterMode");
            put(AUTOMATIC_CREATION_TEST_CASES, "tmsAutomaticCreationTestCases");
            put(AUTOMATIC_UPDATION_LINKS_TO_TEST_CASES, "tmsAutomaticUpdationLinksToTestCases");
            put(CERT_VALIDATION, "tmsCertValidation");
            put(TMS_INTEGRATION, "tmsTestIt");
            put(TMS_IMPORT_REALTIME, "tmsImportRealtime");
        }});
    }};

    public AppProperties() {
    }

    public static Properties loadProperties() {
        String configFile = getConfigFileName();
        Properties properties = new Properties();

        loadPropertiesFrom(Thread.currentThread().getContextClassLoader(), properties, configFile);
        loadPropertiesFrom(ClassLoader.getSystemClassLoader(), properties, configFile);

        String token = String.valueOf(properties.get(PRIVATE_TOKEN));
        if (token != null && !token.isEmpty() && !token.equals("null")) {
            log.warn("The configuration file specifies a private token. It is not safe. Use TMS_PRIVATE_TOKEN environment variable");
        }

        Properties systemProps = System.getProperties();

        Properties envProps = new Properties();
        envProps.putAll(System.getenv());

        properties.putAll(loadPropertiesFromEnv(systemProps, envVarsNames.getOrDefault("env", new HashMap<>())));
        properties.putAll(loadPropertiesFromEnv(envProps, envVarsNames.getOrDefault("env", new HashMap<>())));
        properties.putAll(loadPropertiesFromEnv(systemProps, envVarsNames.getOrDefault("cli", new HashMap<>())));
        properties.putAll(loadPropertiesFromEnv(envProps, envVarsNames.getOrDefault("cli", new HashMap<>())));

        if (Objects.equals(properties.getProperty(TMS_INTEGRATION, "true"), "false")) {
            return properties;
        } else {
            return validateProperties(properties);
        }
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

    private static Map<String, String> loadPropertiesFromEnv(Properties properties, HashMap<String, String> varNames) {
        Map<String, String> result = new HashMap<>();

        try {
            String url = properties.getProperty(varNames.get(URL), null);
            URI ignored = new java.net.URL(url).toURI();
            result.put(URL, url);
        } catch (MalformedURLException | URISyntaxException | SecurityException | NullPointerException |
                 IllegalArgumentException ignored) {
        }

        try {
            String token = properties.getProperty(varNames.get(PRIVATE_TOKEN), null);
            if (token != null && !token.isEmpty() && !token.equals("null")) {
                result.put(PRIVATE_TOKEN, token);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String projectId = properties.getProperty(varNames.get(PROJECT_ID), null);
            if (projectId != null && !projectId.isEmpty()) {
                java.util.UUID ignored = java.util.UUID.fromString(projectId);
                result.put(PROJECT_ID, projectId);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String configurationId = properties.getProperty(varNames.get(CONFIGURATION_ID), null);
            if (configurationId != null && !configurationId.isEmpty()) {
                java.util.UUID ignored = java.util.UUID.fromString(configurationId);
                result.put(CONFIGURATION_ID, configurationId);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String testRunId = properties.getProperty(varNames.get(TEST_RUN_ID), null);
            if (testRunId != null && !testRunId.isEmpty()) {
                java.util.UUID ignored = java.util.UUID.fromString(testRunId);
                result.put(TEST_RUN_ID, testRunId);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String testRunName = properties.getProperty(varNames.get(TEST_RUN_NAME), null);
            if (testRunName != null && !testRunName.isEmpty() && !testRunName.equals("null")) {
                result.put(TEST_RUN_NAME, testRunName);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String adapterMode = properties.getProperty(varNames.get(ADAPTER_MODE), null);
            int mode = Integer.parseInt(adapterMode);

            if (0 <= mode && mode <= 2) {
                result.put(ADAPTER_MODE, adapterMode);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String createTestCases = properties.getProperty(varNames.get(AUTOMATIC_CREATION_TEST_CASES), null);
            if (Objects.equals(createTestCases, "false") || Objects.equals(createTestCases, "true")) {
                result.put(AUTOMATIC_CREATION_TEST_CASES, createTestCases);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String updateLinksToTestCases = properties.getProperty(varNames.get(AUTOMATIC_UPDATION_LINKS_TO_TEST_CASES), null);
            if (Objects.equals(updateLinksToTestCases, "false") || Objects.equals(updateLinksToTestCases, "true")) {
                result.put(AUTOMATIC_UPDATION_LINKS_TO_TEST_CASES, updateLinksToTestCases);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String certValidation = properties.getProperty(varNames.get(CERT_VALIDATION), null);
            if (Objects.equals(certValidation, "false") || Objects.equals(certValidation, "true")) {
                result.put(CERT_VALIDATION, certValidation);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String tmsIntegration = properties.getProperty(varNames.get(TMS_INTEGRATION), null);
            if (Objects.equals(tmsIntegration, "false") || Objects.equals(tmsIntegration, "true")) {
                result.put(TMS_INTEGRATION, tmsIntegration);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String tmsImportRealtime = properties.getProperty(varNames.get(TMS_IMPORT_REALTIME), null);
            if (Objects.equals(tmsImportRealtime, "false") || Objects.equals(tmsImportRealtime, "true")) {
                result.put(TMS_IMPORT_REALTIME, tmsImportRealtime);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        return result;
    }

    private static Properties validateProperties(Properties properties) {
        StringBuilder errorsBuilder = new StringBuilder();

        try {
            String url = properties.getProperty(URL);
            URI ignored = new java.net.URL(url).toURI();
        } catch (Exception e) {
            String message = "Invalid url: " + e.getMessage();
            log.error(message);
            errorsBuilder.append(message).append(System.lineSeparator());
        }

        String token = properties.getProperty(PRIVATE_TOKEN);
        if (token == null || token.isEmpty() || token.equals("null")) {
            String message = "Invalid token: " + token;
            log.error(message);
            errorsBuilder.append(message).append(System.lineSeparator());
        }

        try {
            String projectId = properties.getProperty(PROJECT_ID);
            java.util.UUID ignored = java.util.UUID.fromString(projectId);
        } catch (Exception e) {
            String message = "Invalid projectId: " + e.getMessage();
            log.error(message);
            errorsBuilder.append(message).append(System.lineSeparator());
        }

        try {
            String configurationId = properties.getProperty(CONFIGURATION_ID);
            java.util.UUID ignored = java.util.UUID.fromString(configurationId);
        } catch (Exception e) {
            String message = "Invalid configurationId: " + e.getMessage();
            log.error(message);
            errorsBuilder.append(message).append(System.lineSeparator());
        }

        try {
            String adapterMode = properties.getProperty(ADAPTER_MODE);
            int mode = Integer.parseInt(adapterMode);

            if (mode > 2 || mode < 0) {
                log.warn("Invalid adapterMode: {}. Use default value instead: 0", mode);
                properties.setProperty(ADAPTER_MODE, "0");
            }
        } catch (Exception e) {
            log.warn("Invalid adapterMode: {}. Use default value instead: 0", e.getMessage());
            properties.setProperty(ADAPTER_MODE, "0");
        }

        int adapterMode = 0;

        try {
            adapterMode = Integer.parseInt(properties.getProperty(ADAPTER_MODE));
        } catch (Exception ignored) { }

        try {
            String testRunId = properties.getProperty(TEST_RUN_ID);
            java.util.UUID ignored = java.util.UUID.fromString(testRunId);

            if (adapterMode == 2) {
                String message = "Adapter works in mode 2. Config should not contains test run id.";
                errorsBuilder.append(message).append(System.lineSeparator());
            }
        } catch (Exception e) {
            if (adapterMode == 0 || adapterMode == 1) {
                String message = "Invalid testRunId: " + e.getMessage();
                log.error(message);
                errorsBuilder.append(message).append(System.lineSeparator());
            }
        }

        String createTestCases = properties.getProperty(AUTOMATIC_CREATION_TEST_CASES);
        if (!Objects.equals(createTestCases, "false") && !Objects.equals(createTestCases, "true")) {
            log.warn("Invalid autoCreateTestCases: {}. Use default value instead: false", createTestCases);
            properties.setProperty(AUTOMATIC_CREATION_TEST_CASES, "false");
        }

        String updateLinksToTestCases = properties.getProperty(AUTOMATIC_UPDATION_LINKS_TO_TEST_CASES);
        if (!Objects.equals(updateLinksToTestCases, "false") && !Objects.equals(updateLinksToTestCases, "true")) {
            log.warn("Invalid autoUpdateLinksToTestCases: {}. Use default value instead: false", updateLinksToTestCases);
            properties.setProperty(AUTOMATIC_UPDATION_LINKS_TO_TEST_CASES, "false");
        }

        String certValidation = properties.getProperty(CERT_VALIDATION);
        if (!Objects.equals(certValidation, "false") && !Objects.equals(certValidation, "true")) {
            log.warn("Invalid certValidation: {}. Use default value instead: true", certValidation);
            properties.setProperty(CERT_VALIDATION, "true");
        }

        String tmsIntegration = properties.getProperty(TMS_INTEGRATION);
        if (!Objects.equals(tmsIntegration, "false") && !Objects.equals(tmsIntegration, "true")) {
            log.warn("Invalid tmsIntegration: {}. Use default value instead: true", tmsIntegration);
            properties.setProperty(TMS_INTEGRATION, "true");
        }

        String tmsImportRealtime = properties.getProperty(TMS_IMPORT_REALTIME);
        if (!Objects.equals(tmsImportRealtime, "false") && !Objects.equals(tmsImportRealtime, "true")) {
            log.warn("Invalid tmsImportRealtime: {}. Use default value instead: true", tmsImportRealtime);
            properties.setProperty(TMS_IMPORT_REALTIME, "true");
        }

        String errors = errorsBuilder.toString();
        if (!errors.isEmpty()) {
            throw new AssertionError("Invalid configuration provided : " + errors);
        }

        return properties;
    }

    private static String getConfigFileName() {
        try {
            String fileName = System.getProperty("TMS_CONFIG_FILE");
            if (fileName != null && !fileName.isEmpty() && !fileName.equals("null")) {
                return fileName;
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        return PROPERTIES_FILE;
    }
}
