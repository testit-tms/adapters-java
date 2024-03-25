description = "TestIT Selenide Integration"

plugins {
    id("java")
}

repositories {
    mavenCentral()
}

val selenideVersion = "6.11.2"

dependencies {
    implementation(project(":testit-java-commons"))
    compileOnly("com.codeborne:selenide:$selenideVersion")
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
    options.encoding = "UTF-8"
    options.setIncremental(true)
    // Allows the adapter to accept real parameter names
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
}