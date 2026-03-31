package ru.testit.services;

import ru.testit.clients.ClientConfiguration;
import ru.testit.clients.TmsApiClient;
import ru.testit.writers.HttpWriter;

final class AdapterManagerDefaultsFactory {

    private AdapterManagerDefaultsFactory() {
    }

    static ConstructorDefaults create(ClientConfiguration clientConfiguration) {
        ResultStorage storage = Adapter.getResultStorage();
        ThreadContext threadContext = new ThreadContext();
        TmsApiClient client = new TmsApiClient(clientConfiguration);
        HttpWriter writer = new HttpWriter(
                clientConfiguration,
                client,
                storage
        );
        return new ConstructorDefaults(threadContext, storage, client, writer);
    }

    static final class ConstructorDefaults {
        final ThreadContext threadContext;
        final ResultStorage storage;
        final TmsApiClient client;
        final HttpWriter writer;

        private ConstructorDefaults(
                ThreadContext threadContext,
                ResultStorage storage,
                TmsApiClient client,
                HttpWriter writer
        ) {
            this.threadContext = threadContext;
            this.storage = storage;
            this.client = client;
            this.writer = writer;
        }
    }
}
