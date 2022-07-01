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
        properties.putAll(System.getenv());
        loadPropertiesFrom(Thread.currentThread().getContextClassLoader(), properties);
        loadPropertiesFrom(ClassLoader.getSystemClassLoader(), properties);
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
