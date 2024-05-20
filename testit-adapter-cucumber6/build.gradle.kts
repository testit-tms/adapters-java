import org.gradle.api.tasks.testing.logging.TestLogEvent

description = "TestIT Cucumber6 Integration"

plugins {
    java
}

val cucumberVersion = "6.10.2"
val cucumberGherkinVersion = "18.0.0"
val slf4jVersion = "1.7.36"
val testngVersion = "7.5.1"

dependencies {
    implementation(project(":testit-java-commons"))
    implementation("io.cucumber:gherkin:$cucumberGherkinVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("org.json:json:20231013")

    compileOnly("io.cucumber:cucumber-plugin:$cucumberVersion")

    testImplementation("io.cucumber:cucumber-core:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-testng:$cucumberVersion")
    testImplementation("org.testng:testng:$testngVersion")
}

tasks.test {
    useTestNG()
    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.PASSED)
        showCauses = false
        showStackTraces = false
        showStandardStreams = true
    }

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

tasks.jar {
    manifest {
        attributes(mapOf(
            "Automatic-Module-Name" to "ru.testit.cucumber6"
        ))
    }
}