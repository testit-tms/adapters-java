package ru.testit.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.models.TestResult;

import java.util.List;
import java.util.function.BiConsumer;

public class ListenerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerManager.class);
    private List<AdapterListener> listeners;

    public ListenerManager(final List<AdapterListener> listeners) {

        this.listeners = listeners;
    }

    public void beforeTestStop(final TestResult result) {
        runSafelyMethod(listeners, AdapterListener::beforeTestStop, result);
    }

    protected <T extends DefaultListener, S> void runSafelyMethod(final List<T> listeners,
                                                                  final BiConsumer<T, S> method,
                                                                  final S object) {
        listeners.forEach(listener -> {
            try {
                method.accept(listener, object);
            } catch (Exception e) {
                LOGGER.error("Could not invoke listener method", e);
            }
        });
    }
}
