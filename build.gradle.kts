import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

plugins {
    kotlin("js") version "1.5.20"
}

group = "de.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(npm("obsidian", "0.12.5", false))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.5.1")
}

kotlin {
    js(IR) {
        binaries.executable()
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
    }
}