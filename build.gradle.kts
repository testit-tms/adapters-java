plugins {
    java
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    jacoco
    id("org.sonarqube") version "3.4.0.2513"
}

sonarqube {
    properties {
        properties["sonar.projectKey"] = "testit-tms_adapters-java"
        properties["sonar.organization"] = "testit-tms"
        properties["sonar.host.url"] = "https://sonarcloud.io"
    }
}
group = "ru.testit"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

configure(subprojects) {
    group = "ru.testit"
    version = version

    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "java")
    apply(plugin = "jacoco")

    publishing {
        publications {
            create<MavenPublication>("maven") {
                suppressAllPomMetadataWarnings()
                versionMapping {
                    allVariants {
                        fromResolutionResult()
                    }
                }
                pom {
                    name.set(project.name)
                    description.set("Module ${project.name} of TestIT Framework.")
                    url.set("https://github.com/testit-tms/adapters-java")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("integration")
                            name.set("Integration team")
                            email.set("integrations@testit.software")
                        }
                    }
                    scm {
                        developerConnection.set("scm:git:git://github.com/testit-tms/adapters-java")
                        connection.set("scm:git:git://github.com/testit-tms/adapters-java")
                        url.set("https://github.com/testit-tms/adapters-java")
                    }
                    issueManagement {
                        system.set("GitHub Issues")
                        url.set("https://github.com/testit-tms/adapters-java/issues")
                    }
                }
            }
        }
    }

    signing {
        sign(publishing.publications["maven"])
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    tasks.withType<Sign>().configureEach {
        onlyIf { !project.version.toString().endsWith("-SNAPSHOT") }
    }

    tasks.withType<GenerateModuleMetadata> {
        enabled = false
    }

    tasks.jar {
        manifest {
            attributes(mapOf(
            "Specification-Title" to project.name,
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
            ))
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    publishing.publications.named<MavenPublication>("maven") {
        pom {
            from(components["java"])
        }
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.required.set(false)
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}