export SYNC_STORAGE_VERSION=$(grep -o 'SYNC_STORAGE_VERSION = "[^"]*"' testit-java-commons/src/main/java/ru/testit/syncstorage/SyncStorageRunner.java | cut -d'"' -f2)
