package ru.testit.listener;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.*;
import io.cucumber.plugin.event.TestSourceRead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ScenarioStorage {
    private static final Logger log = LoggerFactory.getLogger(ScenarioStorage.class);
    private final Map<URI, TestSourceRead> pathToScenarioMap = new HashMap<>();
    private final Map<URI, GherkinDocument> pathToDocumentMap = new HashMap<>();
    private final Map<URI, Map<Long, CucumberNode>> pathToNodeMap = new HashMap<>();

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
            return pathToDocumentMap.get(path).getFeature().orElse(null);
        }
        return null;
    }

    public CucumberNode getCucumberNode(final URI path, final int line) {
        if (!pathToNodeMap.containsKey(path)) {
            parseGherkinSource(path);
        }
        if (pathToNodeMap.containsKey(path)) {
            return pathToNodeMap.get(path).get((long) line);
        }
        return null;
    }

    private void parseGherkinSource(final URI path) {
        if (!pathToScenarioMap.containsKey(path)) {
            return;
        }
        final String source = pathToScenarioMap.get(path).getSource();

        final GherkinParser parser = GherkinParser.builder()
                .build();

        final Stream<Envelope> envelopes = parser.parse(
                Envelope.of(new Source(path.toString(), source, SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN)));

        final GherkinDocument gherkinDocument = envelopes
                .map(Envelope::getGherkinDocument)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);

        pathToDocumentMap.put(path, gherkinDocument);

        final Map<Long, CucumberNode> nodeMap = new HashMap<>();

        if (gherkinDocument == null) {
            log.error("gherkinDocument is null");
            throw new AssertionError();
        }

        Optional<Feature> featureOptional = gherkinDocument.getFeature();
        if (!featureOptional.isPresent()) {
            log.error("Feature is not present");
            throw new AssertionError();
        }
        final Feature feature = featureOptional.get();
        final CucumberNode currentParent = createCucumberNode(feature, null);
        for (FeatureChild child : feature.getChildren()) {
            processFeatureDefinition(nodeMap, child, currentParent);
        }
        pathToNodeMap.put(path, nodeMap);

    }

    private void processFeatureDefinition(
            final Map<Long, CucumberNode> nodeMap, final FeatureChild child, final CucumberNode currentParent) {
        child.getBackground().ifPresent(background -> processBackgroundDefinition(nodeMap, background, currentParent));
        child.getScenario().ifPresent(scenario -> processScenarioDefinition(nodeMap, scenario, currentParent));
        child.getRule().ifPresent(rule -> {
            final CucumberNode childNode = new CucumberNode(rule, currentParent);
            nodeMap.put(rule.getLocation().getLine(), childNode);
            rule.getChildren().forEach(ruleChild -> processRuleDefinition(nodeMap, ruleChild, childNode));
        });
    }

    private void processScenarioDefinition(final Map<Long, CucumberNode> nodeMap, final Scenario child,
                                           final CucumberNode currentParent) {
        final CucumberNode childNode = createCucumberNode(child, currentParent);
        nodeMap.put(child.getLocation().getLine(), childNode);
        for (Step step : child.getSteps()) {
            nodeMap.put(step.getLocation().getLine(), createCucumberNode(step, childNode));
        }
        if (!child.getExamples().isEmpty()) {
            processScenarioOutlineExamples(nodeMap, child, childNode);
        }
    }

    private void processBackgroundDefinition(
            final Map<Long, CucumberNode> nodeMap, final Background background, final CucumberNode currentParent
    ) {
        final CucumberNode childNode = createCucumberNode(background, currentParent);
        nodeMap.put(background.getLocation().getLine(), childNode);
        for (Step step : background.getSteps()) {
            nodeMap.put(step.getLocation().getLine(), createCucumberNode(step, childNode));
        }
    }

    private void processRuleDefinition(
            final Map<Long, CucumberNode> nodeMap, final RuleChild child, final CucumberNode currentParent) {
        child.getBackground().ifPresent(background -> processBackgroundDefinition(nodeMap, background, currentParent));
        child.getScenario().ifPresent(scenario -> processScenarioDefinition(nodeMap, scenario, currentParent));
    }

    private void processScenarioOutlineExamples(final Map<Long, CucumberNode> nodeMap,
                                                final Scenario scenarioOutline,
                                                final CucumberNode parent) {
        for (Examples examples : scenarioOutline.getExamples()) {
            final CucumberNode examplesNode = createCucumberNode(examples, parent);
            final Optional<TableRow> headerRowOptional = examples.getTableHeader();
            if (!headerRowOptional.isPresent()) {
                log.error("HeaderRow is null");
                throw new AssertionError();
            }
            TableRow headerRow = headerRowOptional.get();
            final CucumberNode headerNode = createCucumberNode(headerRow, examplesNode);
            nodeMap.put(headerRow.getLocation().getLine(), headerNode);
            for (int i = 0; i < examples.getTableBody().size(); ++i) {
                final TableRow examplesRow = examples.getTableBody().get(i);
                final CucumberNode expandedScenarioNode = createCucumberNode(examplesRow, examplesNode);
                nodeMap.put(examplesRow.getLocation().getLine(), expandedScenarioNode);
            }
        }
    }

    private static CucumberNode createCucumberNode(final Object node, final CucumberNode astNode) {
        return new CucumberNode(node, astNode);
    }

    private static class CucumberNode {
        private final Object node;
        private final CucumberNode parent;

        CucumberNode(final Object node, final CucumberNode parent) {
            this.node = node;
            this.parent = parent;
        }
    }
}
