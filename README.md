# SupportSync SDK

SupportSync is a versatile Android chat SDK that enables seamless integration of customer support functionality into your Android applications. Built with modern Android development practices and Jetpack Compose, it provides a robust real-time chat solution with support for text messages and image sharing.


<img width="339" alt="Screenshot 2025-01-03 at 12 58 15 AM" src="https://github.com/user-attachments/assets/978696d4-7076-466b-bb8e-743377e3e1a6" />

<img width="345" alt="Screenshot 2025-01-03 at 12 59 54 AM" src="https://github.com/user-attachments/assets/51415b08-77be-4987-9473-dbdc818bef88" />


---

## Features

- 🌐 **Real-time communication** using WebSocket
- 🖼️ **Image sharing** and message threading
- 🎨 **Fully customizable UI** using Jetpack Compose
- 🧑‍🎨 **Material 3 design principles** for modern aesthetics
- 🛡️ **Built-in error handling** and automatic reconnections
- 📝 **Pre-chat form** for collecting user details and categorizing issues
- 🏗️ **Clean and scalable architecture**


---

## Prerequisites

Before integrating the SDK, ensure your project meets the following requirements:

- **Android Studio Version**: Arctic Fox or later.
- **Minimum SDK Version**: 24 (Android 7.0).
- **Kotlin Version**: 1.9.21 or later.
- **Java Version**: 17 or later.
- **Hilt Setup**: The application must be configured as a **Hilt Android App**.

---

## Installation

### Step 1: Add Maven Central Repository and jitpack repository
Add the Maven Central repository in your `settings.gradle.kts` or `build.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add the Dependency
Include the **SupportSync** dependency in your app's `build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.chatSDK:supportsync:1.0.0")

    // Required dependencies for SupportSync
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // WebSocket and network dependencies
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.4.0")
}
```

### Step 3: Apply Hilt Plugin
Add the Hilt plugin to your `build.gradle.kts`:
```kotlin
plugins {
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
}
```

---

## Configuration

### Step 1: Configure Hilt
Mark your application class as a **Hilt Android App**:
```kotlin
@HiltAndroidApp
class YourApplication : Application()
```

Ensure the `AndroidManifest.xml` includes your application class:
```xml
<application
    android:name=".YourApplication"
    ... >
</application>
```

### Step 2: Initialize SupportSync
Initialize the SDK in your `Application` class:
```kotlin
@HiltAndroidApp
class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        SupportSync.builder(this)
            .serverUrl("https://your-server-url.com")
            .apiKey("Basic " + Base64.getEncoder().encodeToString("username:password".toByteArray()))
            .user(123L, "User123")
            .build()
    }
}
```

---

## Usage

### Display Chat Interface
Call the following method to show the chat interface from an activity:
```kotlin
SupportSync.getInstance().showSupportChat(this)
```

### Pre-Chat Customization (Optional)
Use the pre-chat form to collect additional user input:
```kotlin
PreChatScreen { selectedCategory, title, description ->
    // Handle pre-chat details here
}
```

### Customize the Theme
Modify the chat interface theme using `SupportSyncTheme`:
```kotlin
val customTheme = SupportSyncTheme.Default.copy(
    primaryColor = Color(0xFF6200EE),
    secondaryColor = Color(0xFF03DAC6)
)

SupportSync.builder(this)
    .theme(customTheme)
    .build()
```

---

## Advanced Features

### Feature Configuration
Enable or disable specific features:
```kotlin
val features = Features(
    imageUpload = true,
    typing = true
)

SupportSync.builder(this)
    .features(features)
    .build()
```

### Error Handling
The SDK handles errors such as:
- WebSocket connection failures
- Image upload errors
- Session management issues

Errors are logged to help with debugging.

---

## Testing

### Unit Testing
Add `JUnit` to your testing dependencies:
```kotlin
testImplementation("junit:junit:4.13.2")
```

### UI Testing
Include testing dependencies for Jetpack Compose:
```kotlin
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.1")
```

### Mocking Dependencies
Use Hilt testing for dependency injection:
```kotlin
androidTestImplementation("com.google.dagger:hilt-android-testing:2.50")
kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.50")
```

---

## Dependencies

- **Core Libraries**: Jetpack Compose, Kotlin Coroutines, Hilt.
- **Networking**: Retrofit, OkHttp, STOMP Protocol.
- **Image Handling**: Coil.
- **Reactive Programming**: RxJava, RxAndroid.

---

## FAQs

**1. What is the minimum SDK requirement?**  
The library requires a minimum SDK version of 24.

**2. Is internet permission required?**  
Yes, add the following to your `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

**3. Can I customize the chat UI?**  
Yes, you can modify the theme or extend the provided components.

---

## Support

For support, email **[krishnapoddar2071@gmail.com](mailto:krishnapoddar2071@gmail.com)** or create an issue on the GitHub repository.

---

## License

Licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
```
