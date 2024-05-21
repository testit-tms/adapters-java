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
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
    }

    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    systemProperties(systemProperties)
}