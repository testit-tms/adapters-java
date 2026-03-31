package ru.testit.services.core;

import ru.testit.syncstorage.SyncStorageService;

import java.util.List;
import java.util.Map;

public class AdapterSupportFacade {

    private final AdapterMetadataHelper metadataHelper;
    private final AdapterStartupHelper startupHelper;
    private final SyncStorageService syncStorageService;

    public AdapterSupportFacade(
            AdapterMetadataHelper metadataHelper,
            AdapterStartupHelper startupHelper,
            SyncStorageService syncStorageService
    ) {
        this.metadataHelper = metadataHelper;
        this.startupHelper = startupHelper;
        this.syncStorageService = syncStorageService;
    }

    public void addAttachments(List<String> attachments) {
        metadataHelper.addAttachments(attachments);
    }

    public void addParameters(Map<String, String> parameters) {
        metadataHelper.addParameters(parameters);
    }

    public void addTitle(String title) {
        metadataHelper.addTitle(title);
    }

    public void addDescription(String description) {
        metadataHelper.addDescription(description);
    }

    public void startTests() {
        startupHelper.startTests();
    }

    public List<String> getTestFromTestRun() {
        return startupHelper.getTestFromTestRun();
    }

    public void setWorkerStatus(String status) {
        syncStorageService.setWorkerStatus(status);
    }

    public void setWorkerStatus(String pid, String status) {
        syncStorageService.setWorkerStatus(pid, status);
    }
}
