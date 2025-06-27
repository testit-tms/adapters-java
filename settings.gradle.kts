rootProject.name = "adapters-java"
include("testit-java-commons")
include("testit-adapter-testng")
include("testit-adapter-junit4")
include("testit-adapter-junit5")
include("testit-adapter-cucumber4")
include("testit-adapter-cucumber5")
include("testit-adapter-cucumber6")
include("testit-adapter-cucumber7")
include("testit-adapter-jbehave")
include("testit-adapter-selenide")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "1.6.21"
    }
}
