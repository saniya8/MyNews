import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Add the Google services Gradle plugin
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    kotlin("plugin.serialization") version "2.0.20"
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

android {
    namespace = "com.example.mynews"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mynews"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "NEWS_API_KEY", "\"${localProperties["NEWS_API_KEY"]}\"")
        buildConfigField("String", "DIFFBOT_API_KEY", "\"${localProperties["DIFFBOT_API_KEY"]}\"")
        buildConfigField("String", "HUGGINGFACE_API_KEY", "\"${localProperties["HUGGINGFACE_API_KEY"]}\"")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // Refer to libs.versions.toml

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    //implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-auth:23.2.0")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.2")

    // Dagger Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.hilt.compiler)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform("androidx.compose:compose-bom:2025.01.01"))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)

    // Jetpack Compose Material
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended.android)

    // Jetpack Compose Compiler
    implementation(libs.androidx.compiler)

    // Retrofit API
    val retrofitVersion = "2.11.0"
    implementation ("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation ("com.squareup.retrofit2:converter-gson:$retrofitVersion")

    // Ktor API

    // Ktor client core
    implementation("io.ktor:ktor-client-core:3.1.0")
    implementation("io.ktor:ktor-client-cio:3.1.0")
    implementation ("io.ktor:ktor-client-encoding:3.1.0")
    implementation ("io.ktor:ktor-client-android:3.1.0")

    // Ktor serialization (Kotlinx)
    implementation("io.ktor:ktor-client-content-negotiation:3.1.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.0")

    // Logging (Optional but useful)
    implementation("io.ktor:ktor-client-logging:3.1.0")

    // Coil - Image Loading
    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0") //- don't need this now but this is in COIL documentation - might need later

    // Serialization For Navigation
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1") // works with Kotlin 2.0

    // Condensed Article Dependencies For Parsing
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("org.json:json:20210307")

    // Unit Testing - Test
    testImplementation("junit:junit:4.13.2") // JUnit 4
    testImplementation("org.mockito:mockito-core:5.11.0") // Mockito fpr mocking
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1") // Mockito for mocking
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3") // coroutines
    testImplementation("app.cash.turbine:turbine:1.0.0") // flow testing
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Unit Testing - Android Test

    // Required to use JUnit4 runner in androidTest/
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    // Needed for  coroutines in instrumented tests
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    // Firebase for instrumented tests (same versions as main ones)
    androidTestImplementation("com.google.firebase:firebase-auth:23.2.0")
    androidTestImplementation("com.google.firebase:firebase-firestore-ktx:25.1.2")



}

// Allow references to generated code
kapt {
    correctErrorTypes = true
    useBuildCache = true
}
