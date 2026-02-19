package ru.testit.syncstorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.services.AdapterManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class SyncStorageRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            AdapterManager.class
    );

    private Process syncStorageProcess;

    private final String testRunId;

    private final String port;

    private final String baseURL;

    private final String privateToken;

    private String workerPid;

    private boolean isMaster = false;
    private boolean isAlreadyInProgress = false;
    // running in process
    private boolean isRunning = false;
    // running outside of this process
    private boolean isExternal = false;


    private static final String SYNC_STORAGE_VERSION = "v0.1.9";

    private static final String SYNC_STORAGE_REPO_URL ="https://github.com/testit-tms/sync-storage-public/releases/download/";
    private static final String AMD64 = "amd64";
    private static final String ARM64 = "arm64";

    public SyncStorageRunner(
            String testRunId,
            String port,
            String baseURL,
            String privateToken
    ) {
        this.testRunId = testRunId;
        this.port = port;
        this.baseURL = baseURL;
        this.privateToken = privateToken;
    }

    /**
     * Prepare executable file: checks build/.caches, if not - download from GitHub Releases
     *
     * @param originalExecutablePath file name
     * @return path to correct file
     * @throws IOException FS / Network errors
     */
    private String prepareExecutableFile(String originalExecutablePath)
            throws IOException {
        String currentDir = System.getProperty("user.dir");
        Path cachesDir = Paths.get(currentDir, "build/.caches");

        if (!Files.exists(cachesDir)) {
            Files.createDirectories(cachesDir);
        }

        Path originalPath = Paths.get(originalExecutablePath);
        String fileName = originalPath.getFileName().toString();
        Path targetPath = cachesDir.resolve(fileName);

        if (Files.exists(targetPath)) {
            LOGGER.info(
                    "Using existing file: " +
                            targetPath.toString()
            );

            // Make file executable for Unix-based systems
            if (
                    !System.getProperty("os.name").toLowerCase().contains("windows")
            ) {
                targetPath.toFile().setExecutable(true);
            }

            return targetPath.toString();
        }

        LOGGER.info(
                "File not present, downloading from GitHub Releases"
        );
        downloadExecutableFromGitHub(targetPath);

        return targetPath.toString();
    }

    /**
     * Download file from GitHub Releases
     *
     * @param targetPath path for saving file
     * @throws IOException Network or OS errors
     */
    private void downloadExecutableFromGitHub(Path targetPath)
            throws IOException {
        // Determine OS and arch
        String downloadUrl = getDownloadUrlForCurrentPlatform();

        LOGGER.info("Downloading file from: " + downloadUrl);
        LOGGER.info("Saving in: " + targetPath.toString());

        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(30000); // 30 sec
        connection.setReadTimeout(30000); // 30 sec

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException(
                        "Error while downloading file, error code: " + responseCode
                );
            }

            // Скачиваем файл
            try (
                    InputStream inputStream = connection.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(
                            targetPath.toFile()
                    )
            ) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            LOGGER.info("File downloaded successfully: " + targetPath.toString());

            // Делаем файл исполняемым (для Unix-систем)
            if (
                    !System.getProperty("os.name").toLowerCase().contains("windows")
            ) {
                targetPath.toFile().setExecutable(true);
            }
        } finally {
            connection.disconnect();
        }
    }

    private String getFileNameByArchAndOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        // Определяем ОС
        String osPart;
        if (osName.contains("win")) {
            osPart = "windows";
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            osPart = "darwin";
        } else if (osName.contains("linux")) {
            osPart = "linux";
        } else {
            throw new RuntimeException(
                    "Unsupported OS, please contact dev team: " + osName
            );
        }

        return makeFileName(osArch, osPart);
    }

    private boolean isMacOs(String osName) {
        return osName.contains("mac") || osName.contains("darwin");
    }

    private boolean isLinux(String osName) {
        return osName.contains("linux");
    }

    /**
     * Возвращает URL для скачивания файла в зависимости от текущей платформы
     *
     * @return URL для скачивания
     */
    private String getDownloadUrlForCurrentPlatform() {
        String fileName = getFileNameByArchAndOS();

        // Returns URL for GitHub Releases
        return (
                SYNC_STORAGE_REPO_URL + SYNC_STORAGE_VERSION + "/" +
                        fileName
        );
    }

    private static String makeFileName(String osArch, String osPart) {
        String archPart;
        if (osArch.contains(AMD64) || osArch.contains("x86_64")) {
            archPart = AMD64;
        } else if (osArch.contains(ARM64) || osArch.contains("aarch64")) {
            archPart = ARM64;
        } else {
            throw new RuntimeException(
                    "Unsupported architecture: " + osArch
            );
        }

        // Формируем имя файла
        String fileName = "syncstorage-" + SYNC_STORAGE_VERSION + "-" + osPart + "_" + archPart;
        if (osPart.equals("windows")) {
            fileName += ".exe";
        }
        return fileName;
    }

    /**
     * Start SyncStorage process or connect to existing one
     */
    public void start() throws IOException, InterruptedException {
        if (isRunning) {
            System.out.println("SyncStorage уже запущен");
            return;
        }

        // check if SyncStorage running on selected port
        if (isSyncStorageAlreadyRunning()) {
            System.out.println(
                    "SyncStorage already started " +
                            port +
                            ". Connecting to existing one..."
            );
            isRunning = true;
            isExternal = true;

            try {
                registerWorkerWithRetry();
            }
            catch (Exception e) {
                LOGGER.error(e.getMessage());
            }

            return;
        }

        String executablePath = getFileNameByArchAndOS();

        List<String> command = new ArrayList<>();
        command.add(executablePath);

        if (testRunId != null && !testRunId.isEmpty()) {
            command.add("--testRunId");
            command.add(testRunId);
        }

        if (port != null && !port.isEmpty()) {
            command.add("--port");
            command.add(port);
        }

        // Add baseURL if defined
        if (baseURL != null && !baseURL.isEmpty()) {
            command.add("--baseURL");

            command.add(baseURL);
        }

        // Add privateToken if defined
        if (privateToken != null && !privateToken.isEmpty()) {
            command.add("--privateToken");
            command.add(privateToken);
        }



        // prepare executable file
        String preparedExecutablePath = prepareExecutableFile(executablePath);

        // Update command with selected file
        command.set(0, preparedExecutablePath);

        String osName = System.getProperty("os.name").toLowerCase();
//        if (isMacOs(osName)) {
//            String command1 = String.join(" ", command);
//            command1 += "& disown -h %1";
//
//            command = new ArrayList<>();
//            command.add("zsh");
//            command.add("-c");
//            command.add(command1);
//        }
//        if (isLinux(osName)) {
//            String command1 = String.join(" ", command);
//            command1 = "nohup " + command1 + " > service.log 2>&1";
//            command1 += " & disown -h %1";
//
//            command = new ArrayList<>();
//            command.add("bash");
//            command.add("-c");
//            command.add(command1);
//        }

        System.out.println(
                "Starting SyncStorage with command: " + String.join(" ", command)
        );

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(
                new File(new File(preparedExecutablePath).getParent())
        );

        processBuilder.redirectErrorStream(true);

        syncStorageProcess = processBuilder.start();

        // Read output as different thread
        startOutputReader();

        if (waitForServerStartup(30)) {
            isRunning = true;
            System.out.println("SyncStorage started successfully on port " + port);
            Thread.sleep(2000);
            try {
                registerWorkerWithRetry();
            }
            catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        } else {
            throw new RuntimeException(
                    "Cannot start the SyncStorage until timeout"
            );
        }
    }

    private void registerWorkerWithRetry() {
        // Register current process as worker
        // try 5 times in a row
        for (int i = 0; i < 5; i++) {
            boolean isRegistered = registerWorker();
            if (isRegistered) break;
        }
    }

    /**
     * Run output reading thread
     */
    private void startOutputReader() {
        Thread readerThread = new Thread(() -> {
            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(syncStorageProcess.getInputStream())
                    )
            ) {
                String line;
                while (
                        (line = reader.readLine()) != null &&
                                !Thread.currentThread().isInterrupted()
                ) {
                    System.out.println("[SyncStorage] " + line);
                }
            } catch (IOException e) {
                if (!Thread.currentThread().isInterrupted()) {
                    System.err.println(
                            "Ошибка чтения вывода SyncStorage: " + e.getMessage()
                    );
                }
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private boolean waitForServerStartup(int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (isSyncStorageAlreadyRunning()) {
                return true;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * Check if SyncStorage running on port
     */
    private boolean isSyncStorageAlreadyRunning() {
        try {
            URL url = new URL("http://localhost:" + port + "/health");
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);

            int responseCode = connection.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Register current process as worker SyncStorage
     * API CALL
     */
    private boolean registerWorker() {
        try {
            workerPid =
                    "worker-" +
                            Thread.currentThread().getId() +
                            "-" +
                            System.currentTimeMillis();

            URL url = new URL(this.getUrl() + "/register");
            LOGGER.info("register on " + url.toString());
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString =
                    "{\"pid\": \"" +
                            workerPid +
                            "\", \"testRunId\": \"" +
                            testRunId +
                            "\"}";

            java.io.OutputStream os = connection.getOutputStream();
            try {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            } finally {
                os.close();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON answer to check is_master
                String responseStr = response.toString();
                if (
                        responseStr.contains("\"is_master\":true") ||
                                responseStr.contains("\"is_master\": true")
                ) {
                    isMaster = true;
                    LOGGER.info(
                            "Master worker registered, PID: " +
                                    workerPid
                    );
                } else {
                    LOGGER.info(
                            "Worker registered successfully, PID: " + workerPid
                    );
                }
                return true;
            } else {
                LOGGER.warn(
                        "Error worker register. Response code: " + responseCode
                );
            }
        } catch (Exception e) {
            LOGGER.error(
                    "Error on worker registering: " + e.getMessage()
            );
        }
        return false;
    }

    /**
     * Get worker PID
     */
    public String getWorkerPid() {
        return workerPid;
    }

    public String getTestRunId() {
        return testRunId;
    }

    /**
     * Check is current process master
     */
    public boolean isMaster() {
        return isMaster;
    }

    public boolean isAlreadyInProgress() {
        return isAlreadyInProgress;
    }

    public void setIsAlreadyInProgress(boolean value) {
        this.isAlreadyInProgress = value;
    }

    /**
     * Check is SyncStorage running
     */
    public boolean isRunning() {
//        LOGGER.info("isRunning" + isRunning);
//        LOGGER.info("not null" + (syncStorageProcess != null));

        return (
                isRunning
        );
    }

    public boolean isRunningAsProcess() {
        LOGGER.info("isRunning" + isRunning);
        LOGGER.info("not null" + (syncStorageProcess != null));

        return (
                isRunning &&
                        syncStorageProcess != null &&
                        syncStorageProcess.isAlive()
        );
    }

    /**
     * Get SyncStorage URL
     */
    public String getUrl() {
        return "http://localhost:" + port;
    }
}
