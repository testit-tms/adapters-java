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

    private final List<Label> labels = new ArrayList<>();
    private final List<LinkItem> links = new ArrayList<>();
    private final List<String> workItemIds = new ArrayList<>();
    private String externalId;
    private String displayName;
    private final String title;
    private final String description;

    TagParser(final Story story, final Scenario scenario) {
        Meta storyMeta = story.getMeta();
        Meta scenarioMeta = scenario.getMeta();

        externalId = getMetaValue(storyMeta, scenarioMeta, EXTERNAL_ID);
        title = getMetaValue(storyMeta, scenarioMeta, TITLE);
        displayName = getMetaValue(storyMeta, scenarioMeta, DISPLAY_NAME);
        description = getMetaValue(storyMeta, scenarioMeta, DESCRIPTION);

        String labelsValue = getMetaValue(storyMeta, scenarioMeta, LABELS);

        if (!labelsValue.isEmpty()) {
            Arrays.stream(labelsValue.split(TAG_VALUE_DELIMITER))
                    .forEach(label -> getLabels().add(getTagLabel(label)));
        }

        String linksValue = getMetaValue(storyMeta, scenarioMeta, LINKS);

        if (isJson(linksValue)) {
            getLinks().add(getLinkItem(linksValue));
        } else if (isJsonArray(linksValue)) {
            getLinks().addAll(getLinkItems(linksValue));
        }

        String workItemIdsValue = getMetaValue(storyMeta, scenarioMeta, WORK_ITEM_IDS);

        if (!workItemIdsValue.isEmpty()) {
            Arrays.stream(workItemIdsValue.split(TAG_VALUE_DELIMITER))
                    .forEach(id -> getWorkItemIds().add(id));
        }

        final String name = scenario.getTitle();

        if (externalId.isEmpty()) {
            externalId = Utils.getHash(story.getPath() + name);
        }

        if (displayName.isEmpty()) {
            displayName = name;
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

    public List<Label> getLabels() {
        return labels;
    }

    public List<LinkItem> getLinks() {
        return links;
    }

    public List<String> getWorkItemIds() {
        return workItemIds;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getTitle() {
        return title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    private Label getTagLabel(final String tag) {
        return new Label().setName(tag);
    }

    private Boolean isJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    private Boolean isJsonArray(String json) {
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
        JSONArray links = new JSONArray(json);
        for (int i = 0; i < links.length(); i++) {
            items.add(
                    new LinkItem()
                            .setUrl(links.getJSONObject(i).getString("url"))
                            .setDescription(links.getJSONObject(i).getString("description"))
                            .setTitle(links.getJSONObject(i).getString("title"))
                            .setType(LinkType.fromString(links.getJSONObject(i).getString("type")))
            );
        }
        return items;
    }
}
