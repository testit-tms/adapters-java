package ru.testit.properties;

import ru.testit.services.Utils;

import java.io.Serializable;
import java.util.Objects;
import java.util.Properties;

public class AdapterConfig implements Serializable {
    private AdapterMode mode;
    private boolean automaticCreationTestCases;
    private boolean tmsIntegration;
    private String syncStoragePath;
    private String syncStoragePort;


    public AdapterConfig(Properties properties) {
        try {
            String modeValue = String.valueOf(properties.get(AppProperties.ADAPTER_MODE));
            this.mode = AdapterMode.valueOf(Integer.parseInt(modeValue));
        } catch (NullPointerException | NumberFormatException ignored) {
            this.mode = AdapterMode.USE_FILTER;
        }

        try {
            String automaticCreationTestCasesValue = String.valueOf(properties.get(AppProperties.AUTOMATIC_CREATION_TEST_CASES));
            this.automaticCreationTestCases = Objects.equals(automaticCreationTestCasesValue, "true");
        } catch (NullPointerException ignored) {
            this.automaticCreationTestCases = false;
        }

        try {
            String tmsIntegrationItValue = String.valueOf(properties.get(AppProperties.TMS_INTEGRATION));
            this.tmsIntegration = !Objects.equals(tmsIntegrationItValue, "false");
        } catch (NullPointerException ignored) {
            this.tmsIntegration = true;
        }

        try {
            this.syncStoragePath = String.valueOf(properties.get(AppProperties.SYNC_STORAGE_PATH));
            if ("null".equals(this.syncStoragePath)) {
                this.syncStoragePath = null;
            }
        } catch (NullPointerException ignored) {
            this.syncStoragePath = null;
        }

        try {
            this.syncStoragePort = String.valueOf(properties.get(AppProperties.SYNC_STORAGE_PORT));
            if ("null".equals(this.syncStoragePort)) {
                this.syncStoragePort = "49152"; // Порт по умолчанию
            }
        } catch (NullPointerException ignored) {
            this.syncStoragePort = "49152"; // Порт по умолчанию
        }
    }

    public AdapterMode getMode() {
        return mode;
    }

    public boolean shouldAutomaticCreationTestCases() {
        return automaticCreationTestCases;
    }

    public boolean shouldEnableTmsIntegration() {
        return tmsIntegration;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AdapterConfig {\n");
        sb.append("    mode: ").append(Utils.toIndentedString(this.mode)).append("\n");
        sb.append("    automaticCreationTestCases: ").append(Utils.toIndentedString(this.automaticCreationTestCases)).append("\n");
        sb.append("    tmsIntegration: ").append(Utils.toIndentedString(this.tmsIntegration)).append("\n");
        sb.append("    syncStoragePath: ").append(Utils.toIndentedString(this.syncStoragePath)).append("\n");
        sb.append("    syncStoragePort: ").append(Utils.toIndentedString(this.syncStoragePort)).append("\n");
        sb.append("}");

        return sb.toString();
    }

    public String getSyncStoragePath() {
        return syncStoragePath;
    }

    public String getSyncStoragePort() {
        return syncStoragePort;
    }
}
