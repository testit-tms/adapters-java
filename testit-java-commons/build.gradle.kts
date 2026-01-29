import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
}

var apiClientVersion = "2.3.0"
val slf4jVersion = "1.7.36"
val jacksonVersion = "2.17.1"
val aspectjrtVersion = "1.9.22"
val commonsLang3Version = "3.18.0"
val jakartaWsRsVersion = "3.0.0"
val junitJupiterVersion = "5.8.2"
val mockitoInlineVersion = "4.4.0"

dependencies {
    implementation("org.aspectj:aspectjrt:$aspectjrtVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("ru.testit:testit-api-client:$apiClientVersion")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:$jakartaWsRsVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testImplementation("org.mockito:mockito-inline:$mockitoInlineVersion")
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
