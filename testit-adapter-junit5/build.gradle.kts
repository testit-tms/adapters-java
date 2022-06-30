description = "TestIT JUnit 5 Integration"

plugins {
    id("java")
}

val junitVersion = "5.8.2"
val aspectjVersion = "1.9.7"
val slf4jVersion = "1.7.36"
val agent: Configuration by configurations.creating

dependencies {
    agent("org.aspectj:aspectjweaver:$aspectjVersion")

    implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    //implementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation(project(":testit-java-commons"))

    testImplementation(project(":testit-java-commons"))
    testImplementation("org.aspectj:aspectjrt:$aspectjVersion")
    testImplementation("org.aspectj:aspectjweaver:$aspectjVersion");
    //testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    //testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
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
    options.encoding = "UTF-8"
}

//tasks.test {
//    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
//    useJUnitPlatform()
//    //exclude("**/features/*")
//    doFirst {
//        println("doFirst")
//        jvmArgs("-javaagent:${agent.singleFile}")
//        //jvmArgs("-javaagent:${weaver}")
//        //def weaver = configurations.compile.find { it.name.contains("aspectjweaver") }
//    }
//}

tasks.getByName<Test>("test") {
    //systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    useJUnitPlatform()
    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    doFirst {
        println("doFirst")
        jvmArgs("-javaagent:${agent.singleFile}")
        println("doFirst")
        //jvmArgs("-javaagent:${weaver}")
        //def weaver = configurations.compile.find { it.name.contains("aspectjweaver") }
    }
}