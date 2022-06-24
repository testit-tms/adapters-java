rootProject.name = "adapters-java"
include("testit-java-commons")
include("testit-adapter-testng")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version "1.6.21"
    }
}