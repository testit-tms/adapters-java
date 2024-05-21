import org.gradle.api.tasks.testing.logging.TestLogEvent

description = "TestIT Cucumber4 Integration"

plugins {
    java
}

val cucumberVersion = "4.8.1"
val slf4jVersion = "1.7.36"
val testngVersion = "7.5.1"

dependencies {
    implementation(project(":testit-java-commons"))
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("io.cucumber:cucumber-core:$cucumberVersion")
    implementation("io.cucumber:cucumber-java:$cucumberVersion")
    implementation("org.json:json:20231013")

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
    systemProperties(systemProperties)
}

tasks.jar {
    manifest {
        attributes(mapOf(
            "Automatic-Module-Name" to "ru.testit.cucumber4"
        ))
    }
}