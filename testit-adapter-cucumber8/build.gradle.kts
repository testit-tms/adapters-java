import org.gradle.api.tasks.testing.logging.TestLogEvent

description = "TestIT Cucumber8 Integration"

plugins {
    java
}

if (!JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    logger.warn("Skipping testit-adapter-cucumber8: requires Java 17+, current is ${JavaVersion.current()}")
    tasks.configureEach { enabled = false }
}

val cucumberVersion = "8.0.4"
val cucumberGherkinVersion = "42.0.1"
val slf4jVersion = "1.7.36"
/** Max TestNG without ITestNGListener.isEnabled() (added in 7.10); required for Gradle 7.4. */
val testngVersion = "7.9.0"
val jsonVersion = "20231013"
val jacksonDatatypeVersion = "2.17.1"

dependencies {
    implementation(project(":testit-java-commons"))
    implementation("io.cucumber:gherkin:$cucumberGherkinVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("org.json:json:$jsonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonDatatypeVersion")

    compileOnly("io.cucumber:cucumber-plugin:$cucumberVersion")

    testImplementation("io.cucumber:cucumber-core:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-testng:$cucumberVersion")
    testImplementation("org.testng:testng:$testngVersion")
}

configurations.all {
    resolutionStrategy.force("org.testng:testng:$testngVersion")
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "17"
    targetCompatibility = "17"
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
            "Automatic-Module-Name" to "ru.testit.cucumber8"
        ))
    }
}
