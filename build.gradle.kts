// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.dokka)
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(rootProject.file("docs"))
    }
}

dependencies {
    dokka(project(":face-detection"))
    dokka(project(":face-landmark-detection"))
}