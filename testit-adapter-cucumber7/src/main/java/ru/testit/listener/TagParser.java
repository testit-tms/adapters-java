package ru.testit.listener;

import io.cucumber.messages.types.Feature;
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

    private final List<Label> labels = new ArrayList<>();
    private final List<LinkItem> links = new ArrayList<>();
    private final List<String> workItemIds = new ArrayList<>();
    private String externalId = "";
    private String title = "";
    private String displayName = "";
    private String description = "";

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
                        externalId = Utils.setParameters(tagValue, parameters);
                        break;
                    case TITLE:
                        title = Utils.setParameters(tagValue, parameters);
                        break;
                    case DISPLAY_NAME:
                        displayName = Utils.setParameters(tagValue, parameters);
                        break;
                    case DESCRIPTION:
                        description = Utils.setParameters(tagValue, parameters);
                        break;
                    case LABELS:
                        Arrays.stream(Utils.setParameters(tagValue, parameters).split(TAG_VALUE_DELIMITER))
                                .forEach(label -> getScenarioLabels().add(getTagLabel(label)));
                        break;
                    case LINKS:
                        if (isJson(Utils.setParameters(tagValue, parameters))) {
                            getScenarioLinks().add(getLinkItem(tagValue));
                        } else if (isJsonArray(Utils.setParameters(tagValue, parameters))) {
                            getScenarioLinks().addAll(getLinkItems(Utils.setParameters(tagValue, parameters)));
                        }
                        break;
                    case WORK_ITEM_IDS:
                        Arrays.stream(Utils.setParameters(tagValue, parameters).split(TAG_VALUE_DELIMITER))
                                .forEach(id -> getWorkItemIds().add(id));
                        break;
                }
            }
        }

        final String featureName = feature.getName();
        final String name = scenario.getName();

        if (externalId.isEmpty()) {
            externalId = Utils.getHash(featureName + name);
        }

        if (displayName.isEmpty()) {
            displayName = name;
        }
    }

    public List<Label> getScenarioLabels() {
        return labels;
    }

    public List<LinkItem> getScenarioLinks() {
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
