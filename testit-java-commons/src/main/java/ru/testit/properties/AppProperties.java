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
    private static final String ENV_PREFIX = "TMS_";
    private static final String CONFIG_FILE = "CONFIG_FILE";
    private static final String PROPERTIES_FILE = "testit.properties";
    private static final Logger log = LoggerFactory.getLogger(AppProperties.class);

    public AppProperties() {
    }

    public static Properties loadProperties() {

        Properties properties = new Properties();
        loadPropertiesFrom(Thread.currentThread().getContextClassLoader(), properties, getConfigFileName());
        loadPropertiesFrom(ClassLoader.getSystemClassLoader(), properties, getConfigFileName());

        if (!String.valueOf(properties.get(PRIVATE_TOKEN)).equals("null")) {
            log.warn("The configuration file specifies a private token. It is not safe. Use TMS_PRIVATE_TOKEN environment variable");
        }

        properties.putAll(loadPropertiesFromEnv());
        return properties;
    }

    private static void loadPropertiesFrom(final ClassLoader classLoader, final Properties properties, String fileName) {
        try (InputStream stream = classLoader.getResourceAsStream(fileName)) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException e) {
            log.error("Exception while read properties: {}", e.getMessage());
        }
    }

    private static Map<String, String> loadPropertiesFromEnv() {
        Map<String, String> map = new HashMap<>();

        String url = System.getenv(String.format("%sURL", ENV_PREFIX));
        if (url != null) {
            map.put(URL, url);
        }

        String token = System.getenv(String.format("%sPRIVATE_TOKEN", ENV_PREFIX));
        if (token != null) {
            map.put(PRIVATE_TOKEN, token);
        }

        String project = System.getenv(String.format("%sPROJECT_ID", ENV_PREFIX));
        if (project != null) {
            map.put(PROJECT_ID, project);
        }

        String config = System.getenv(String.format("%sCONFIGURATION_ID", ENV_PREFIX));
        if (config != null) {
            map.put(CONFIGURATION_ID, config);
        }

        String testRunId = System.getenv(String.format("%sTEST_RUN_ID", ENV_PREFIX));
        if (testRunId != null) {
            map.put(TEST_RUN_ID, testRunId);
        }

        String testRunName = System.getenv(String.format("%sTEST_RUN_NAME", ENV_PREFIX));
        if (testRunName != null) {
            map.put(TEST_RUN_NAME, testRunName);
        }

        String adapterMode = System.getenv(String.format("%sADAPTER_MODE", ENV_PREFIX));
        if (adapterMode != null) {
            map.put(ADAPTER_MODE, adapterMode);
        }

        return map;
    }

    private static String getConfigFileName() {
        String fileName = System.getenv(String.format("%s%s", ENV_PREFIX, CONFIG_FILE.toUpperCase(Locale.getDefault())));

        if (fileName != null) {
            return fileName;
        }

        return PROPERTIES_FILE;
    }
}
