package ru.testit.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NormalizeWebDriverMessageTest {

    @Test
    void normalizeWebDriverMessage_unifiesDifferentChromeStartupErrors() {
        String chromeExited = "Could not start a new session. Response code 500. Message: session not created: "
                + "Chrome instance exited. Examine ChromeDriver verbose log to determine the cause. \n"
                + "Host info: host: 'runnervmrw5os', ip: '10.1.0.196'\n"
                + "System info: os.name: 'Linux', os.version: '6.17.0-1013-azure', java.version: '1.8.0_492'\n"
                + "selenide.url: https://testit.software/";

        String userDataDirInUse = "Could not start a new session. Response code 500. Message: session not created: "
                + "probably user data directory is already in use, please specify a unique value for --user-data-dir "
                + "argument, or don't use --user-data-dir \n"
                + "Host info: host: 'pkrvm7jw40e0xgp', ip: '10.1.0.4'\n"
                + "System info: os.name: 'Linux', os.version: '6.11.0-1018-azure', java.version: '17.0.16'\n"
                + "selenide.url: https://testit.software/";

        String normalizedChromeExited = Utils.normalizeWebDriverMessage(chromeExited);
        String normalizedUserDataDir = Utils.normalizeWebDriverMessage(userDataDirInUse);

        Assertions.assertEquals(normalizedChromeExited, normalizedUserDataDir);
        Assertions.assertFalse(normalizedChromeExited.contains("Host info:"));
        Assertions.assertTrue(normalizedChromeExited.contains("session not created: <unspecified>"));
        Assertions.assertTrue(normalizedChromeExited.contains("java.version: '*'"));
    }

    @Test
    void normalizeWebDriverMessage_leavesAssertionErrorsUntouched() {
        String assertionMessage = "Element should have text \"Система управления тестированием\"";
        Assertions.assertEquals(assertionMessage, Utils.normalizeWebDriverMessage(assertionMessage));
    }
}
