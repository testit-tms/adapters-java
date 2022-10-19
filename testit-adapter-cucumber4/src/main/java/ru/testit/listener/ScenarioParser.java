package ru.testit.listener;

import cucumber.api.event.TestSourceRead;
import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioDefinition;

public class ScenarioParser {
    private final ScenarioStorage scenarioStorage;

    public ScenarioParser() {
        this.scenarioStorage = new ScenarioStorage();
    }

    public void addScenarioEvent(final String path, final TestSourceRead event) {
        scenarioStorage.addScenarioEvent(path, event);
    }

    public Feature getFeature(final String path) {
        return scenarioStorage.getFeature(path);
    }

    public ScenarioDefinition getScenarioDefinition(final String path, final int line) {
        return ScenarioStorage.getScenarioDefinition(scenarioStorage.getCucumberNode(path, line));
    }

    public String getAction(final String uri, final int stepLine) {
        return this.getActionFromSourceInternal(uri, stepLine);
    }

    private String getActionFromSourceInternal(final String uri, final int stepLine) {
        final Feature feature = getFeature(uri);
        if (feature != null) {
            final TestSourceRead event = scenarioStorage.getScenarioEvent(uri);
            final String trimmedSourceLine = event.source.split("\n")[stepLine - 1].trim();
            final GherkinDialect dialect = new GherkinDialectProvider(feature.getLanguage()).getDefaultDialect();
            for (String keyword : dialect.getStepKeywords()) {
                if (trimmedSourceLine.startsWith(keyword)) {
                    return keyword;
                }
            }
        }
        return "";
    }
}
