import java.time.Duration

plugins {
    java
    `maven-publish`
    signing
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "ru.testit"

tasks.withType(JavaCompile::class.java).configureEach {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.encoding = "utf-8"
    options.setIncremental(true)
    options.isFork = true
}

configure(subprojects) {
    group = "ru.testit"
    version = version

    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "java")

    publishing {
        repositories {
            // JReleaser staging repository
            maven {
                name = "staging"
                url = uri(layout.buildDirectory.dir("staging-deploy"))
            }
        }
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
        mavenLocal()
        mavenCentral()
    }
}

// JReleaser helper tasks
tasks.register("jreleaserStage") {
    group = "publishing"
    description = "Stages all modules for JReleaser deployment"
    
    // Depend on publishing tasks from all subprojects
    subprojects.forEach { project ->
        dependsOn("${project.path}:publishMavenPublicationToStagingRepository")
    }
    
    doLast {
        println("✅ All modules staged for JReleaser deployment")
        println("📁 Staging directories:")
        subprojects.forEach { project ->
            val stagingDir = project.layout.buildDirectory.dir("staging-deploy").get().asFile
            if (stagingDir.exists()) {
                println("   ${project.name}: ${stagingDir.absolutePath}")
            }
        }
        println("🚀 Run 'jreleaser deploy' to publish to Maven Central")
    }
}