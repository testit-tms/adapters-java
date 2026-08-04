package ru.testit.services.core;

import org.slf4j.Logger;
import ru.testit.models.FixtureResult;
import ru.testit.models.ResultWithAttachments;
import ru.testit.models.ResultWithDescription;
import ru.testit.models.ResultWithParameters;
import ru.testit.models.ResultWithTitle;
import ru.testit.models.TestResult;
import ru.testit.properties.AdapterConfig;
import ru.testit.services.ResultStorage;
import ru.testit.services.ThreadContext;
import ru.testit.writers.Writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdapterMetadataHelper {

    private final AdapterConfig adapterConfig;
    private final ThreadContext threadContext;
    private final ResultStorage storage;
    private final Writer writer;
    private final Logger logger;

    public AdapterMetadataHelper(
            AdapterConfig adapterConfig,
            ThreadContext threadContext,
            ResultStorage storage,
            Writer writer,
            Logger logger
    ) {
        this.adapterConfig = adapterConfig;
        this.threadContext = threadContext;
        this.storage = storage;
        this.writer = writer;
        this.logger = logger;
    }

    public void addAttachments(List<String> attachments) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        List<String> uuids = new ArrayList<>();
        for (final String attachment : attachments) {
            String attachmentsId = writer.writeAttachment(attachment);
            if (attachmentsId.isEmpty()) {
                return;
            }
            uuids.add(attachmentsId);
        }

        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            logger.error("Could not add attachment: no test is running");
            return;
        }

        // During Cucumber/TestNG @After hooks the current context is a fixture.
        // Attachments must land on the parent test result so they appear in the
        // Attachments tab and stay linked (not orphan-cleaned).
        final String targetUuid = resolveAttachmentTargetUuid(current.get());

        storage.get(targetUuid, ResultWithAttachments.class).ifPresent(
                result -> storage.updateIfPresent(
                        targetUuid,
                        ResultWithAttachments.class,
                        r -> r.getAttachments().addAll(uuids)
                )
        );
    }

    /**
     * If the current context is a fixture with a known parent test case,
     * return that test case uuid; otherwise return the current uuid.
     */
    private String resolveAttachmentTargetUuid(final String currentUuid) {
        final Optional<FixtureResult> fixture = storage.getFixture(currentUuid);
        if (!fixture.isPresent()) {
            return currentUuid;
        }

        final String parentUuid = fixture.get().getParent();
        if (parentUuid == null || parentUuid.isEmpty()) {
            return currentUuid;
        }

        final Optional<TestResult> parentTest = storage.getTestResult(parentUuid);
        if (!parentTest.isPresent()) {
            logger.warn(
                    "Fixture {} has parent {}, but test result is missing; attaching to fixture",
                    currentUuid,
                    parentUuid
            );
            return currentUuid;
        }

        return parentUuid;
    }

    public void addParameters(Map<String, String> parameters) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            logger.error("Could not add parameter: no test is running");
            return;
        }

        storage.get(current.get(), ResultWithParameters.class).ifPresent(
                result -> storage.updateIfPresent(
                        current.get(),
                        ResultWithParameters.class,
                        r -> r.getParameters().putAll(parameters)
                )
        );
    }

    public void addTitle(String title) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            logger.error("Could not set title: no test is running");
            return;
        }

        storage.get(current.get(), ResultWithTitle.class).ifPresent(
                result -> storage.updateIfPresent(
                        current.get(),
                        ResultWithTitle.class,
                        r -> r.setTitle(title)
                )
        );
    }

    public void addDescription(String description) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            logger.error("Could not set description: no test is running");
            return;
        }

        storage.get(current.get(), ResultWithDescription.class).ifPresent(
                result -> storage.updateIfPresent(
                        current.get(),
                        ResultWithDescription.class,
                        r -> r.setDescription(description)
                )
        );
    }
}
