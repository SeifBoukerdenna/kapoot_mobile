buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin.v190)  // Match this version
        classpath(libs.hilt.android.gradle.plugin)
    }
}

plugins {
    id("com.android.application") version "8.8.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false    // Match this version
    id("org.jetbrains.kotlin.kapt") version "1.9.21" apply false      // Match this version
    id("com.google.dagger.hilt.android") version "2.50" apply false
    alias(libs.plugins.compose.compiler) apply false
}