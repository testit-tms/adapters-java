package ru.testit.services;

import ru.testit.models.*;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Storage for test results
 */
public class ResultStorage {
    private final Map<String, Object> storage = new ConcurrentHashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public Optional<TestResult> getTestResult(final String uuid) {
        return get(uuid, TestResult.class);
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
        lock.readLock().lock();
        try {
            Objects.requireNonNull(uuid, "Can't get result from storage: uuid can't be null");
            return Optional.ofNullable(storage.get(uuid))
                    .filter(clazz::isInstance)
                    .map(clazz::cast);
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T> T put(final String uuid, final T item) {
        lock.writeLock().lock();
        try {
            Objects.requireNonNull(uuid, "Can't put result to storage: uuid can't be null");
            Objects.requireNonNull(item, "Can't put result to storage: item can't be null");
            storage.put(uuid, item);
            return item;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void remove(final String uuid) {
        lock.writeLock().lock();
        try {
            Objects.requireNonNull(uuid, "Can't remove item from storage: uuid can't be null");
            storage.remove(uuid);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
