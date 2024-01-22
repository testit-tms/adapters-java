description = "TestIT JBehave Integration"

plugins {
    id("java")
}

val junitVersion = "5.8.2"
val jbehaveVersion = "4.8.3"
val aspectjVersion = "1.9.7"
val slf4jVersion = "1.7.36"
val agent: Configuration by configurations.creating

dependencies {
    agent("org.aspectj:aspectjweaver:$aspectjVersion")

    implementation("org.jbehave:jbehave-core:$jbehaveVersion")
    implementation(project(":testit-java-commons"))
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("org.json:json:20220924")

    testImplementation("org.jbehave:jbehave-core:$jbehaveVersion")
    testImplementation("org.aspectj:aspectjrt:$aspectjVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.jar {
    manifest {
        attributes(mapOf(
                "Automatic-Module-Name" to "ru.testit.jbehave"
        ))
    }
}

tasks.compileTestJava {
    options.encoding = "UTF-8"
    options.setIncremental(true)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    exclude("**/samples/*")
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    doFirst {
        jvmArgs(
            "-javaagent:${agent.singleFile}"
        )
    }
}
