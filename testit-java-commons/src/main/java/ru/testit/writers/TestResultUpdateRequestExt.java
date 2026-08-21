package ru.testit.writers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.testit.adaptersapi.model.AutoTestStepResultUpdateRequest;
import ru.testit.adaptersapi.model.TestResultUpdateRequest;

import java.util.List;
import java.util.Map;

/**
 * Temporary workaround (mode 0): extend PUT body with {@code parameters} / {@code autoTestStepResults}
 * so we can avoid orphan {@code sendTestResults}. OpenAPI update model omits them; TMS may still ignore
 * these fields until the API supports enrich-via-PUT properly.
 */
public class TestResultUpdateRequestExt extends TestResultUpdateRequest {

    public static final String JSON_PROPERTY_PARAMETERS = "parameters";
    public static final String JSON_PROPERTY_AUTO_TEST_STEP_RESULTS = "autoTestStepResults";

    private Map<String, String> parameters;
    private List<AutoTestStepResultUpdateRequest> autoTestStepResults;

    @JsonProperty(JSON_PROPERTY_PARAMETERS)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, String> getParameters() {
        return parameters;
    }

    @JsonProperty(JSON_PROPERTY_PARAMETERS)
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @JsonProperty(JSON_PROPERTY_AUTO_TEST_STEP_RESULTS)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<AutoTestStepResultUpdateRequest> getAutoTestStepResults() {
        return autoTestStepResults;
    }

    @JsonProperty(JSON_PROPERTY_AUTO_TEST_STEP_RESULTS)
    public void setAutoTestStepResults(List<AutoTestStepResultUpdateRequest> autoTestStepResults) {
        this.autoTestStepResults = autoTestStepResults;
    }
}
