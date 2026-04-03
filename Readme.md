# TestIT Java Integrations
The repository contains new versions of adaptors for JVM-based test frameworks.

## Compatibility

| Test IT | Cucumber      | JBehave       | JUnit         | TestNG        |
|---------|---------------|---------------|---------------|---------------|
| 3.5     | 1.1           | 1.1           | 1.1           | 1.1           |
| 4.0     | 1.2           | 1.2           | 1.2           | 1.2           |
| 4.5     | 1.5           | 1.5           | 1.5           | 1.5           |
| 4.6     | 1.6           | 1.6           | 1.6           | 1.6           |
| 5.0     | 2.3           | 2.3           | 2.3           | 2.3           |
| 5.2     | 2.4           | 2.4           | 2.4           | 2.4           |
| 5.2.2   | 2.5           | 2.5           | 2.5           | 2.5           |
| 5.3     | 2.6.2-TMS-5.3 | 2.6.2-TMS-5.3 | 2.6.2-TMS-5.3 | 2.6.2-TMS-5.3 |
| 5.4     | 2.7.3-TMS-5.4 | 2.7.3-TMS-5.4 | 2.7.3-TMS-5.4 | 2.7.3-TMS-5.4 |
| 5.5     | 2.8.1-TMS-5.5 | 2.8.1-TMS-5.5 | 2.8.1-TMS-5.5 | 2.8.1-TMS-5.5 |
| 5.6     | 2.9.1-TMS-5.6 | 2.9.1-TMS-5.6 | 2.9.1-TMS-5.6 | 2.9.1-TMS-5.6 |
| Cloud   | 2.10.0 +      | 2.10.0 +      | 2.10.0 +      | 2.10.0 +      |

1. For current versions, see the releases tab. 
2. Starting with 5.2, we have added a TMS postscript, which means that the utility is compatible with a specific enterprise version. 
3. If you are in doubt about which version to use, check with the support staff. support@yoonion.ru


Supported test frameworks :
 1. [TestNG](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-testng)
 2. [Junit4](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-junit4)
 3. [Junit5](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-junit5)
 4. [Cucumber4](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-cucumber4)
 5. [Cucumber5](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-cucumber5)
 6. [Cucumber6](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-cucumber6)
 7. [Cucumber7](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-cucumber7)
 8. [JBehave](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-jbehave)
 9. [Selenide](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-selenide)



## What's new in 3.0.0?

- New logic with a fix for test results loading
- Added sync-storage subprocess usage for worker synchronization on port **49152** by defailt.
- importRealtime=false is a default mode (previously true)

### How to run 3.0+ locally?

You can change nothing, it's full compatible with previous versions of adapters for local run on all OS.


### How to run 3.0+ with CI/CD?

For CI/CD pipelines, we recommend starting the sync-storage instance before the adapter and waiting for its completion within the same job.

You can see how we implement this [here.](https://github.com/testit-tms/adapters-java/tree/main/.github/workflows/test.yml#176) 

- to get the latest version of sync-storage, please use our [script](https://github.com/testit-tms/adapters-java/tree/main/scripts/curl_last_version.sh)

- To download a specific version of sync-storage, use our [script](https://github.com/testit-tms/adapters-java/tree/main/scripts/get_sync_storage.sh) and pass the desired version number as the first parameter. Sync-storage will be downloaded as `.caches/syncstorage-linux-amd64`

1. Create an empty test run using `testit-cli` or use an existing one, and save the `testRunId`.
2. Start **sync-storage** with the correct parameters as a background process (alternatives to nohup can be used). Stream the log output to the `service.log` file:
```bash
nohup .caches/syncstorage-linux-amd64 --testRunId ${{ env.TMS_TEST_RUN_ID }} --port 49152 \
    --baseURL ${{ env.TMS_URL }} --privateToken ${{ env.TMS_PRIVATE_TOKEN }}  > service.log 2>&1 & 
```
3. Start the adapter using adapterMode=1 or adapterMode=0 for the selected testRunId.
4. Wait for sync-storage to complete background jobs by calling:
```bash
curl -v http://127.0.0.1:49152/wait-completion?testRunId=${{ env.TMS_TEST_RUN_ID }} || true
```
5. You can read the sync-storage logs from the service.log file.


# 🚀 Warning
Since 2.2.0 version:
- If value from @WorkItemIds annotation not found in TMS then test result will NOT be uploaded.
