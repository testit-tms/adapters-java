package ru.testit.listener;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.testit.models.Label;
import ru.testit.models.LinkItem;
import ru.testit.models.LinkType;
import ru.testit.services.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TagParser {
    private static final String TAG_VALUE_DELIMITER = ",";
    private static final String EXTERNAL_ID = "ExternalId";
    private static final String TITLE = "Title";
    private static final String DISPLAY_NAME = "DisplayName";
    private static final String DESCRIPTION = "Description";
    private static final String LABELS = "Labels";
    private static final String TAGS = "Tags";
    private static final String LINKS = "Links";
    private static final String WORK_ITEM_IDS = "WorkItemIds";

    private final List<Label> labelList = new ArrayList<>();
    private final List<String> tagList = new ArrayList<>();
    private final List<LinkItem> linkItemList = new ArrayList<>();
    private final List<String> workItemIdList = new ArrayList<>();
    private String externalIdValue;
    private String displayNameValue;
    private final String titleValue;
    private final String descriptionValue;

    TagParser(final Story story, final Scenario scenario) {
        this(story, scenario, null);
    }

    TagParser(final Story story, final Scenario scenario, final Map<String, String> exampleParameters) {
        Meta storyMeta = story.getMeta();
        Meta scenarioMeta = scenario.getMeta();

        externalIdValue = substituteExampleParameters(
                getMetaValue(storyMeta, scenarioMeta, EXTERNAL_ID),
                exampleParameters
        );
        String titleRaw = substituteExampleParameters(
                getMetaValue(storyMeta, scenarioMeta, TITLE),
                exampleParameters
        );
        displayNameValue = substituteExampleParameters(
                getMetaValue(storyMeta, scenarioMeta, DISPLAY_NAME),
                exampleParameters
        );
        String descriptionRaw = substituteExampleParameters(
                getMetaValue(storyMeta, scenarioMeta, DESCRIPTION),
                exampleParameters
        );
        titleValue = titleRaw;
        descriptionValue = descriptionRaw;

        String labelsValue = substituteExampleParameters(
                getMetaValue(storyMeta, scenarioMeta, LABELS),
                exampleParameters
        );

        if (!labelsValue.isEmpty()) {
            Arrays.stream(labelsValue.split(TAG_VALUE_DELIMITER))
                    .forEach(label -> getLabelList().add(getTagLabel(label)));
        }

        String tagsValue = substituteExampleParameters(
                getMetaValue(storyMeta, scenarioMeta, TAGS),
                exampleParameters
        );

        if (!tagsValue.isEmpty()) {
            Arrays.stream(tagsValue.split(TAG_VALUE_DELIMITER))
                    .forEach(tag -> getTagList().add(tag));
        }

        String linksValue = substituteExampleParameters(
                getMetaValue(storyMeta, scenarioMeta, LINKS),
                exampleParameters
        );

        if (isJson(linksValue)) {
            getLinkItemList().add(getLinkItem(linksValue));
        } else if (isJsonArray(linksValue)) {
            getLinkItemList().addAll(getLinkItems(linksValue));
        }

        String workItemIdsValue = substituteExampleParameters(
                getMetaValue(storyMeta, scenarioMeta, WORK_ITEM_IDS),
                exampleParameters
        );

        if (!workItemIdsValue.isEmpty()) {
            Arrays.stream(workItemIdsValue.split(TAG_VALUE_DELIMITER))
                    .forEach(id -> getWorkItemIdList().add(id));
        }

        final String name = scenario.getTitle();

        if (externalIdValue.isEmpty()) {
            String key = story.getPath() + name;
            if (exampleParameters != null && !exampleParameters.isEmpty()) {
                key += exampleParameters.toString();
            }
            externalIdValue = Utils.getHash(key);
        }

        if (displayNameValue.isEmpty()) {
            displayNameValue = name;
        }
    }

    private String getMetaValue(Meta storyMeta, Meta scenarioMeta, String key) {
        if (storyMeta.hasProperty(key)) {
            return storyMeta.getProperty(key);
        }

        if (scenarioMeta.hasProperty(key)) {
            return scenarioMeta.getProperty(key);
        }

        return "";
    }

    /**
     * Fills {@code {name}} and {@code <name>} from Examples column names (JBehave example row map keys).
     */
    static String substituteExampleParameters(String value, Map<String, String> exampleParameters) {
        if (value == null || value.isEmpty() || exampleParameters == null || exampleParameters.isEmpty()) {
            return value == null ? "" : value;
        }
        String result = value;
        for (Map.Entry<String, String> e : exampleParameters.entrySet()) {
            String key = e.getKey();
            if (key == null) {
                continue;
            }
            String v = e.getValue() != null ? e.getValue() : "";
            result = result.replace("{" + key + "}", v);
            result = result.replace("<" + key + ">", v);
        }
        return result;
    }

    public List<Label> getLabelList() {
        return labelList;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public List<LinkItem> getLinkItemList() {
        return linkItemList;
    }

    public List<String> getWorkItemIdList() {
        return workItemIdList;
    }

    public String getExternalIdValue() {
        return externalIdValue;
    }

    public String getTitleValue() {
        return titleValue;
    }

    public String getDisplayNameValue() {
        return displayNameValue;
    }

    public String getDescriptionValue() {
        return descriptionValue;
    }

    private Label getTagLabel(final String tag) {
        return new Label().setName(tag);
    }

    private boolean isJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    private boolean isJsonArray(String json) {
        try {
            new JSONArray(json);
        } catch (JSONException ex1) {
            return false;
        }

        return true;
    }

    private LinkItem getLinkItem(String json) {
        JSONObject link = new JSONObject(json);

        return new LinkItem()
                .setUrl(link.getString("url"))
                .setDescription(link.getString("description"))
                .setTitle(link.getString("title"))
                .setType(LinkType.fromString(link.getString("type")));
    }

    private List<LinkItem> getLinkItems(String json) {
        List<LinkItem> items = new ArrayList<>();
        JSONArray linksArr = new JSONArray(json);
        for (int i = 0; i < linksArr.length(); i++) {
            items.add(
                    new LinkItem()
                            .setUrl(linksArr.getJSONObject(i).getString("url"))
                            .setDescription(linksArr.getJSONObject(i).getString("description"))
                            .setTitle(linksArr.getJSONObject(i).getString("title"))
                            .setType(LinkType.fromString(linksArr.getJSONObject(i).getString("type")))
            );
        }
        return items;
    }
}
