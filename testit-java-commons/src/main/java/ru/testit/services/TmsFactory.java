package ru.testit.services;

import ru.testit.models.LinkItem;
import ru.testit.models.LinkType;

import java.util.Objects;

public final class TmsFactory {
    private static TmsManager tmsManager;
    private static ResultStorage storage;
    public static TmsManager getTmsManager() {
        if (Objects.isNull(tmsManager)) {
            tmsManager = new TmsManager();
        }
        return tmsManager;
    }
    public static ResultStorage getResultStorage() {
        if (Objects.isNull(storage)) {
            storage = new ResultStorage();
        }
        return storage;
    }

    public static void link(final String title, final String description, final LinkType type, final String url) {
        final LinkItem link = new LinkItem().setTitle(title).setDescription(description).setType(type).setUrl(url);
        getTmsManager().updateTestCase(testResult -> testResult.getResultLinks().add(link));
    }
}
