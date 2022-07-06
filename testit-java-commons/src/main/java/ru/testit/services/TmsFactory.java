package ru.testit.services;

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
}
