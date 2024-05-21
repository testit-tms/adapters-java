package ru.testit.listener;

import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.messages.types.*;
import io.cucumber.plugin.event.TestSourceRead;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class ScenarioParser {
    private final ScenarioStorage scenarioStorage;

    public ScenarioParser() {
        this.scenarioStorage = new ScenarioStorage();
    }

    public void addScenarioEvent(final URI path, final TestSourceRead event) {
        scenarioStorage.addScenarioEvent(path, event);
    }

    public Feature getFeature(final URI path) {
        return scenarioStorage.getFeature(path);
    }

    public Scenario getScenarioDefinition(final URI path, final int line) {
        return ScenarioStorage.getScenarioDefinition(scenarioStorage.getCucumberNode(path, line));
    }

    public String getAction(final URI uri, final int stepLine) {
        return this.getActionFromSourceInternal(uri, stepLine);
    }

    private String getActionFromSourceInternal(final URI uri, final int stepLine) {
        final Feature feature = getFeature(uri);
        if (feature != null) {
            final TestSourceRead event = scenarioStorage.getScenarioEvent(uri);
            final String trimmedSourceLine = event.getSource().split("\n")[stepLine - 1].trim();
            final GherkinDialect dialect = new GherkinDialectProvider(feature.getLanguage()).getDefaultDialect();
            for (String keyword : dialect.getStepKeywords()) {
                if (trimmedSourceLine.startsWith(keyword)) {
                    return keyword;
                }
            }
        }
        return "";
    }

    public Map<String, String> getParameters(final URI uri, final int stepLine, Map<String, String> parameters) {
        Step step = getStep(uri, stepLine);

        if (step == null || !step.getDataTable().isPresent()) {
            return parameters;
        }

        return updateParameters(step.getDataTable().get(), parameters);
    }

    private Step getStep(final URI uri, final int stepLine) {
        Scenario scenario = getScenarioDefinition(uri, stepLine);

        if (scenario == null) {
            return null;
        }

        return scenario.getSteps().stream().filter(s -> s.getLocation().getLine().equals((long) stepLine)).findFirst().orElse(null);
    }

    private Map<String, String> updateParameters(DataTable dataTable, Map<String, String> parameters) {
        for (TableRow row : dataTable.getRows()) {
            List<TableCell> cells = row.getCells();

            if (cells.size() == 2) {
                parameters.put(cells.get(0).getValue(), cells.get(1).getValue());

                continue;
            }

            parameters.put("arg" + parameters.size(), cells.get(0).getValue());
        }

        return parameters;
    }
}
