package ru.testit.listener;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import ru.testit.models.TestResult;

import java.util.Map;

public class ScenarioParser {

    private ScenarioParser() {}

    public static TestResult parseScenario(final Story story, final Scenario scenario, Map<String, String> parameters) {
        final TagParser tagParser = new TagParser(story, scenario);
        final String featureName = story.getName();

        String nameSpace = story.getPath().split(featureName)[0];
        nameSpace = nameSpace.substring(0, nameSpace.length() - 1);

        return new TestResult()
                .setExternalId(tagParser.getExternalIdValue())
                .setName(tagParser.getDisplayNameValue())
                .setTitle(tagParser.getTitleValue())
                .setDescription(tagParser.getDescriptionValue())
                .setWorkItemIds(tagParser.getWorkItemIdList())
                .setClassName(featureName)
                .setSpaceName(nameSpace)
                .setLabels(tagParser.getLabelList())
                .setLinkItems(tagParser.getLinkItemList())
                .setParameters(parameters);
    }
}
