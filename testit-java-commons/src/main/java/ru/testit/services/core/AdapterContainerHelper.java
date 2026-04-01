package ru.testit.services.core;

import org.slf4j.Logger;
import ru.testit.models.ClassContainer;
import ru.testit.models.MainContainer;
import ru.testit.properties.AdapterConfig;
import ru.testit.services.ResultStorage;
import ru.testit.syncstorage.SyncStorageService;
import ru.testit.writers.Writer;

import java.util.Optional;
import java.util.function.Consumer;

public class AdapterContainerHelper {

    private final AdapterConfig adapterConfig;
    private final ResultStorage storage;
    private final Writer writer;
    private final Logger logger;
    private final SyncStorageService syncStorageService;

    public AdapterContainerHelper(
            AdapterConfig adapterConfig,
            ResultStorage storage,
            Writer writer,
            Logger logger,
            SyncStorageService syncStorageService
    ) {
        this.adapterConfig = adapterConfig;
        this.storage = storage;
        this.writer = writer;
        this.logger = logger;
        this.syncStorageService = syncStorageService;
    }

    public void startMainContainer(final MainContainer container) {
        if (!isTmsEnabled()) {
            return;
        }

        container.setStart(System.currentTimeMillis());
        storage.put(container.getUuid(), container);

        if (logger.isDebugEnabled()) {
            logger.debug("Start new main container {}", container);
        }
        logger.info("Set in progress worker status");
        syncStorageService.setWorkerStatus("in_progress");
    }

    public void stopMainContainer(final String uuid) {
        if (!isTmsEnabled()) {
            return;
        }

        final Optional<MainContainer> found = storage.getTestsContainer(uuid);
        if (!found.isPresent()) {
            logger.error(
                    "Could not stop main container: container with uuid {} not found",
                    uuid
            );
            return;
        }
        final MainContainer container = found.get();
        container.setStop(System.currentTimeMillis());

        if (logger.isDebugEnabled()) {
            logger.debug("Stop main container {}", container);
        }

        writer.writeTests(container);

        logger.info("End of main container, set completed");
        syncStorageService.setWorkerStatus("completed");
    }

    public void startClassContainer(
            final String parentUuid,
            final ClassContainer container
    ) {
        if (!isTmsEnabled()) {
            return;
        }

        storage.getTestsContainer(parentUuid).ifPresent(parent ->
                storage.updateIfPresent(
                        parentUuid,
                        MainContainer.class,
                        p -> p.getChildren().add(container.getUuid())
                )
        );
        container.setStart(System.currentTimeMillis());
        storage.put(container.getUuid(), container);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Start new class container {} for parent {}",
                    container,
                    parentUuid
            );
        }
        syncStorageService.setWorkerStatus("in_progress");
    }

    public void stopClassContainer(final String uuid) {
        if (!isTmsEnabled()) {
            return;
        }

        final Optional<ClassContainer> found = storage.getClassContainer(uuid);
        if (!found.isPresent()) {
            logger.debug(
                    "Could not stop class container: container with uuid {} not found",
                    uuid
            );
            return;
        }
        final ClassContainer container = found.get();
        container.setStop(System.currentTimeMillis());

        if (logger.isDebugEnabled()) {
            logger.debug("Stop class container {}", container);
        }

        writer.writeClass(container);

        logger.info("End of class container");
    }

    public void updateClassContainer(
            final String uuid,
            final Consumer<ClassContainer> update
    ) {
        if (!isTmsEnabled()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Update class container {}", uuid);
        }

        final Optional<ClassContainer> found = storage.getClassContainer(uuid);
        if (!found.isPresent()) {
            logger.warn(
                    "Could not update class container: no container with uuid {}. "
                            + "Test UUIDs scheduled after this will not be added to class children; "
                            + "with importRealtime=false bulk import will skip those tests. "
                            + "Check lifecycle order and parallel execution.",
                    uuid
            );
            return;
        }
        update.accept(found.get());
    }

    private boolean isTmsEnabled() {
        return adapterConfig.shouldEnableTmsIntegration();
    }
}
