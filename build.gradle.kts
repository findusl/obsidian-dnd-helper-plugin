import java.util.Properties

plugins {
    kotlin("js") version "1.7.20"
}

group = "de.lehrbaum"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(npm("obsidian", "0.14.8", false))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.5")
}

kotlin {
    js(IR) {
        browser {
            useCommonJs()
            webpackTask {
                output.libraryTarget = "commonjs"
                output.library = null
                outputFileName = "main.js"
            }
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        binaries.executable()
    }
}

private val propertiesFile = project.rootProject.file("local.properties")
if (propertiesFile.exists()) {
    val properties = Properties().apply { load(propertiesFile.inputStream()) }
    val obsidianPluginFolderPath = properties.getProperty("obsidianPluginFolderPath")
    val obsidianPluginFolder = File(obsidianPluginFolderPath, "dnd-helper")

    val copyPluginTask by tasks.register<Copy>("copyToObsidianVault") {
        group = "obsidian"
        from(
            layout.buildDirectory.file("distributions/main.js"),
            layout.buildDirectory.file("distributions/main.js.map"),
            layout.buildDirectory.file("distributions/manifest.json"),
            layout.buildDirectory.file("distributions/style.css")
        )
        into(obsidianPluginFolder)
        dependsOn("browserDistribution")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
