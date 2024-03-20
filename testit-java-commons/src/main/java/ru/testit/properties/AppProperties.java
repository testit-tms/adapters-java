package ru.testit.properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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
    public static final String TEST_IT = "testIt";

    private static final String ENV_PREFIX = "TMS";
    private static final String CONFIG_FILE = "CONFIG_FILE";
    private static final String PROPERTIES_FILE = "testit.properties";
    private static final Logger log = LoggerFactory.getLogger(AppProperties.class);

    public AppProperties() {
    }

    public static Properties loadProperties() {
        String configFile = getConfigFileName();

        Properties properties = new Properties();
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

                    if (value != null && !value.isEmpty())
                    {
                        properties.setProperty(key, value);
                    }
                }

                return;
            }
        } catch (IOException e) {
            log.error("Exception while read properties: {}", e.getMessage());
        }

        throw new RuntimeException(String.format("Config file '%s' not found", fileName));
    }

    private static Map<String, String> loadPropertiesFromEnv() {
        Map<String, String> map = new HashMap<>();

        String url = System.getenv(String.format("%s_URL", ENV_PREFIX));
        if (url != null && !url.isEmpty()) {
            map.put(URL, url);
        }

        String token = System.getenv(String.format("%s_PRIVATE_TOKEN", ENV_PREFIX));
        if (token != null && !token.isEmpty()) {
            map.put(PRIVATE_TOKEN, token);
        }

        String project = System.getenv(String.format("%s_PROJECT_ID", ENV_PREFIX));
        if (project != null && !project.isEmpty()) {
            map.put(PROJECT_ID, project);
        }

        String config = System.getenv(String.format("%s_CONFIGURATION_ID", ENV_PREFIX));
        if (config != null && !config.isEmpty()) {
            map.put(CONFIGURATION_ID, config);
        }

        String testRunId = System.getenv(String.format("%s_TEST_RUN_ID", ENV_PREFIX));
        if (testRunId != null && !testRunId.isEmpty()) {
            map.put(TEST_RUN_ID, testRunId);
        }

        String testRunName = System.getenv(String.format("%s_TEST_RUN_NAME", ENV_PREFIX));
        if (testRunName != null && !testRunName.isEmpty()) {
            map.put(TEST_RUN_NAME, testRunName);
        }

        String adapterMode = System.getenv(String.format("%s_ADAPTER_MODE", ENV_PREFIX));
        if (adapterMode != null  && !adapterMode.isEmpty()) {
            map.put(ADAPTER_MODE, adapterMode);
        }

        String createTestCases = System.getenv(String.format("%s_AUTOMATIC_CREATION_TEST_CASES", ENV_PREFIX));
        if (createTestCases != null && !createTestCases.isEmpty()) {
            map.put(AUTOMATIC_CREATION_TEST_CASES, createTestCases);
        }

        String certValidation = System.getenv(String.format("%s_CERT_VALIDATION", ENV_PREFIX.toLowerCase()));
        if (certValidation != null && !certValidation.isEmpty()) {
            map.put(CERT_VALIDATION, certValidation);
        }

        String testIt = System.getenv(String.format("%s_TEST_IT", ENV_PREFIX.toLowerCase()));
        if (testIt != null && !testIt.isEmpty()) {
            map.put(TEST_IT, testIt);
        }

        return map;
    }

    private static Map<String, String> loadPropertiesFromCli() {
        Map<String, String> map = new HashMap<>();
        Properties systemProperties = System.getProperties();

        String url = systemProperties.getProperty(String.format("%sUrl", ENV_PREFIX.toLowerCase()));
        if (url != null && !url.isEmpty()) {
            map.put(URL, url);
        }

        String token = systemProperties.getProperty(String.format("%sPrivateToken", ENV_PREFIX.toLowerCase()));
        if (token != null && !token.isEmpty()) {
            map.put(PRIVATE_TOKEN, token);
        }

        String project = systemProperties.getProperty(String.format("%sProjectId", ENV_PREFIX.toLowerCase()));
        if (project != null && !project.isEmpty()) {
            map.put(PROJECT_ID, project);
        }

        String config = systemProperties.getProperty(String.format("%sConfigurationId", ENV_PREFIX.toLowerCase()));
        if (config != null && !config.isEmpty()) {
            map.put(CONFIGURATION_ID, config);
        }

        String testRunId = systemProperties.getProperty(String.format("%sTestRunId", ENV_PREFIX.toLowerCase()));
        if (testRunId != null && !testRunId.isEmpty()) {
            map.put(TEST_RUN_ID, testRunId);
        }

        String testRunName = systemProperties.getProperty(String.format("%sTestRunName", ENV_PREFIX.toLowerCase()));
        if (testRunName != null && !testRunName.isEmpty()) {
            map.put(TEST_RUN_NAME, testRunName);
        }

        String adapterMode = systemProperties.getProperty(String.format("%sAdapterMode", ENV_PREFIX.toLowerCase()));
        if (adapterMode != null && !adapterMode.isEmpty()) {
            map.put(ADAPTER_MODE, adapterMode);
        }

        String createTestCases = systemProperties.getProperty(String.format("%sAutomaticCreationTestCases", ENV_PREFIX.toLowerCase()));
        if (createTestCases != null && !createTestCases.isEmpty()) {
            map.put(AUTOMATIC_CREATION_TEST_CASES, createTestCases);
        }

        String certValidation = systemProperties.getProperty(String.format("%sCertValidation", ENV_PREFIX.toLowerCase()));
        if (certValidation != null && !certValidation.isEmpty()) {
            map.put(CERT_VALIDATION, certValidation);
        }

        String testIt = systemProperties.getProperty(String.format("%sTestIt", ENV_PREFIX.toLowerCase()));
        if (testIt != null && !testIt.isEmpty()) {
            map.put(TEST_IT, testIt);
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
