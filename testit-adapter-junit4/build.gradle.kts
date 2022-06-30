description = "TestIT JUnit 4 Integration"

plugins {
    id("java")
}

val junitVersion = "12.2.0"
val aspectjVersion = "1.9.7"
val slf4jVersion = "1.7.36"
val agent: Configuration by configurations.creating

dependencies {
    agent("org.aspectj:aspectjweaver:$aspectjVersion")

    implementation("com.nordstrom.tools:junit-foundation:$junitVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation(project(":testit-java-commons"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.aspectj:aspectjrt:$aspectjVersion")
    testImplementation("org.aspectj:aspectjweaver:$aspectjVersion");
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
}

tasks.jar {
    manifest {
        attributes(mapOf(
                "Automatic-Module-Name" to "ru.testit.junit4"
        ))
    }
    from("src/main/services") {
        into("META-INF/services")
    }
}

tasks.compileTestJava {
    options.encoding = "UTF-8"
}

//tasks.getByName<Test>("test") {
//    useJUnit(closureOf<TestNGOptions> {
//        suites("src/test/resources/testng.xml")
//    })
//    //exclude("**/samples/*")
//    doFirst {
//        jvmArgs("-javaagent:${agent.singleFile}")
//    }
//}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    //exclude("**/features/*")
    doFirst {
        jvmArgs("-javaagent:${agent.singleFile}")
    }
}
