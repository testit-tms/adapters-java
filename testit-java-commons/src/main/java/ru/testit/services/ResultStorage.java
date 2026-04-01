package ru.testit.services;

import ru.testit.models.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Storage for test results
 */
public class ResultStorage {
    private final Map<String, Object> storage = new ConcurrentHashMap<>();

    public Optional<TestResult> getTestResult(final String uuid) {
        return get(uuid, TestResult.class);
    }

    /**
     * All keys that currently hold a {@link TestResult} (for diagnostics, e.g. bulk vs tree mismatch).
     */
    public Set<String> getAllTestResultUuids() {
        Set<String> uuids = new HashSet<>();
        for (Map.Entry<String, Object> e : storage.entrySet()) {
            if (e.getValue() instanceof TestResult) {
                uuids.add(e.getKey());
            }
        }
        return uuids;
    }

    public Optional<FixtureResult> getFixture(final String uuid) {
        return get(uuid, FixtureResult.class);
    }

    public Optional<StepResult> getStep(final String uuid) {
        return get(uuid, StepResult.class);
    }

    public Optional<MainContainer> getTestsContainer(final String uuid) {
        return get(uuid, MainContainer.class);
    }

    public Optional<ClassContainer> getClassContainer(final String uuid) {
        return get(uuid, ClassContainer.class);
    }

    public <T> Optional<T> get(final String uuid, final Class<T> clazz) {
        Objects.requireNonNull(uuid, "Can't get result from storage: uuid can't be null");
        return Optional.ofNullable(storage.get(uuid))
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }

    public <T> T put(final String uuid, final T item) {
        Objects.requireNonNull(uuid, "Can't put result to storage: uuid can't be null");
        Objects.requireNonNull(item, "Can't put result to storage: item can't be null");
        storage.put(uuid, item);
        return item;
    }

    public void remove(final String uuid) {
        Objects.requireNonNull(uuid, "Can't remove item from storage: uuid can't be null");
        storage.remove(uuid);
    }

    /**
     * Updates stored item in a thread-safe way.
     * <p>
     * Uses {@link ConcurrentHashMap#computeIfPresent(Object, java.util.function.BiFunction)} to avoid
     * "get-then-update" races and synchronizes on the stored instance to protect its internal state
     * (e.g. nested lists/maps) from concurrent modifications.
     */
    public <T> void updateIfPresent(final String uuid, final Class<T> clazz, final Consumer<T> update) {
        Objects.requireNonNull(uuid, "Can't update item in storage: uuid can't be null");
        Objects.requireNonNull(clazz, "Can't update item in storage: clazz can't be null");
        Objects.requireNonNull(update, "Can't update item in storage: update can't be null");

        storage.computeIfPresent(uuid, (key, value) -> {
            if (!clazz.isInstance(value)) {
                return value;
            }

            // Lock per stored instance to reduce contention compared to global "synchronized(storage)".
            synchronized (value) {
                update.accept(clazz.cast(value));
            }
            return value;
        });
    }
}
