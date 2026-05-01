package ru.testit.services.core;

import org.slf4j.Logger;
import ru.testit.models.ClassContainer;
import ru.testit.models.MainContainer;
import ru.testit.properties.AdapterConfig;
import ru.testit.services.ResultStorage;
import ru.testit.syncstorage.SyncStorageService;
import ru.testit.writers.Writer;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class AdapterContainerHelper {

    private final AdapterConfig adapterConfig;
    private final ResultStorage storage;
    private final Writer writer;
    private final Logger logger;
    private final SyncStorageService syncStorageService;

    private final AtomicInteger activeMainContainers = new AtomicInteger(0);
    private final Set<String> finalizedMainContainers = ConcurrentHashMap.newKeySet();
    /** Uuids registered by a non-duplicate {@link #startMainContainer}; drives refcount on stop. */
    private final Set<String> registeredMainContainers = ConcurrentHashMap.newKeySet();

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

        final String uuid = container.getUuid();
        if (storage.getTestsContainer(uuid).isPresent()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Main container {} already registered, skip duplicate start (e.g. nested @BeforeAll)", uuid);
            }
            return;
        }

        container.setStart(System.currentTimeMillis());
        storage.put(uuid, container);
        registeredMainContainers.add(uuid);
        activeMainContainers.incrementAndGet();

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

        if (!finalizedMainContainers.add(uuid)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Main container {} already finalized, skip duplicate stop", uuid);
            }
            return;
        }

        final Optional<MainContainer> found = storage.getTestsContainer(uuid);
        if (!found.isPresent()) {
            finalizedMainContainers.remove(uuid);
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

        logger.info(
                "TMS finalize: mainUuid={}, classContainersInMain={}",
                uuid,
                container.getChildren().size()
        );

        try {
            writer.writeTests(container);
        } catch (RuntimeException e) {
            finalizedMainContainers.remove(uuid);
            throw e;
        }

        if (!registeredMainContainers.remove(uuid)) {
            logger.warn("stopMainContainer: no matching start for {}; skip worker refcount", uuid);
            return;
        }
        int remaining = activeMainContainers.decrementAndGet();
        logger.info("End of main container, remainingMainContainers={}", remaining);
        if (remaining == 0) {
            syncStorageService.setWorkerStatus("completed");
        }
    }

    public void startClassContainer(
            final String parentUuid,
            final ClassContainer container
    ) {
        if (!isTmsEnabled()) {
            return;
        }

        final String classUuid = container.getUuid();
        // Cucumber reuses the same class UUID per thread; do not replace storage entry or accumulated children.
        if (storage.getClassContainer(classUuid).isPresent()) {
            storage.updateIfPresent(classUuid, ClassContainer.class, c -> c.setStart(System.currentTimeMillis()));
            syncStorageService.setWorkerStatus("in_progress");
            return;
        }

        storage.getTestsContainer(parentUuid).ifPresent(parent ->
                storage.updateIfPresent(
                        parentUuid,
                        MainContainer.class,
                        p -> {
                            if (!p.getChildren().contains(classUuid)) {
                                p.getChildren().add(classUuid);
                            }
                        }
                )
        );
        container.setStart(System.currentTimeMillis());
        storage.put(classUuid, container);

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
