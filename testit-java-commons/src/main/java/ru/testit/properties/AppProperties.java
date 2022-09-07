package ru.testit.properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;

public class AppProperties {
    private static final String PROPERTIES_FILE = "testit.properties";
    private static final Logger log = LoggerFactory.getLogger(AppProperties.class);

    public AppProperties() {
    }

    public static Properties loadProperties() {
        Properties properties = new Properties();
        loadPropertiesFrom(Thread.currentThread().getContextClassLoader(), properties);
        loadPropertiesFrom(ClassLoader.getSystemClassLoader(), properties);

        if (!String.valueOf(properties.get("PrivateToken")).equals("null")) {
            log.warn("The configuration file specifies a private token. It is not safe. Use TMS_PRIVATE_TOKEN environment variable");
        }

        properties.putAll(System.getenv());
        return properties;
    }

    private static void loadPropertiesFrom(final ClassLoader classLoader, final Properties properties) {
        try (InputStream stream = classLoader.getResourceAsStream(PROPERTIES_FILE)) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException e) {
            log.error("Exception while read properties: {}", e.getMessage());
        }
    }
}
