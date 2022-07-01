package ru.testit.services;

import java.util.Objects;

public final class TmsFactory {
    private static TmsProxyService tmsProxyService;
    public static TmsProxyService getLifecycle() {
        if (Objects.isNull(tmsProxyService)) {
            tmsProxyService = new TmsProxyService();
        }
        return tmsProxyService;
    }
}
