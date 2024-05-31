import org.gradle.api.tasks.testing.logging.TestLogEvent

description = "TestIT JUnit 4 Integration"

plugins {
    java
}

val junitVersion = "4.13.2"
val aspectjVersion = "1.9.22"
val slf4jVersion = "1.7.36"
val agent: Configuration by configurations.creating

dependencies {
    agent("org.aspectj:aspectjweaver:$aspectjVersion")

    implementation("junit:junit:$junitVersion")
    implementation(project(":testit-java-commons"))
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("javax.xml.bind:jaxb-api:2.3.1")

    testImplementation("org.aspectj:aspectjrt:$aspectjVersion")
    testImplementation ("org.junit.platform:junit-platform-runner:1.6.3") {
        exclude("junit", "junit")
    }
}

tasks.jar {
    manifest {
        attributes(mapOf(
                "Automatic-Module-Name" to "ru.testit.junit4"
        ))
    }
}

tasks.test {
    useJUnit()
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
    systemProperties(systemProperties)
}
