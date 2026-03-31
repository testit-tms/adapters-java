package ru.testit.services;

import org.slf4j.Logger;
import ru.testit.models.ItemStage;
import ru.testit.models.ResultWithSteps;
import ru.testit.models.StepResult;
import ru.testit.properties.AdapterConfig;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class AdapterStepHelper {

    private final AdapterConfig adapterConfig;
    private final ThreadContext threadContext;
    private final ResultStorage storage;
    private final Logger logger;

    public AdapterStepHelper(
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

    public void startStep(final String uuid, final StepResult result) {
        if (!isTmsEnabled()) {
            return;
        }

        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            logger.debug("Could not start step {}: no test case running", result);
            return;
        }
        startStep(current.get(), uuid, result);
    }

    public void startStep(
            final String parentUuid,
            final String uuid,
            final StepResult result
    ) {
        if (!isTmsEnabled()) {
            return;
        }

        result
                .setItemStage(ItemStage.RUNNING)
                .setStart(System.currentTimeMillis());

        threadContext.start(uuid);

        storage.put(uuid, result);
        storage.get(parentUuid, ResultWithSteps.class).ifPresent(parentStep ->
                storage.updateIfPresent(
                        parentUuid,
                        ResultWithSteps.class,
                        p -> p.getSteps().add(result)
                )
        );

        if (logger.isDebugEnabled()) {
            logger.debug("Start step {} for parent {}", result, parentUuid);
        }
    }

    public void updateStep(final Consumer<StepResult> update) {
        if (!isTmsEnabled()) {
            return;
        }

        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            logger.debug("Could not update step: no step running");
            return;
        }
        updateStep(current.get(), update);
    }

    public void updateStep(
            final String uuid,
            final Consumer<StepResult> update
    ) {
        if (!isTmsEnabled()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Update step {}", uuid);
        }

        final Optional<StepResult> found = storage.getStep(uuid);
        if (!found.isPresent()) {
            logger.error(
                    "Could not update step: step with uuid {} not found",
                    uuid
            );
            return;
        }

        update.accept(found.get());
    }

    public void stopStep() {
        if (!isTmsEnabled()) {
            return;
        }

        final String root = threadContext.getRoot().orElse(null);
        final Optional<String> current = threadContext
                .getCurrent()
                .filter(uuid -> !Objects.equals(uuid, root));
        if (!current.isPresent()) {
            logger.debug("Could not stop step: no step running");
            return;
        }
        stopStep(current.get());
    }

    public void stopStep(final String uuid) {
        if (!isTmsEnabled()) {
            return;
        }

        final Optional<StepResult> found = storage.getStep(uuid);
        if (!found.isPresent()) {
            logger.error(
                    "Could not stop step: step with uuid {} not found",
                    uuid
            );
            return;
        }

        final StepResult step = found.get();
        step.setItemStage(ItemStage.FINISHED);
        step.setStop(System.currentTimeMillis());

        storage.remove(uuid);
        threadContext.stop();

        if (logger.isDebugEnabled()) {
            logger.debug("Stop step {}", step);
        }
    }

    private boolean isTmsEnabled() {
        return adapterConfig.shouldEnableTmsIntegration();
    }
}
