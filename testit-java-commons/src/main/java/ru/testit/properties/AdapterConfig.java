package ru.testit.properties;

import java.util.Properties;

public class AdapterConfig {
    private final AdapterMode mode;

    public AdapterConfig(Properties properties) {
        this.mode = AdapterMode.valueOf(Integer.parseInt(String.valueOf(properties.get("AdapterMode"))));
    }

    public AdapterMode getMode() {
        return mode;
    }
}

