import org.gradle.api.tasks.testing.logging.TestLogEvent

description = "TestIT TestNG Integration"

plugins {
    java
}

val testNgVersion = "7.5.1"
val aspectjVersion = "1.9.7"
val slf4jVersion = "1.7.36"
val agent: Configuration by configurations.creating

dependencies {
    agent("org.aspectj:aspectjweaver:$aspectjVersion")

    implementation("org.testng:testng:$testNgVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation(project(":testit-java-commons"))

    testImplementation("org.aspectj:aspectjrt:$aspectjVersion")
}

tasks.test {
    useTestNG(closureOf<TestNGOptions> {
        suites("src/test/resources/testng.xml")
    })
    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.PASSED)
        showCauses = false
        showStackTraces = false
        showStandardStreams = true
    }
    doFirst {
        jvmArgs("-javaagent:${agent.singleFile}")
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

tasks.compileTestJava {
    options.compilerArgs.add("-parameters")
}

tasks.jar {
    manifest {
        attributes(mapOf(
            "Automatic-Module-Name" to "ru.testit.testng"
        ))
    }
    from("src/main/services") {
        into("META-INF/services")
    }
}