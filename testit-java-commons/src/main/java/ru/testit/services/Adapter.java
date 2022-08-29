package ru.testit.services;

import ru.testit.models.LinkItem;
import ru.testit.models.LinkType;
import ru.testit.properties.AppProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public final class Adapter {
    private static AdapterManager adapterManager;
    private static ResultStorage storage;

    public static AdapterManager getAdapterManager() {
        if (Objects.isNull(adapterManager)) {
            Properties appProperties = AppProperties.loadProperties();
            ConfigManager manager = new ConfigManager(appProperties);
            adapterManager = new AdapterManager(manager);
        }
        return adapterManager;
    }

    public static ResultStorage getResultStorage() {
        if (Objects.isNull(storage)) {
            storage = new ResultStorage();
        }
        return storage;
    }

    /**
     * @deprecated This method is no longer acceptable to compute time between versions.
     * <p> Use {@link Adapter#addLink(String, String, String, LinkType)} addLink()} instead.
     */
    @Deprecated
    public static void link(final String title, final String description, final LinkType type, final String url) {
        final LinkItem link = new LinkItem().setTitle(title).setDescription(description).setType(type).setUrl(url);
        getAdapterManager().updateTestCase(testResult -> testResult.getResultLinks().add(link));
    }

    public static void addLink(final String url, final String title, final String description, final LinkType type) {
        LinkItem link = new LinkItem().setTitle(title)
                .setDescription(description)
                .setType(type)
                .setUrl(url);
        addLinks(new ArrayList<LinkItem>() {{
            add(link);
        }});
    }

    public static void addLinks(List<LinkItem> links) {
        getAdapterManager().updateTestCase(testResult -> testResult.getResultLinks().addAll(links));
    }

    public static void addAttachments(List<String> attachments) {
        getAdapterManager().addAttachments(attachments);
    }

    public static void addAttachment(String attachment) {
        getAdapterManager().addAttachments(new ArrayList<String>() {{
            add(attachment);
        }});
    }

    public static void addMessage(String message) {
        getAdapterManager().updateTestCase(testResult -> testResult.setMessage(message));
    }
}
