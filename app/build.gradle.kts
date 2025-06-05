plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.promptmaster"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.promptmaster"
        minSdk = 24
        targetSdk = 35
        versionCode = 6
        versionName = "6.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    kotlin {
        jvmToolchain(17) // o 11 si tu JDK base es Java 11
    }


    buildFeatures {
        compose = true
        viewBinding = true // Keep view binding for now, might be needed for some interop
        dataBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13" // Use the appropriate Compose compiler version
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {
    // Core Android
    implementation(libs.core.ktx)
    implementation(libs.appcompat) // Keep appcompat for basic compatibility
    implementation(libs.material) // Keep material for basic compatibility

    // Jetpack Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.navigation.ui)
    implementation("androidx.navigation:navigation-fragment-ktx")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.foundation.layout.android)
    implementation(libs.preference.ktx)
    // Room
    val room_version = "2.6.1" // Use the appropriate Room version
    implementation("androidx.room:room-runtime:$room_version")
   /// annotationProcessor("androidx.room:room-compiler:$room_version")
    // If using KSP, replace annotationProcessor with ksp and add the KSP plugin
    //ksp("androidx.room:room-compiler:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Gson for Type Converters
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    androidTestImplementation(libs.espresso.core)

    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
