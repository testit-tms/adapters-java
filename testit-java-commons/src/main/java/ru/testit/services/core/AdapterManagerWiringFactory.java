package ru.testit.services;

import org.slf4j.Logger;
import ru.testit.clients.ClientConfiguration;
import ru.testit.clients.ITmsApiClient;
import ru.testit.listener.ListenerManager;
import ru.testit.properties.AdapterConfig;
import ru.testit.services.core.AdapterContainerHelper;
import ru.testit.services.core.AdapterCoreFacade;
import ru.testit.services.core.AdapterFixtureHelper;
import ru.testit.services.core.AdapterMetadataHelper;
import ru.testit.services.core.AdapterStartupHelper;
import ru.testit.services.core.AdapterStepHelper;
import ru.testit.services.core.AdapterSupportFacade;
import ru.testit.services.core.AdapterTestCaseHelper;
import ru.testit.syncstorage.ClientWrapper;
import ru.testit.syncstorage.SyncStorageService;
import ru.testit.writers.Writer;

final class AdapterManagerWiringFactory {

    private AdapterManagerWiringFactory() {
    }

    static HelperWiring create(
            AdapterConfig adapterConfig,
            ClientConfiguration clientConfiguration,
            ThreadContext threadContext,
            ResultStorage storage,
            Writer writer,
            ITmsApiClient client,
            ListenerManager listenerManager,
            Logger logger
    ) {
        SyncStorageService syncStorageService = new SyncStorageService(
                adapterConfig,
                clientConfiguration,
                client,
                new ClientWrapper()
        );
        AdapterMetadataHelper metadataHelper = new AdapterMetadataHelper(
                adapterConfig,
                threadContext,
                storage,
                writer,
                logger
        );
        AdapterStepHelper stepHelper = new AdapterStepHelper(
                adapterConfig,
                threadContext,
                storage,
                logger
        );
        AdapterFixtureHelper fixtureHelper = new AdapterFixtureHelper(
                adapterConfig,
                threadContext,
                storage,
                logger
        );
        AdapterTestCaseHelper testCaseHelper = new AdapterTestCaseHelper(
                adapterConfig,
                threadContext,
                storage,
                listenerManager,
                writer,
                syncStorageService,
                logger
        );
        AdapterContainerHelper containerHelper = new AdapterContainerHelper(
                adapterConfig,
                storage,
                writer,
                logger,
                syncStorageService
        );
        AdapterStartupHelper startupHelper = new AdapterStartupHelper(
                adapterConfig,
                clientConfiguration,
                client,
                logger
        );
        AdapterCoreFacade coreFacade = new AdapterCoreFacade(
                containerHelper,
                testCaseHelper,
                fixtureHelper,
                stepHelper
        );
        AdapterSupportFacade supportFacade = new AdapterSupportFacade(
                metadataHelper,
                startupHelper,
                syncStorageService
        );
        return new HelperWiring(
                coreFacade,
                supportFacade
        );
    }

    static final class HelperWiring {
        final AdapterCoreFacade coreFacade;
        final AdapterSupportFacade supportFacade;

        private HelperWiring(
                AdapterCoreFacade coreFacade,
                AdapterSupportFacade supportFacade
        ) {
            this.coreFacade = coreFacade;
            this.supportFacade = supportFacade;
        }
    }
}
