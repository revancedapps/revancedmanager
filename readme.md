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
- Android SDK API 35
- Kotlin 2.0.0 or higher

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

The release build process requires a keystore file for signing the APK. For security reasons, this keystore is not included in the repository.

#### 1. Generate Keystore (First time only)

You have two options to create a keystore:

a) Using Gradle task (Recommended):
```bash
# This will generate a keystore with predefined secure settings
./gradlew generateKeystore
```

b) Manual creation using keytool:
```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```


⚠️ Important Security Notes:
- Keep your keystore file (`.jks`) and `keystore.properties` secure
- Never commit these files to version control
- Back up these files safely - losing them means you can't update your app on Play Store

#### 2. Build Release APK

You have two options to build the release APK:

a) Simple build:
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

b) Build with version management (Recommended):
```bash
./gradlew revancedRelease
# Output: apk/revanced_manager_v[VERSION].apk
```

The `revancedRelease` task offers additional benefits:
- Automatically increments version number
- Names output file with version number
- Creates a dedicated 'apk' directory for releases
- Makes version tracking easier
- Prevents confusion between different builds

Example output file: `revanced_manager_v2.0.1.apk`

#### 4. Verify Build

After building, verify your APK:
1. Check the signature:
```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```
2. Test installation on a device
3. Verify app functionality
4. Check the version number in app settings

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
- Built targeting Android 15 (API 35)
