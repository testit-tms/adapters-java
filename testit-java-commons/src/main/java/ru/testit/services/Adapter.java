package ru.testit.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.models.Label;
import ru.testit.models.LinkItem;
import ru.testit.models.LinkType;
import ru.testit.properties.AppProperties;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class Adapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Adapter.class);
    private static AdapterManager adapterManager;
    private static ResultStorage storage;

    private Adapter() {}

    public static AdapterManager getAdapterManager() {
        if (Objects.isNull(adapterManager)) {
            Properties appProperties = AppProperties.loadProperties();
            ConfigManager manager = new ConfigManager(appProperties);
            adapterManager = new AdapterManager(manager.getClientConfiguration(), manager.getAdapterConfig());
        }
        return adapterManager;
    }

    public static ResultStorage getResultStorage() {
        if (Objects.isNull(storage)) {
            storage = new ResultStorage();
        }
        return storage;
    }

    public static void addLinks(final String url, final String title, final String description, final LinkType type) {
        LinkItem link = new LinkItem().setTitle(title)
                .setDescription(description)
                .setType(type)
                .setUrl(url);

        List<LinkItem> links = new ArrayList<>();
        links.add(link);

        addLinks(links);
    }

    public static void addLinks(List<LinkItem> links) {
        getAdapterManager().updateTestCase(testResult -> testResult.getResultLinks().addAll(links));
    }

    public static void addAttachments(List<String> attachments) {
        getAdapterManager().addAttachments(attachments);
    }

    public static void addAttachments(String attachment) {
        List<String> attachments = new ArrayList<>();
        attachments.add(attachment);

        addAttachments(attachments);
    }

    public static void addAttachments(String content, String fileName) {
        if (fileName == null) {
            fileName = UUID.randomUUID() + "-attachment.txt";
        }

        Path path = Paths.get(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset())) {
            writer.write(content);
        } catch (IOException e) {
            LOGGER.error(String.format("Can not write file '%s':", fileName), e);
        }

        addAttachments(fileName);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.error(String.format("Can not delete file '%s':", fileName), e);
        }
    }

    public static void addAttachments(String fileName, InputStream inputStream) {
        if (fileName == null) {
            LOGGER.error("Attachment name is empty");
            return;
        }

        Path path = Paths.get(fileName);
        try {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error(String.format("Can not write file '%s':", fileName), e);
        }

        addAttachments(fileName);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.error(String.format("Can not delete file '%s':", fileName), e);
        }
    }

    public static void addMessage(String message) {
        getAdapterManager().updateTestCase(testResult -> testResult.setMessage(message));
    }

    public static void addParameters(Map<String, String> parameters) {
        getAdapterManager().addParameters(parameters);
    }

    public static void addParameter(String key, String value) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(key, value);

        addParameters(parameters);
    }

    public static void addTitle(String title) {
        getAdapterManager().addTitle(title);
    }

    public static void addDisplayName(String displayName) {
        getAdapterManager().updateTestCase(testResult -> testResult.setName(displayName));
    }

    public static void addDescription(String description) {
        getAdapterManager().addDescription(description);
    }

    public static void addNameSpace(String nameSpace) {
        getAdapterManager().updateTestCase(testResult -> testResult.setSpaceName(nameSpace));
    }

    public static void addClassName(String className) {
        getAdapterManager().updateTestCase(testResult -> testResult.setClassName(className));
    }

    public static void addExternalId(String externalId) {
        getAdapterManager().updateTestCase(testResult -> testResult.setExternalId(externalId));
    }

    public static void addWorkItemIds(String[] workItemIds) {
        getAdapterManager().updateTestCase(testResult -> testResult.setWorkItemIds(Arrays.asList(workItemIds)));
    }

    public static void addLabels(String[] labels) {
        final List<Label> modifyLabels = new LinkedList<>();

        for (final String label : labels) {
            final Label modifyLabel = new Label().setName(label);
            modifyLabels.add(modifyLabel);
        }

        getAdapterManager().updateTestCase(testResult -> testResult.setLabels(modifyLabels));
    }
}
