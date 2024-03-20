package ru.testit.properties;

import ru.testit.services.Utils;

import java.io.Serializable;
import java.util.Properties;

public class AdapterConfig implements Serializable {
    private final AdapterMode mode;
    private final boolean automaticCreationTestCases;
    private final boolean testIt;


    public AdapterConfig(Properties properties) {
        String modeValue = String.valueOf(properties.get(AppProperties.ADAPTER_MODE));
        if (modeValue.equals("null")) {
            modeValue = "0";
        }
        this.mode = AdapterMode.valueOf(Integer.parseInt(modeValue));

        String automaticCreationTestCasesValue = String.valueOf(
                properties.get(AppProperties.AUTOMATIC_CREATION_TEST_CASES));
        if (automaticCreationTestCasesValue.equals("null")) {
            automaticCreationTestCasesValue = "false";
        }
        this.automaticCreationTestCases = Boolean.parseBoolean(automaticCreationTestCasesValue);

        String testItValue = String.valueOf(properties.get(AppProperties.TEST_IT)).toLowerCase().trim();
        this.testIt = !testItValue.equals("false");
    }

    public AdapterMode getMode() {
        return mode;
    }

    public boolean shouldAutomaticCreationTestCases() {
        return automaticCreationTestCases;
    }

    public boolean shouldIntegrateToTestIt() {
        return testIt;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AdapterConfig {\n");
        sb.append("    mode: ").append(Utils.toIndentedString(this.mode)).append("\n");
        sb.append("    automaticCreationTestCases: ").append(Utils.toIndentedString(this.automaticCreationTestCases)).append("\n");
        sb.append("    testIt: ").append(Utils.toIndentedString(this.testIt)).append("\n");
        sb.append("}");

        return sb.toString();
    }
}

