package ru.testit.listener;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import ru.testit.models.TestResult;

import java.util.Map;

public class ScenarioParser {

    static public TestResult parseScenario(final Story story, final Scenario scenario, Map<String, String> parameters) {
        final TagParser tagParser = new TagParser(story, scenario);
        final String featureName = story.getName();

        String nameSpace = story.getPath().split(featureName)[0];
        nameSpace = nameSpace.substring(0, nameSpace.length() - 1);

        return new TestResult()
                .setExternalId(tagParser.getExternalId())
                .setName(tagParser.getDisplayName())
                .setTitle(tagParser.getTitle())
                .setDescription(tagParser.getDescription())
                .setWorkItemId(tagParser.getWorkItemIds())
                .setClassName(featureName)
                .setSpaceName(nameSpace)
                .setLabels(tagParser.getLabels())
                .setLinkItems(tagParser.getLinks())
                .setParameters(parameters);
    }
}
