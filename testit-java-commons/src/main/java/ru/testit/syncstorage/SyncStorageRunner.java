package ru.testit.syncstorage;

import jakarta.annotation.Nonnull;
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
            SyncStorageRunner.class
    );
    public static final int CONNECT_TIMEOUT = 30000;

    private Process syncStorageProcess;

    private final String testRunId;

    private final String port;

    private final String baseURL;

    private final String privateToken;

    private final String executablePath;

    private String workerPid;

    private boolean isMaster = false;
    private boolean isAlreadyInProgress = false;
    // running in process
    private boolean isRunning = false;
    // running outside of this process
    private boolean isExternal = false;


    private static final String SYNC_STORAGE_VERSION = "v0.3.2";

    private static final String SYNC_STORAGE_REPO_URL ="https://github.com/testit-tms/sync-storage-public/releases/download/";
    private static final String AMD64 = "amd64";
    private static final String ARM64 = "arm64";
    private final ClientWrapper clientWrapper = new ClientWrapper();

    public SyncStorageRunner(
            String testRunId,
            String port,
            String baseURL,
            String privateToken,
            String executablePath
    ) {
        this.testRunId = testRunId;
        this.port = port;
        this.baseURL = baseURL;
        this.privateToken = privateToken;
        this.executablePath = executablePath;
    }

    /**
     * Prepare executable file: checks build/.caches, if not - download from GitHub Releases
     *
     * @param originalExecutablePath file name
     * @return path to correct file
     * @throws IOException FS / Network errors
     */
    private String prepareBundledExecutableFile(String originalExecutablePath)
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
            LOGGER.debug(
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

        LOGGER.debug(
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

        LOGGER.debug("Downloading file from: " + downloadUrl);
        LOGGER.debug("Saving in: " + targetPath.toString());

        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(CONNECT_TIMEOUT);

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

            LOGGER.debug("File downloaded successfully: " + targetPath.toString());

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
            LOGGER.debug("SyncStorage already running");
            return;
        }

        // check if SyncStorage running on selected port
        if (isSyncStorageAlreadyRunning()) {
            LOGGER.debug(
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
                LOGGER.warn("Failed to register sync-storage worker: {}", e.getMessage());
            }

            return;
        }

        String binaryPath = executablePath;
        if (binaryPath == null || binaryPath.isEmpty()) {
            binaryPath = getFileNameByArchAndOS();
        }

        List<String> command = getCommand(binaryPath);


        // prepare executable file
        String preparedExecutablePath = prepareExecutablePath(binaryPath);

        // Update command with selected file
        command.set(0, preparedExecutablePath);

        // String osName = System.getProperty("os.name").toLowerCase();

        LOGGER.debug(
                "Starting SyncStorage with command: " + String.join(" ", command)
        );

        File workingDirectory = new File(new File(preparedExecutablePath).getParent());
        if (isLinux(System.getProperty("os.name").toLowerCase())) {
            startDetachedProcess(command, workingDirectory);
            isExternal = true;
            syncStorageProcess = null;
        } else {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(workingDirectory);
            processBuilder.redirectErrorStream(true);

            syncStorageProcess = processBuilder.start();

            // Read output as different thread
            startOutputReader();
        }

        if (waitForServerStartup(30)) {
            isRunning = true;
            LOGGER.debug("SyncStorage started successfully on port {}", port);
            Thread.sleep(2000);
            try {
                registerWorkerWithRetry();
            }
            catch (Exception e) {
                LOGGER.warn("Failed to register sync-storage worker: {}", e.getMessage());
            }
        } else {
            throw new RuntimeException(
                    "Cannot start the SyncStorage until timeout"
            );
        }
    }

    @Nonnull
    private List<String> getCommand(String executablePath) {
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
        return command;
    }

    private String prepareExecutableFileFromPath(String executablePath)
            throws IOException {
        Path path = Paths.get(executablePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException(
                    "SyncStorage executable not found: " + executablePath
            );
        }

        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            path.toFile().setExecutable(true);
        }

        return path.toAbsolutePath().toString();
    }

    private String prepareExecutablePath(String executablePath)
            throws IOException {
        Path path = Paths.get(executablePath);
        if (path.isAbsolute() || Files.exists(path)) {
            return prepareExecutableFileFromPath(executablePath);
        }
        return prepareBundledExecutableFile(path.toString());
    }

    private void registerWorkerWithRetry() {
        // Register current process as worker
        // try 5 times in a row
        for (int i = 0; i < 5; i++) {
            boolean isRegistered = registerWorker();
            if (isRegistered) break;
        }
    }

    private void startDetachedProcess(List<String> command, File workingDirectory)
            throws IOException {
        File logFile = new File(workingDirectory, "syncstorage.log");
        StringBuilder shellCommand = new StringBuilder("setsid ");

        for (String arg : command) {
            shellCommand.append(escapeShellArgument(arg)).append(" ");
        }

        shellCommand
                .append("</dev/null >>")
                .append(escapeShellArgument(logFile.getAbsolutePath()))
                .append(" 2>&1 &");

        ProcessBuilder processBuilder = new ProcessBuilder(
                "sh",
                "-c",
                shellCommand.toString()
        );
        processBuilder.directory(workingDirectory);
        processBuilder.start();
    }

    private String escapeShellArgument(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
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
                    LOGGER.debug("[SyncStorage] {}", line);
                }
            } catch (IOException e) {
                if (!Thread.currentThread().isInterrupted()) {
                    LOGGER.debug(
                            "Failed to read SyncStorage output: {}",
                            e.getMessage()
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
        ClientWrapper.RegistrationResult registrationResult =
                clientWrapper.registerWorker(this.getUrl(), testRunId);

        if (registrationResult == null) {
            return false;
        }

        workerPid = registrationResult.getWorkerPid();
        isMaster = registrationResult.isMaster();

        if (isMaster) {
            LOGGER.debug("Master worker registered, PID: {}", workerPid);
        } else {
            LOGGER.debug("Worker registered successfully, PID: {}", workerPid);
        }

        return true;
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
    public boolean isNotRunning() {
        return (!isRunning);
    }

    public boolean isRunningAsProcess() {
        LOGGER.debug("isRunning{}", isRunning);
        LOGGER.debug("processNotNull{}", syncStorageProcess != null);

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
