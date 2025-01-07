plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.compose")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("maven-publish")
}

android {
    namespace = "com.chatSDK.SupportSync"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 34

        // For libraries, use these instead of versionCode and versionName
        group = "com.chatSDK"
        version = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // or the latest compatible version
    }

}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.chatSDK"
                artifactId = "SupportSync"
                version = "1.0.1"

                from(components["release"])

                pom {
                    name.set("SupportSync")
                    description.set("A chat support SDK for Android applications")
                    url.set("https://github.com/krishnapoddar1592/SupportSync")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // OkHttp
    implementation(libs.okhttp)

    // Optional logging interceptor for debugging
    implementation(libs.logging.interceptor)

    // Retrofit for REST API calls
    implementation(libs.retrofit)
    implementation(libs.converter.gson)  // For JSON conversion

    // Coroutines support for networking
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.adapter.rxjava2)

    // Coil for image loading (used in ImageMessageBubble)
    implementation(libs.coil.compose)
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Hilt ViewModel
    implementation("androidx.hilt:hilt-work:1.1.0")
    kapt("androidx.hilt:hilt-compiler:1.1.0")

    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //stomp protocl dependency
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")

    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21") // RxJava dependency
    implementation("org.apache.commons:commons-text:1.10.0")
}
