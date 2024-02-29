description = "TestIT Cucumber4 Integration"

plugins {
    id("java")
}

val cucumberVersion = "4.8.0"
val slf4jVersion = "1.7.36"
val testngVersion = "6.14.3"

dependencies {
    implementation(project(":testit-java-commons"))
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("io.cucumber:cucumber-core:$cucumberVersion")
    implementation("io.cucumber:cucumber-java:$cucumberVersion")
    implementation("org.json:json:20220924")

    testImplementation("io.cucumber:cucumber-core:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-testng:$cucumberVersion")
    testImplementation("org.testng:testng:$testngVersion")
}

tasks.getByName<Test>("test") {
    useTestNG {}
    exclude("**/samples/*")
    //maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}

tasks.compileTestJava {
    options.encoding = "UTF-8"
    options.setIncremental(true)
}

tasks.jar {
    manifest {
        attributes(mapOf(
            "Automatic-Module-Name" to "ru.testit.cucumber4"
        ))
    }
}