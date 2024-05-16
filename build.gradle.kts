import java.time.Duration

plugins {
    java
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "ru.testit"

tasks.withType(JavaCompile::class.java).configureEach {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.encoding = "utf-8"
    options.setIncremental(true)
    options.isFork = true
}

nexusPublishing {
    connectTimeout.set(Duration.ofMinutes(7))
    clientTimeout.set(Duration.ofMinutes(7))

    transitionCheckOptions {
        maxRetries.set(100)
        delayBetween.set(Duration.ofSeconds(10))
    }

    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("MAVEN_USERNAME"))
            password.set(System.getenv("MAVEN_PASSWORD"))
        }
    }
}

configure(subprojects) {
    group = "ru.testit"
    version = version

    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "java")

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
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
        val signingKeyId: String? by project
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)

        sign(publishing.publications["maven"])
    }

    tasks.withType<Sign>().configureEach {
        if (System.getProperty("disableSign") == "true") {
            enabled = false;
        }

        onlyIf { !project.version.toString().endsWith("-SNAPSHOT") }
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    tasks.withType(JavaCompile::class.java).configureEach {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        options.encoding = "utf-8"
        options.setIncremental(true)
        options.isFork = true
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
        maven {
            val releasesUrl = uri("https://s01.oss.sonatype.org/content/repositories/releases")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
            url = if (version.toString().toLowerCase().contains("snapshot")) snapshotsUrl else releasesUrl

            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        mavenLocal()
        mavenCentral()
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}