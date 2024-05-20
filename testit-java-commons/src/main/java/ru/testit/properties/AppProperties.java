package ru.testit.properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    public static final String TMS_INTEGRATION = "tmsTestIt";

    private static final String ENV_PREFIX = "TMS";
    private static final String PROPERTIES_FILE = "testit.properties";
    private static final Logger log = LoggerFactory.getLogger(AppProperties.class);

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

        properties.putAll(loadPropertiesFromEnv());

        return validateProperties(properties);
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
            String url = System.getProperty(String.format("%s_URL", ENV_PREFIX));
            URI ignored = new java.net.URL(url).toURI();
            map.put(URL, url);
        } catch (MalformedURLException | URISyntaxException | SecurityException | NullPointerException |
                 IllegalArgumentException ignored) {
        }

        try {
            String token = System.getProperty(String.format("%s_PRIVATE_TOKEN", ENV_PREFIX));
            if (token != null && !token.isEmpty() && !token.equals("null")) {
                map.put(PRIVATE_TOKEN, token);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String projectId = System.getProperty(String.format("%s_PROJECT_ID", ENV_PREFIX));
            if (projectId != null && !projectId.isEmpty()) {
                java.util.UUID ignored = java.util.UUID.fromString(projectId);
                map.put(PROJECT_ID, projectId);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String configurationId = System.getProperty(String.format("%s_CONFIGURATION_ID", ENV_PREFIX));
            if (configurationId != null && !configurationId.isEmpty()) {
                java.util.UUID ignored = java.util.UUID.fromString(configurationId);
                map.put(CONFIGURATION_ID, configurationId);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String testRunId = System.getProperty(String.format("%s_TEST_RUN_ID", ENV_PREFIX));
            if (testRunId != null && !testRunId.isEmpty()) {
                java.util.UUID ignored = java.util.UUID.fromString(testRunId);
                map.put(TEST_RUN_ID, testRunId);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String testRunName = System.getProperty(String.format("%s_TEST_RUN_NAME", ENV_PREFIX));
            if (testRunName != null && !testRunName.isEmpty() && !testRunName.equals("null")) {
                map.put(TEST_RUN_NAME, testRunName);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String adapterMode = System.getProperty(String.format("%s_ADAPTER_MODE", ENV_PREFIX));
            int mode = Integer.parseInt(adapterMode);

            if (0 <= mode && mode <= 2) {
                map.put(ADAPTER_MODE, adapterMode);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String createTestCases = System.getProperty(String.format("%s_AUTOMATIC_CREATION_TEST_CASES", ENV_PREFIX));
            if (Objects.equals(createTestCases, "false") || Objects.equals(createTestCases, "true")) {
                map.put(AUTOMATIC_CREATION_TEST_CASES, createTestCases);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String certValidation = System.getProperty(String.format("%s_CERT_VALIDATION", ENV_PREFIX));
            if (Objects.equals(certValidation, "false") || Objects.equals(certValidation, "true")) {
                map.put(CERT_VALIDATION, certValidation);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            String tmsIntegration = System.getProperty(String.format("%s_TEST_IT", ENV_PREFIX));
            if (Objects.equals(tmsIntegration, "false") || Objects.equals(tmsIntegration, "true")) {
                map.put(TMS_INTEGRATION, tmsIntegration);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        return map;
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

        String errors = errorsBuilder.toString();
        if (!errors.isEmpty()) {
            throw new AssertionError("Invalid configuration provided : " + errors);
        }

        return properties;
    }

    private static String getConfigFileName() {
        try {
            String fileName = System.getProperty(String.format("%s_CONFIG_FILE", ENV_PREFIX));
            if (fileName != null && !fileName.isEmpty() && !fileName.equals("null")) {
                return fileName;
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignored) {
        }

        return PROPERTIES_FILE;
    }
}
