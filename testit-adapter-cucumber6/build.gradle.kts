description = "TestIT Cucumber6 Integration"

plugins {
    id("java")
}

val cucumberVersion = "6.10.2"
val cucumberGherkinVersion = "18.0.0"
val slf4jVersion = "1.7.36"
val testngVersion = "6.14.3"

dependencies {
    implementation(project(":testit-java-commons"))
    implementation("io.cucumber:gherkin:$cucumberGherkinVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("org.json:json:20220924")

    compileOnly("io.cucumber:cucumber-plugin:$cucumberVersion")

    testImplementation("io.cucumber:gherkin:$cucumberGherkinVersion")
    testImplementation("io.cucumber:cucumber-core:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-testng:$cucumberVersion")
    testImplementation("org.testng:testng:$testngVersion")
}

tasks.getByName<Test>("test") {
    useTestNG {}
    exclude("**/samples/*")
}

tasks.compileTestJava {
    options.encoding = "UTF-8"
}

tasks.jar {
    manifest {
        attributes(mapOf(
            "Automatic-Module-Name" to "ru.testit.cucumber6"
        ))
    }
}