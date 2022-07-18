description = "TestIT JUnit 4 Integration"

plugins {
    id("java")
}

val junitVersion = "4.13.2"
val aspectjVersion = "1.9.7"
val agent: Configuration by configurations.creating

dependencies {
    agent("org.aspectj:aspectjweaver:$aspectjVersion")

    implementation("junit:junit:$junitVersion")
    implementation(project(":testit-java-commons"))

    testImplementation("junit:junit:$junitVersion")
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

tasks.compileTestJava {
    options.encoding = "UTF-8"
}

tasks.getByName<Test>("test") {
    useJUnit()
    exclude("**/samples/*")
    doFirst {
        jvmArgs(
            "-javaagent:${agent.singleFile}"
        )
    }
}
