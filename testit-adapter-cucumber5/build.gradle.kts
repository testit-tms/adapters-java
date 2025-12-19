import org.gradle.api.tasks.testing.logging.TestLogEvent

description = "TestIT Cucumber5 Integration"

plugins {
    java
}

val cucumberVersion = "5.1.2"
val cucumberGherkinVersion = "5.1.0"
val slf4jVersion = "1.7.36"
val testngVersion = "7.5.1"
val jsonVersion = "20231013"

dependencies {
    implementation(project(":testit-java-commons"))
    implementation("io.cucumber:gherkin:$cucumberGherkinVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("org.json:json:$jsonVersion")

    compileOnly("io.cucumber:cucumber-plugin:$cucumberVersion")

    testImplementation("io.cucumber:cucumber-core:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-testng:$cucumberVersion")
    testImplementation("org.testng:testng:$testngVersion")
}

tasks.test {
    useTestNG()
    exclude("**/samples/*")
    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.PASSED)
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
    }
    systemProperties(systemProperties)
    environment(environment)
}

tasks.jar {
    manifest {
        attributes(mapOf(
            "Automatic-Module-Name" to "ru.testit.cucumber5"
        ))
    }
}