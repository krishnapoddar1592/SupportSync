# SupportSync SDK

SupportSync is a versatile Android chat SDK that enables seamless integration of customer support functionality into your Android applications. Built with modern Android development practices and Jetpack Compose, it provides a robust real-time chat solution with support for text messages and image sharing.


<img width="339" alt="Screenshot 2025-01-03 at 12 58 15 AM" src="https://github.com/user-attachments/assets/978696d4-7076-466b-bb8e-743377e3e1a6" />

<img width="345" alt="Screenshot 2025-01-03 at 12 59 54 AM" src="https://github.com/user-attachments/assets/51415b08-77be-4987-9473-dbdc818bef88" />





## Features

- 🚀 Real-time chat functionality using WebSocket
- 🖼️ Image sharing capabilities
- 📱 Modern UI with Jetpack Compose
- 🎨 Material 3 Design
- 🔄 Automatic reconnection handling
- 📊 Different issue categories
- 💻 Clean Architecture

## Prerequisites

- Android Studio Arctic Fox or later
- Minimum SDK version: 21 (Android 5.0)
- Kotlin version: 1.8.0 or later
- Java 11

## Installation

1. Add the JitPack repository to your root build.gradle:
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependency to your app's build.gradle:
```gradle
dependencies {
    implementation 'com.github.YourUsername:SupportSync:1.0.0'
}
```

## Usage

### Initialize SupportSync

Initialize the SDK in your Application class:

```kotlin
class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        SupportSync.builder(this)
            .serverUrl("YOUR_SERVER_URL")
            .apiKey("YOUR_API_KEY")
            .build()
    }
}
```

### Start a Chat Session

```kotlin
SupportSync.getInstance().showSupportChat(
    activity = this,
    config = SupportSyncConfig(
        serverUrl = "YOUR_SERVER_URL",
        wsUrl = "YOUR_WEBSOCKET_URL",
        apiKey = "YOUR_API_KEY",
        theme = SupportSyncTheme.Default,
        features = Features()
    )
)
```

### Customization

You can customize the appearance using SupportSyncTheme:

```kotlin
val customTheme = SupportSyncTheme.Default.copy(
    // Add your custom theme properties
)

SupportSync.builder(context)
    .theme(customTheme)
    .build()
```


## Key Components

- `ChatScreen`: Main chat interface
- `PreChatScreen`: Initial screen for issue categorization
- `WebSocketService`: Handles real-time communication
- `RestApiService`: Handles the image uploading and session creation
- `ChatRepository`: Manages chat data and operations
- `ChatViewModel`: Handles UI state and business logic

## Error Handling

The SDK includes comprehensive error handling:

- WebSocket connection errors
- Image upload failures
- Network connectivity issues
- Session management errors

## Dependencies

- Jetpack Compose
- Dagger Hilt
- Kotlin Coroutines
- OkHttp
- Retrofit
- STOMP Protocol
- Coil for image loading

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request


## Support

For support, email krishnapoddar2071@gmail.com or create an issue in the GitHub repository.

## Acknowledgments

- Material Design 3 Guidelines
- Android Jetpack libraries
- STOMP protocol for WebSocket communication
