description = "TestIT TestNG Integration"

plugins {
    id("java")
}

val testNgVersion = "7.5"
val aspectjVersion = "1.9.7"
val slf4jVersion = "1.7.36"
val agent: Configuration by configurations.creating

dependencies {
    agent("org.aspectj:aspectjweaver:$aspectjVersion")

    implementation("org.testng:testng:$testNgVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation(project(":testit-java-commons"))

    testImplementation("org.aspectj:aspectjrt:$aspectjVersion")
    testImplementation("org.testng:testng:$testNgVersion")
}

tasks.getByName<Test>("test")  {
    useTestNG(closureOf<TestNGOptions> {
        suites("src/test/resources/testng.xml")
    })
    exclude("**/samples/*")
    doFirst {
        jvmArgs("-javaagent:${agent.singleFile}")
    }
    systemProperties(System.getProperties().toMap() as Map<String,Object>)
}

tasks.compileTestJava {
    options.encoding = "UTF-8"
    // Allows the adapter to accept real parameter names
    options.compilerArgs.add("-parameters")
}

tasks.jar {
    manifest {
        attributes(mapOf(
            "Automatic-Module-Name" to "ru.testit.testng"
        ))
    }
    from("src/main/services") {
        into("META-INF/services")
    }
}