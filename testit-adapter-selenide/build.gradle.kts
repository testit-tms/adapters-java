import org.gradle.api.tasks.testing.logging.TestLogEvent

description = "TestIT Selenide Integration"

plugins {
    java
}

val selenideVersion = "6.19.1"

dependencies {
    implementation(project(":testit-java-commons"))
    compileOnly("com.codeborne:selenide:$selenideVersion")
    testImplementation("com.codeborne:selenide:$selenideVersion")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.jar {
    manifest {
        attributes(mapOf(
            "Automatic-Module-Name" to "ru.testit.selenide"
        ))
    }
    from("src/main/services") {
        into("META-INF/services")
    }
}

tasks.compileTestJava {
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.PASSED)
        showCauses = false
        showStackTraces = false
        showStandardStreams = true
    }

    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    environment("TMS_URL", System.getProperty("tmsUrl"))
    environment("TMS_PRIVATE_TOKEN", System.getProperty("tmsPrivateToken"))
    environment("TMS_PROJECT_ID", System.getProperty("tmsProjectId"))
    environment("TMS_CONFIGURATION_ID", System.getProperty("tmsConfigurationId"))
    environment("TMS_TEST_RUN_ID", System.getProperty("tmsTestRunId"))
    environment("TMS_TEST_RUN_NAME", System.getProperty("tmsTestRunName"))
    environment("TMS_ADAPTER_MODE", System.getProperty("tmsAdapterMode"))
    environment("TMS_CERT_VALIDATION", System.getProperty("tmsCertValidation"))
    environment("TMS_TEST_IT", System.getProperty("tmsTestIt"))
    environment("TMS_AUTOMATIC_CREATION_TEST_CASES", System.getProperty("tmsAutomaticCreationTestCases"))
}