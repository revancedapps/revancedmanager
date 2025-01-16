import java.io.FileInputStream
import java.util.Properties
import java.io.FileOutputStream

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

// Function to load version properties
fun loadVersionProps(): Properties {
    val versionPropsFile = project.rootProject.file("version.properties")
    val versionProps = Properties()

    if (versionPropsFile.exists()) {
        versionProps.load(FileInputStream(versionPropsFile))
    } else {
        // Initialize with default values if file doesn't exist
        versionProps["VERSION_CODE"] = "1"
        versionProps["VERSION_NAME_MAJOR"] = "2"
        versionProps["VERSION_NAME_MINOR"] = "0"
        versionProps["VERSION_NAME_PATCH"] = "0"
        versionProps.store(FileOutputStream(versionPropsFile), null)
    }

    return versionProps
}
// Load version properties
val versionProps = loadVersionProps()


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)

    kotlin("plugin.serialization")

}

android {
    namespace = "com.rv.net.rvmanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rv.net.rvmanager"
        minSdk = 24
        targetSdk = 34

        // Use version properties
        versionCode = versionProps["VERSION_CODE"].toString().toInt()
        versionName = "${versionProps["VERSION_NAME_MAJOR"]}.${versionProps["VERSION_NAME_MINOR"]}.${versionProps["VERSION_NAME_PATCH"]}"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


tasks.register("incrementVersion") {
    group = "versioning"
    description = "Increments version code and patch version"

    doLast {
        val versionPropsFile = project.rootProject.file("version.properties")
        val versionProps = loadVersionProps()

        // Increment version code
        val versionCode = versionProps["VERSION_CODE"].toString().toInt() + 1
        versionProps["VERSION_CODE"] = versionCode.toString()

        // Increment patch version
        val patch = versionProps["VERSION_NAME_PATCH"].toString().toInt() + 1
        versionProps["VERSION_NAME_PATCH"] = patch.toString()

        // Save updated properties
        versionProps.store(FileOutputStream(versionPropsFile), null)

        println("Version incremented to: ${versionProps["VERSION_NAME_MAJOR"]}.${versionProps["VERSION_NAME_MINOR"]}.${versionProps["VERSION_NAME_PATCH"]} (${versionCode})")
    }
}


tasks.register("rvRelease") {
    description = "Builds release APK, increments version, and copies to apk directory"

    // Make sure this task runs after assembleRelease
    dependsOn("assembleRelease")

    finalizedBy("incrementVersion")
    doLast {
        // Get the version name from android config
        val versionName = android.defaultConfig.versionName

        // Define source and destination files
        val sourceFile = layout.buildDirectory.file("outputs/apk/release/app-release.apk")
        val destinationDir = project.rootDir.resolve("apk")
        val destinationFile = destinationDir.resolve("rv.net_rv_manager_v${versionName}.apk")

        // Create destination directory if it doesn't exist
        destinationDir.mkdirs()

        // Copy and rename the file
        sourceFile.get().asFile.copyTo(destinationFile, overwrite = true)

        // Log success message
        println("APK copied to: ${destinationFile.absolutePath}")
    }
}

tasks.register("generateKeystore") {
    group = "security"
    description = "Generates a new keystore file with predefined credentials"

    doLast {
        // Load keystore properties
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        val keystoreProperties = Properties().apply {
            load(FileInputStream(keystorePropertiesFile))
        }

        // Extract keystore details from properties
        val storePassword = keystoreProperties["storePassword"] as String
        val keyPassword = keystoreProperties["keyPassword"] as String
        val keyAlias = keystoreProperties["keyAlias"] as String
        val storeFile = keystoreProperties["storeFile"] as String

        // Create keystore file in app folder
        val appFolder = project.projectDir
        val keystoreFile = File(appFolder, storeFile)

        // Prepare the keytool command
        val keyToolCommand = arrayOf(
            "keytool",
            "-genkey",
            "-v",
            "-keystore", keystoreFile.absolutePath,
            "-alias", keyAlias,
            "-keyalg", "RSA",
            "-keysize", "2048",
            "-validity", "10000",
            "-storepass", storePassword,
            "-keypass", keyPassword,
            "-dname", "CN=RV,OU=RV,O=RV,L=Unknown,ST=Unknown,C=US"
        )

        try {
            // Execute keytool command
            val process = ProcessBuilder(*keyToolCommand)
                .redirectErrorStream(true)
                .start()

            // Print the output
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { println(it) }
            }

            // Wait for the process to complete
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                println("""
                    ✅ Keystore generated successfully!
                    📁 Location: ${project.rootDir}/${storeFile}
                    🔑 Key Alias: $keyAlias
                    ⚡ Remember to keep your keystore file safe and secure!
                """.trimIndent())
            } else {
                println("❌ Failed to generate keystore. Exit code: $exitCode")
            }
        } catch (e: Exception) {
            println("❌ Error generating keystore: ${e.message}")
            throw e
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
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    // Add Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    // Add Material icons
    implementation("androidx.compose.material:material-icons-extended")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("org.greenrobot:eventbus:3.3.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("androidx.work:work-runtime-ktx:2.9.1")


    implementation("androidx.core:core-splashscreen:1.0.1")

}