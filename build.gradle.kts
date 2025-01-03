

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.49")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")// Use the latest version


    }
}


plugins {
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("org.jetbrains.compose") version "1.5.11" apply false
    id("maven-publish")
}
