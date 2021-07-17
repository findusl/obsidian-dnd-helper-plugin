
plugins {
    kotlin("js") version "1.5.20"
}

group = "de.lehrbaum"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(npm("obsidian", "0.12.5", false))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.5.1")

    testImplementation(kotlin("test"))
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

val pluginPath = "/Users/slehrbaum/OneDrive/My_DND5e_Campaign/.obsidian/plugins/dnd-generator"
val pluginFolder = file(pluginPath)

val copyPluginTask by tasks.register<Copy>("copyToObsidianVault") {
    from(layout.buildDirectory.file("distributions/main.js")) // TODO add the manifest file
    into(pluginFolder)
    dependsOn("build")
    group = "obsidian"
}
