package ru.testit.listener;

import gherkin.ast.Feature;
import io.cucumber.plugin.event.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.testit.models.Label;
import ru.testit.models.LinkItem;
import ru.testit.models.LinkType;
import ru.testit.services.Utils;

import java.util.*;

public class TagParser {
    private static final String TAG_DELIMITER = "=";
    private static final String TAG_VALUE_DELIMITER = ",";
    private static final String EXTERNAL_ID = "@EXTERNALID";
    private static final String TITLE = "@TITLE";
    private static final String DISPLAY_NAME = "@DISPLAYNAME";
    private static final String DESCRIPTION = "@DESCRIPTION";
    private static final String LABELS = "@LABELS";
    private static final String LINKS = "@LINKS";
    private static final String WORK_ITEM_IDS = "@WORKITEMIDS";

    private final List<Label> labelList = new ArrayList<>();
    private final List<LinkItem> linkItemList = new ArrayList<>();
    private final List<String> workItemIdList = new ArrayList<>();
    private String externalIdValue = "";
    private String titleValue = "";
    private String displayNameValue = "";
    private String descriptionValue = "";

    TagParser(final Feature feature, final TestCase scenario, final Deque<String> tags, Map<String, String> parameters) {

        while (tags.peek() != null) {
            final String tag = tags.remove();

            if (tag.contains(TAG_DELIMITER)) {
                final String[] tagParts = tag.split(TAG_DELIMITER, 2);

                if (tagParts.length < 2 || Objects.isNull(tagParts[1]) || tagParts[1].isEmpty()) {
                    continue;
                }

                final String tagKey = tagParts[0].toUpperCase();
                final String tagValue = tagParts[1];

                switch (tagKey) {
                    case EXTERNAL_ID:
                        externalIdValue = Utils.setParameters(parseSpaceInTag(tagValue), parameters);
                        break;
                    case TITLE:
                        titleValue = Utils.setParameters(parseSpaceInTag(tagValue), parameters);
                        break;
                    case DISPLAY_NAME:
                        displayNameValue = Utils.setParameters(parseSpaceInTag(tagValue), parameters);
                        break;
                    case DESCRIPTION:
                        descriptionValue = Utils.setParameters(parseSpaceInTag(tagValue), parameters);
                        break;
                    case LABELS:
                        Arrays.stream(Utils.setParameters(parseSpaceInTag(tagValue), parameters).split(TAG_VALUE_DELIMITER))
                                .forEach(label -> getScenarioLabels().add(getTagLabel(label)));
                        break;
                    case LINKS:
                        if (isJson(tagValue)) {
                            getScenarioLinks().add(getLinkItem(tagValue));
                        } else if (isJsonArray(tagValue)) {
                            getScenarioLinks().addAll(getLinkItems(tagValue));
                        }
                        break;
                    case WORK_ITEM_IDS:
                        Arrays.stream(Utils.setParameters(parseSpaceInTag(tagValue), parameters).split(TAG_VALUE_DELIMITER))
                                .forEach(id -> getWorkItemIdList().add(id));
                        break;
                    default:
                        break;
                }
            }
        }

        final String featureName = feature.getName();
        final String name = scenario.getName();

        if (externalIdValue.isEmpty()) {
            externalIdValue = Utils.getHash(featureName + name);
        }

        if (displayNameValue.isEmpty()) {
            displayNameValue = name;
        }
    }

    public List<Label> getScenarioLabels() {
        return labelList;
    }

    public List<LinkItem> getScenarioLinks() {
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
                .setDescription(
                        parseSpaceInTag(link.getString("description")))
                .setTitle(
                        parseSpaceInTag(link.getString("title")))
                .setType(LinkType.fromString(link.getString("type")));
    }

    private List<LinkItem> getLinkItems(String json) {
        List<LinkItem> items = new ArrayList<>();
        JSONArray linksArr = new JSONArray(json);
        for (int i = 0; i < linksArr.length(); i++) {
            items.add(
                    new LinkItem()
                            .setUrl(linksArr.getJSONObject(i).getString("url"))
                            .setDescription(
                                    parseSpaceInTag(linksArr.getJSONObject(i).getString("description")))
                            .setTitle(
                                    parseSpaceInTag(linksArr.getJSONObject(i).getString("title")))
                            .setType(LinkType.fromString(linksArr.getJSONObject(i).getString("type")))
            );
        }
        return items;
    }

    private String parseSpaceInTag(String tag) {
        return tag.replace("\\_", " ");
    }
}
