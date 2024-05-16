package ru.testit.listener;

import io.cucumber.gherkin.Gherkin;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.*;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.RuleChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples;
import io.cucumber.messages.internal.com.google.protobuf.GeneratedMessageV3;
import io.cucumber.plugin.event.TestSourceRead;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.cucumber.gherkin.Gherkin.makeSourceEnvelope;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class ScenarioStorage {
    private final Map<URI, TestSourceRead> pathToScenarioMap = new HashMap<>();
    private final Map<URI, GherkinDocument> pathToDocumentMap = new HashMap<>();
    private final Map<URI, Map<Integer, CucumberNode>> pathToNodeMap = new HashMap<>();

    public static Scenario getScenarioDefinition(final CucumberNode cucumberNode) {
        CucumberNode candidate = cucumberNode;
        while (candidate != null && !(candidate.node instanceof Scenario)) {
            candidate = candidate.parent;
        }
        return candidate == null ? null : (Scenario) candidate.node;
    }

    public void addScenarioEvent(final URI path, final TestSourceRead event) {
        pathToScenarioMap.put(path, event);
    }

    public TestSourceRead getScenarioEvent(final URI uri) {
        if (this.pathToScenarioMap.containsKey(uri)) {
            return pathToScenarioMap.get(uri);
        }
        return null;
    }

    public Feature getFeature(final URI path) {
        if (!pathToDocumentMap.containsKey(path)) {
            parseGherkinSource(path);
        }
        if (pathToDocumentMap.containsKey(path)) {
            return pathToDocumentMap.get(path).getFeature();
        }
        return null;
    }

    public CucumberNode getCucumberNode(final URI path, final int line) {
        if (!pathToNodeMap.containsKey(path)) {
            parseGherkinSource(path);
        }
        if (pathToNodeMap.containsKey(path)) {
            return pathToNodeMap.get(path).get(line);
        }
        return null;
    }

    private void parseGherkinSource(final URI path) {
        if (!pathToScenarioMap.containsKey(path)) {
            return;
        }
        final String source = pathToScenarioMap.get(path).getSource();

        final List<Messages.Envelope> sources = singletonList(
                makeSourceEnvelope(source, path.toString()));

        final List<Messages.Envelope> envelopes = Gherkin.fromSources(
                sources,
                true,
                true,
                true,
                () -> String.valueOf(UUID.randomUUID())).collect(toList());

        final GherkinDocument gherkinDocument = envelopes.stream()
                .filter(Messages.Envelope::hasGherkinDocument)
                .map(Messages.Envelope::getGherkinDocument)
                .findFirst()
                .orElse(null);

        pathToDocumentMap.put(path, gherkinDocument);
        final Map<Integer, CucumberNode> nodeMap = new HashMap<>();
        final CucumberNode currentParent = createCucumberNode(gherkinDocument.getFeature(), null);
        for (FeatureChild child : gherkinDocument.getFeature().getChildrenList()) {
            processFeatureDefinition(nodeMap, child, currentParent);
        }
        pathToNodeMap.put(path, nodeMap);
    }

    private void processFeatureDefinition(
            final Map<Integer, CucumberNode> nodeMap, final FeatureChild child, final CucumberNode currentParent) {
        if (child.hasBackground()) {
            processBackgroundDefinition(nodeMap, child.getBackground(), currentParent);
        } else if (child.hasScenario()) {
            processScenarioDefinition(nodeMap, child.getScenario(), currentParent);
        } else if (child.hasRule()) {
            final CucumberNode childNode = createCucumberNode(child.getRule(), currentParent);
            nodeMap.put(child.getRule().getLocation().getLine(), childNode);
            for (RuleChild ruleChild : child.getRule().getChildrenList()) {
                processRuleDefinition(nodeMap, ruleChild, childNode);
            }
        }
    }

    private void processScenarioDefinition(final Map<Integer, CucumberNode> nodeMap, final Scenario child,
                                           final CucumberNode currentParent) {
        final CucumberNode childNode = createCucumberNode(child, currentParent);
        nodeMap.put(child.getLocation().getLine(), childNode);
        for (Step step : child.getStepsList()) {
            nodeMap.put(step.getLocation().getLine(), createCucumberNode(step, childNode));
        }
        if (child.getExamplesCount() > 0) {
            processScenarioOutlineExamples(nodeMap, child, childNode);
        }
    }

    private void processBackgroundDefinition(
            final Map<Integer, CucumberNode> nodeMap, final Background background, final CucumberNode currentParent
    ) {
        final CucumberNode childNode = createCucumberNode(background, currentParent);
        nodeMap.put(background.getLocation().getLine(), childNode);
        for (Step step : background.getStepsList()) {
            nodeMap.put(step.getLocation().getLine(), createCucumberNode(step, childNode));
        }
    }

    private void processRuleDefinition(
            final Map<Integer, CucumberNode> nodeMap, final RuleChild child, final CucumberNode currentParent) {
        if (child.hasBackground()) {
            processBackgroundDefinition(nodeMap, child.getBackground(), currentParent);
        } else if (child.hasScenario()) {
            processScenarioDefinition(nodeMap, child.getScenario(), currentParent);
        }
    }

    private void processScenarioOutlineExamples(final Map<Integer, CucumberNode> nodeMap,
                                                final Scenario scenarioOutline,
                                                final CucumberNode parent) {
        for (Examples examples : scenarioOutline.getExamplesList()) {
            final CucumberNode examplesNode = createCucumberNode(examples, parent);
            final TableRow headerRow = examples.getTableHeader();
            final CucumberNode headerNode = createCucumberNode(headerRow, examplesNode);
            nodeMap.put(headerRow.getLocation().getLine(), headerNode);
            for (int i = 0; i < examples.getTableBodyCount(); ++i) {
                final TableRow examplesRow = examples.getTableBody(i);
                final CucumberNode expandedScenarioNode = createCucumberNode(examplesRow, examplesNode);
                nodeMap.put(examplesRow.getLocation().getLine(), expandedScenarioNode);
            }
        }
    }

    private static CucumberNode createCucumberNode(final GeneratedMessageV3 node, final CucumberNode astNode) {
        return new CucumberNode(node, astNode);
    }

    private static class CucumberNode {
        private final GeneratedMessageV3 node;
        private final CucumberNode parent;

        CucumberNode(final GeneratedMessageV3 node, final CucumberNode parent) {
            this.node = node;
            this.parent = parent;
        }
    }
}
