import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
}

var apiClientVersion = "2.0.2-TMS-5.3"
val slf4jVersion = "1.7.36"
val jacksonVersion = "2.17.1"

dependencies {
    implementation("org.aspectj:aspectjrt:1.9.22")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("ru.testit:testit-api-client:$apiClientVersion")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("org.mockito:mockito-inline:4.4.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.PASSED)
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
    }

    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    systemProperties(systemProperties)
    environment(environment)
}
