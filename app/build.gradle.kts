import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    //kotlinx serialization plugin
    kotlin("plugin.serialization")
}

val spotifyClientID = gradleLocalProperties(rootDir).getProperty("spotify_client_id")!!
val spotifyClientSecret = gradleLocalProperties(rootDir).getProperty("spotify_client_secret")!!

android {
    namespace = "pl.lambada.songsync"
    compileSdk = 33

    defaultConfig {
        applicationId = "pl.lambada.songsync"
        minSdk = 30
        targetSdk = 33
        versionCode = 11
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"$spotifyClientID\"")
            buildConfigField("String", "SPOTIFY_CLIENT_SECRET", "\"$spotifyClientSecret\"")
        }
        debug {
            buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"$spotifyClientID\"")
            buildConfigField("String", "SPOTIFY_CLIENT_SECRET", "\"$spotifyClientSecret\"")
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
        android.buildFeatures.buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.paging.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.31.3-beta")
    implementation("org.json:json:20230227")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("io.coil-kt:coil-compose:2.1.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.0-beta01")
    implementation("com.google.accompanist:accompanist-permissions:0.31.3-beta")
    implementation("com.google.accompanist:accompanist-coil:0.15.0")
    //kotlinx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
}