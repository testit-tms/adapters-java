package ru.testit.services.core;

import org.slf4j.Logger;
import ru.testit.models.ClassContainer;
import ru.testit.models.FixtureResult;
import ru.testit.models.ItemStage;
import ru.testit.models.MainContainer;
import ru.testit.properties.AdapterConfig;
import ru.testit.services.ResultStorage;
import ru.testit.services.ThreadContext;

import java.util.Optional;
import java.util.function.Consumer;

public class AdapterFixtureHelper {

    private final AdapterConfig adapterConfig;
    private final ThreadContext threadContext;
    private final ResultStorage storage;
    private final Logger logger;

    public AdapterFixtureHelper(
            AdapterConfig adapterConfig,
            ThreadContext threadContext,
            ResultStorage storage,
            Logger logger
    ) {
        this.adapterConfig = adapterConfig;
        this.threadContext = threadContext;
        this.storage = storage;
        this.logger = logger;
    }

    public void startPrepareFixtureAll(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        if (!isTmsEnabled()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Start prepare all fixture {} for parent {}",
                    result,
                    parentUuid
            );
        }

        storage.getTestsContainer(parentUuid).ifPresent(container ->
                storage.updateIfPresent(
                        parentUuid,
                        MainContainer.class,
                        c -> c.getBeforeMethods().add(result)
                )
        );
        startFixture(uuid, result);
    }

    public void startTearDownFixtureAll(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        if (!isTmsEnabled()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Start tear down all fixture {} for parent {}",
                    result,
                    parentUuid
            );
        }

        storage.getTestsContainer(parentUuid).ifPresent(container ->
                storage.updateIfPresent(
                        parentUuid,
                        MainContainer.class,
                        c -> c.getAfterMethods().add(result)
                )
        );

        startFixture(uuid, result);
    }

    public void startPrepareFixture(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        if (!isTmsEnabled()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Start prepare fixture {} for parent {}",
                    result,
                    parentUuid
            );
        }

        storage.getClassContainer(parentUuid).ifPresent(container ->
                storage.updateIfPresent(
                        parentUuid,
                        ClassContainer.class,
                        c -> c.getBeforeClassMethods().add(result)
                )
        );

        startFixture(uuid, result);
    }

    public void startTearDownFixture(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        if (!isTmsEnabled()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Start tear down fixture {} for parent {}",
                    result,
                    parentUuid
            );
        }

        storage.getClassContainer(parentUuid).ifPresent(container ->
                storage.updateIfPresent(
                        parentUuid,
                        ClassContainer.class,
                        c -> c.getAfterClassMethods().add(result)
                )
        );

        startFixture(uuid, result);
    }

    public void startPrepareFixtureEachTest(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        if (!isTmsEnabled()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Start prepare for each fixture {} for parent {}",
                    result,
                    parentUuid
            );
        }

        storage.getClassContainer(parentUuid).ifPresent(container ->
                storage.updateIfPresent(
                        parentUuid,
                        ClassContainer.class,
                        c -> c.getBeforeEachTest().add(result)
                )
        );

        startFixture(uuid, result);
    }

    public void startTearDownFixtureEachTest(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        if (!isTmsEnabled()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Start tear down for each fixture {} for parent {}",
                    result,
                    parentUuid
            );
        }

        storage.getClassContainer(parentUuid).ifPresent(container ->
                storage.updateIfPresent(
                        parentUuid,
                        ClassContainer.class,
                        c -> c.getAfterEachTest().add(result)
                )
        );

        startFixture(uuid, result);
    }

    public void updateFixture(
            final String uuid,
            final Consumer<FixtureResult> update
    ) {
        if (!isTmsEnabled()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Update fixture {}", uuid);
        }

        final Optional<FixtureResult> found = storage.getFixture(uuid);
        if (!found.isPresent()) {
            logger.error(
                    "Could not update test fixture: test fixture with uuid {} not found",
                    uuid
            );
            return;
        }

        update.accept(found.get());
    }

    public void stopFixture(final String uuid) {
        if (!isTmsEnabled()) {
            return;
        }

        final Optional<FixtureResult> found = storage.getFixture(uuid);
        if (!found.isPresent()) {
            logger.error(
                    "Could not stop test fixture: test fixture with uuid {} not found",
                    uuid
            );
            return;
        }
        final FixtureResult fixture = found.get();

        fixture
                .setItemStage(ItemStage.FINISHED)
                .setStop(System.currentTimeMillis());

        storage.remove(uuid);
        threadContext.clear();

        if (logger.isDebugEnabled()) {
            logger.debug("Stop fixture {}", fixture);
        }
    }

    private void startFixture(final String uuid, final FixtureResult result) {
        if (!isTmsEnabled()) {
            return;
        }

        storage.put(uuid, result);

        result
                .setItemStage(ItemStage.RUNNING)
                .setStart(System.currentTimeMillis());

        threadContext.clear();
        threadContext.start(uuid);
    }

    private boolean isTmsEnabled() {
        return adapterConfig.shouldEnableTmsIntegration();
    }
}
