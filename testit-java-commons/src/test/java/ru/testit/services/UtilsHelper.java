package ru.testit.services;

import ru.testit.models.Label;
import ru.testit.models.LinkItem;
import ru.testit.models.LinkType;

import java.util.*;

public class UtilsHelper {

    public static Map<String, String> generateParameters() {
        Map<String, String> parameters = new HashMap<>();

        parameters.put("number", "121");
        parameters.put("name", "Test №23");
        parameters.put("date", "21 Oct 2020");

        return parameters;
    }

    public static String generateTextBeforeSetParameters(Map<String, String> parameters) {
        StringBuilder text = new StringBuilder();

        for(String key : parameters.keySet()) {
            text
                .append("{Param ")
                .append(key)
                .append("} = {")
                .append(key)
                .append("}; ");
        }

        return text.toString();
    }

    public static String generateTextAfterSetParameters(Map<String, String> parameters) {
        StringBuilder text = new StringBuilder();

        for(String key : parameters.keySet()) {
            text
                .append("{Param ")
                .append(key)
                .append("} = ")
                .append(parameters.get(key))
                .append("; ");
        }

        return text.toString();
    }

    public static List<LinkItem> generateLinkItemsBeforeSetParameters(Map<String, String> parameters) {
        List<LinkItem> links = new ArrayList<>();

        for(String key : parameters.keySet()) {
            links.add(new LinkItem()
                    .setTitle("{Title " + key + "} = {" + key + "}; ")
                    .setDescription("{Description " + key + "} = {" + key + "}; ")
                    .setType(LinkType.ISSUE)
                    .setUrl("{Url " + key + "} = {" + key + "}; "));
        }

        return links;
    }

    public static List<LinkItem> generateLinkItemsAfterSetParameters(Map<String, String> parameters) {
        List<LinkItem> links = new ArrayList<>();

        for(String key : parameters.keySet()) {
            links.add(new LinkItem()
                    .setTitle("{Title " + key + "} = " + parameters.get(key) + "; ")
                    .setDescription("{Description " + key + "} = " + parameters.get(key) + "; ")
                    .setType(LinkType.ISSUE)
                    .setUrl("{Url " + key + "} = " + parameters.get(key) + "; "));
        }

        return links;
    }

    public static String[] generateLabelsBeforeSetParameters(Map<String, String> parameters) {
        List<String> labels = new ArrayList<>();

        for(String key : parameters.keySet()) {
            labels.add("{Param " + key + "} = {" + key + "}; ");
        }

        return labels.toArray(new String[labels.size()]);
    }

    public static List<Label> generateLabelsAfterSetParameters(Map<String, String> parameters) {
        List<Label> labels = new ArrayList<>();

        for(String key : parameters.keySet()) {
            labels.add(new Label()
                    .setName("{Param " + key + "} = " + parameters.get(key) + "; "));
        }

        return labels;
    }
}
