import java.util.Properties

plugins {
    kotlin("js") version "1.5.21"
}

group = "de.lehrbaum"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(npm("obsidian", "0.12.5", false))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.5.1")
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

val properties = Properties().apply { load(project.rootProject.file("local.properties").inputStream()) }
val obsidianPluginFolderPath = properties.getProperty("obsidianPluginFolderPath")
val obsidianPluginFolder = File(obsidianPluginFolderPath, "dnd-helper")

val copyPluginTask by tasks.register<Copy>("copyToObsidianVault") {
    from(
        layout.buildDirectory.file("distributions/main.js"),
        layout.buildDirectory.file("distributions/main.js.map"),
        layout.buildDirectory.file("distributions/manifest.json")
    ) // TODO add the manifest file
    into(obsidianPluginFolder)
    dependsOn("browserDistribution")
    group = "obsidian"
}
