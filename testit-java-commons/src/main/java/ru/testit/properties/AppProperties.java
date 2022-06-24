package ru.testit.properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class AppProperties {
    private static final Logger log;
    private Properties appProps;

    public AppProperties() {
        final String appConfigPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("testit.properties")).getPath();
        this.appProps = new Properties();
        try {
            this.appProps.load(new FileInputStream(appConfigPath));
        } catch (IOException e) {
            AppProperties.log.error("Exception while read properties", (Throwable) e);
        }
    }

    public String getProjectID() {
        return String.valueOf(this.appProps.get("ProjectId"));
    }

    public String getUrl() {
        return String.valueOf(this.appProps.get("URL"));
    }

    public String getPrivateToken() {
        return String.valueOf(this.appProps.get("PrivateToken"));
    }

    public String getConfigurationId() {
        return String.valueOf(this.appProps.get("ConfigurationId"));
    }

    public String getTestRunId() {
        return String.valueOf(this.appProps.get("TestRunId"));
    }

    static {
        log = LoggerFactory.getLogger((Class) AppProperties.class);
    }
}
