# ReVanced Manager 2.0

A lightweight Android application that helps users manage, update, download, and uninstall applications efficiently. Built with modern Android development practices using Kotlin and Jetpack Compose.

## Features

- **App Management**: Easy installation, uninstallation, and updates
- **Material Design 3**: Modern and clean user interface
- **Download Management**: Efficient download handling with progress tracking
- **Version Control**: Track and compare installed vs latest versions
- **Dark Theme**: Built-in dark theme support
- **Adaptive Icons**: Modern Android adaptive icon support
- **Background Updates**: Automatic background version checking

## Tech Stack

- Kotlin
- Jetpack Compose
- Material Design 3
- Coroutines
- StateFlow
- EventBus
- OkHttp
- Coil for image loading
- AndroidX libraries

## Prerequisites

- Android Studio (latest version recommended)
- JDK 11 or higher
- Android SDK API 34
- Kotlin 1.9.0 or higher

## Setup Instructions

1. Clone the repository:
```bash
git clone https://github.com/nsknet/RevancedManager
```

2. Open Android Studio and select "Open an existing project"

3. Navigate to the cloned directory and click "OK"

4. Let Android Studio sync the project and download dependencies

5. Update local.properties with your SDK path:
```properties
sdk.dir=YOUR_ANDROID_SDK_PATH
```

## Building the Project

### Debug Build

1. In Android Studio:
   - Select `Build > Make Project` or press `Ctrl+F9` (Windows) / `Cmd+F9` (Mac)
   - Select `Run > Run 'app'` or press `Shift+F10` (Windows) / `Ctrl+R` (Mac)

2. Using Command Line:
```bash
# Navigate to project root
cd revanced-manager

# Build debug APK
./gradlew assembleDebug

# The APK will be in app/build/outputs/apk/debug/
```

### Release Build

1. Create a keystore file if you don't have one:
```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

2. Create/update `keystore.properties` in project root:
```properties
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=release
storeFile=../release-key.jks
```

3. Build release APK:
```bash
# Build release APK
./gradlew assembleRelease

# The APK will be in app/build/outputs/apk/release/
```

## Project Structure

- `\app\src\main\java\com\revanced\net\revancedmanager`
  - `MainActivity.kt`: Main entry point
  - `AppViewModel.kt`: Manages app state and business logic
  - `AppItem.kt`: Data models
  - `RvDownloader.kt`: Download management
  - `AppInstaller.kt`: Installation handling
  - `AppUninstaller.kt`: Uninstallation handling
  - `ui/`: UI components and themes
  - `utils/`: Utility classes

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [ReVanced Project](https://revanced.net/)
- Icons and graphics from Material Design

## Contact

- Website: [revanced.net](https://revanced.net)
- Support: [Ko-fi](https://ko-fi.com/revancednet)

## Notes

- This app requires Android 7.0 (API 24) or higher
- Some features may require additional permissions
- Built targeting Android 14 (API 34)
