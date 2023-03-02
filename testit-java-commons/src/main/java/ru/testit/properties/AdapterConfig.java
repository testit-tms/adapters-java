package ru.testit.properties;

import ru.testit.services.Utils;

import java.io.Serializable;
import java.util.Properties;

public class AdapterConfig implements Serializable {
    private final AdapterMode mode;
    private final boolean createTestCases;

    public AdapterConfig(Properties properties) {
        String modeValue = String.valueOf(properties.get(AppProperties.ADAPTER_MODE));
        if (modeValue.equals("null")) {
            modeValue = "0";
        }
        this.mode = AdapterMode.valueOf(Integer.parseInt(modeValue));
        this.createTestCases = Boolean.parseBoolean(
                String.valueOf(properties.get(AppProperties.CREATE_TEST_CASES)));
    }

    public AdapterMode getMode() {
        return mode;
    }

    public boolean mustCreateTestCases() {
        return createTestCases;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AdapterConfig {\n");
        sb.append("    mode: ").append(Utils.toIndentedString(this.mode)).append("\n");
        sb.append("    createTestCases: ").append(Utils.toIndentedString(this.createTestCases)).append("\n");
        sb.append("}");

        return sb.toString();
    }
}

