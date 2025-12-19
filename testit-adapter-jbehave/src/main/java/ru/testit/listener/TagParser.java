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

public class TagParser {
    private static final String TAG_VALUE_DELIMITER = ",";
    private static final String EXTERNAL_ID = "ExternalId";
    private static final String TITLE = "Title";
    private static final String DISPLAY_NAME = "DisplayName";
    private static final String DESCRIPTION = "Description";
    private static final String LABELS = "Labels";
    private static final String LINKS = "Links";
    private static final String WORK_ITEM_IDS = "WorkItemIds";

    private final List<Label> labelList = new ArrayList<>();
    private final List<LinkItem> linkItemList = new ArrayList<>();
    private final List<String> workItemIdList = new ArrayList<>();
    private String externalIdValue;
    private String displayNameValue;
    private final String titleValue;
    private final String descriptionValue;

    TagParser(final Story story, final Scenario scenario) {
        Meta storyMeta = story.getMeta();
        Meta scenarioMeta = scenario.getMeta();

        externalIdValue = getMetaValue(storyMeta, scenarioMeta, EXTERNAL_ID);
        titleValue = getMetaValue(storyMeta, scenarioMeta, TITLE);
        displayNameValue = getMetaValue(storyMeta, scenarioMeta, DISPLAY_NAME);
        descriptionValue = getMetaValue(storyMeta, scenarioMeta, DESCRIPTION);

        String labelsValue = getMetaValue(storyMeta, scenarioMeta, LABELS);

        if (!labelsValue.isEmpty()) {
            Arrays.stream(labelsValue.split(TAG_VALUE_DELIMITER))
                    .forEach(label -> getLabelList().add(getTagLabel(label)));
        }

        String linksValue = getMetaValue(storyMeta, scenarioMeta, LINKS);

        if (isJson(linksValue)) {
            getLinkItemList().add(getLinkItem(linksValue));
        } else if (isJsonArray(linksValue)) {
            getLinkItemList().addAll(getLinkItems(linksValue));
        }

        String workItemIdsValue = getMetaValue(storyMeta, scenarioMeta, WORK_ITEM_IDS);

        if (!workItemIdsValue.isEmpty()) {
            Arrays.stream(workItemIdsValue.split(TAG_VALUE_DELIMITER))
                    .forEach(id -> getWorkItemIdList().add(id));
        }

        final String name = scenario.getTitle();

        if (externalIdValue.isEmpty()) {
            externalIdValue = Utils.getHash(story.getPath() + name);
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

    public List<Label> getLabelList() {
        return labelList;
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
