package ru.testit.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public final class ServiceLoaderListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLoaderListener.class);

    private ServiceLoaderListener() {
        throw new IllegalStateException("Do not have instance");
    }

    public static <T> List<T> load(final Class<T> type, final ClassLoader classLoader) {
        final List<T> loaded = new ArrayList<>();
        final Iterator<T> iterator = ServiceLoader.load(type, classLoader).iterator();
        while (nextSafely(iterator)) {
            try {
                final T next = iterator.next();
                loaded.add(next);
                LOGGER.debug("Found type {}", type);
            } catch (Exception e) {
                LOGGER.error("Could not load listener {}: {}", type, e);
            }
        }
        return loaded;
    }

    private static <T> boolean nextSafely(final Iterator<T> iterator) {
        try {
            return iterator.hasNext();
        } catch (Exception e) {
            LOGGER.error("nextSafely failed", e);
            return false;
        }
    }
}
