plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.serialization)
}

android {
    namespace = "com.OxGames.Pluvia"
    compileSdk = 34
    ndkVersion = "22.1.7171670"

    defaultConfig {
        applicationId = "com.OxGames.Pluvia"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        proguardFiles(
            // getDefaultProguardFile("proguard-android-optimize.txt"),
            getDefaultProguardFile("proguard-android.txt"),
            "proguard-rules.pro"
        )
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
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
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(libs.zxing)
    // implementation(libs.steamkit)
    implementation(files("../../../IntelliJ/JavaSteam/build/libs/javasteam-1.6.0-SNAPSHOT.jar"))
    implementation(libs.protobuf.java) // only needed when building JavaSteam manually
    implementation(libs.commons.lang3) // only needed when building JavaSteam manually
    implementation(libs.xz) // only needed when building JavaSteam manually
    implementation(libs.commons.io) // only needed when building JavaSteam manually
    implementation(libs.commons.validator) // only needed when building JavaSteam manually (should crash without it, but I don't on some devices)

    implementation(libs.apache.compress) // for winlator
    implementation("com.github.luben:zstd-jni:1.5.2-3@aar") // for winlator
    implementation(libs.android.preferences) // for winlator

    implementation(libs.navigation.compose)
    implementation(libs.kotlin.coroutines)
    implementation(libs.spongycastle)
    implementation(libs.coil.compose)
    implementation(libs.coil.network)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.jetbrains.kotlinx.json)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}