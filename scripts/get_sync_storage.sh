SYNC_STORAGE_VERSION=$1

mkdir -p .caches

wget -O .caches/syncstorage-linux-amd64 \
"https://github.com/testit-tms/sync-storage-public/releases/download/${SYNC_STORAGE_VERSION}/syncstorage-${SYNC_STORAGE_VERSION}-linux_amd64"
chmod +x .caches/syncstorage-linux-amd64