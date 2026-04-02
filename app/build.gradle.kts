import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
    id("com.google.gms.google-services")
}


android {
    namespace = "com.suraksha.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.suraksha.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        mlModelBinding = true
    }
}

dependencies {
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation("org.tensorflow:tensorflow-lite:2.12.0")
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    
    // OSMDroid for map visualization
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    // OkHttp for Overpass API requests
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // This is the new line you must add for the permission launcher
    implementation("androidx.activity:activity-ktx:1.9.0")

    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // MPAndroidChart for debug visualizations (keep for debug tools)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Encrypted SharedPreferences for secure PIN storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Removed TensorFlow Lite — ML integration disabled
}

tasks.register("verifySGestureService") {
    group = "verification"
    description = "Checks that SGestureOverlayService class exists to avoid unresolved reference issues."
    doLast {
        val serviceFile = File(projectDir, "src/main/java/com/suraksha/app/services/SGestureOverlayService.kt")
        if (!serviceFile.exists()) {
            throw GradleException("SGestureOverlayService.kt missing. Please ensure the S gesture overlay service file is present.")
        } else {
            println("[verifySGestureService] Found SGestureOverlayService.kt")
        }
    }
}

tasks.named("preBuild").configure { dependsOn("verifySGestureService") }
