package ru.testit.listener;

import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.plugin.event.TestSourceRead;

import java.net.URI;

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
}
