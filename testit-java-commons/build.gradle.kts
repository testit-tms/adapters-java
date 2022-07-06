plugins {
    id("java")
}

val slf4jVersion = "1.7.36"
val jacksonVersion = "2.13.3"

dependencies {
    implementation("org.aspectj:aspectjrt:1.9.7")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("ru.testit:testit-api-client:0.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
