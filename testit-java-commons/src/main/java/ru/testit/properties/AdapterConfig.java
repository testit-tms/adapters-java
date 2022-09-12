package ru.testit.properties;

import java.util.Properties;

public class AdapterConfig {
    private final AdapterMode mode;

    public AdapterConfig(Properties properties) {
        String modeValue = String.valueOf(properties.get(AppProperties.ADAPTER_MODE));
        if (modeValue.equals("null")) {
            modeValue = "0";
        }
        this.mode = AdapterMode.valueOf(Integer.parseInt(modeValue));
    }

    public AdapterMode getMode() {
        return mode;
    }
}

