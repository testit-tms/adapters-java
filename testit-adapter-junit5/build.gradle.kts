import org.gradle.api.tasks.testing.logging.TestLogEvent

description = "TestIT JUnit 5 Integration"

plugins {
    java
}

val junitVersion = "5.8.2"
val junitLauncherVersion = "1.9.0"
val aspectjVersion = "1.9.22"
val slf4jVersion = "1.7.36"

val agent: Configuration by configurations.creating

dependencies {
    agent("org.aspectj:aspectjweaver:$aspectjVersion")

    implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    implementation("org.junit.platform:junit-platform-launcher:$junitLauncherVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation(project(":testit-java-commons"))

    testImplementation("org.aspectj:aspectjrt:$aspectjVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
}

tasks.jar {
    manifest {
        attributes(mapOf(
                "Automatic-Module-Name" to "ru.testit.junit5"
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
    exclude("**/samples/*")
    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.PASSED)
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
    }
    doFirst {
        jvmArgs("-javaagent:${agent.singleFile}")
    }

    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    systemProperties(systemProperties)
    environment(environment)
}
