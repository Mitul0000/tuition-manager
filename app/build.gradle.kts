plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("io.sentry.android.gradle")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.digifello.tutordesk"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.digifello.tutordesk"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    // External
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:35.2.0"))

    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-crashlytics")
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("com.google.firebase:firebase-messaging")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.airbnb.android:lottie-compose:6.5.2")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    implementation(platform("com.google.firebase:firebase-bom:34.16.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    //Default
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
configurations.all {
    resolutionStrategy {
        force(
            "com.google.firebase:firebase-auth:23.2.0",
            "com.google.firebase:firebase-auth-ktx:23.2.0",
            "com.google.android.gms:play-services-measurement:22.4.0",
            "com.google.android.gms:play-services-measurement-api:22.4.0",
            "com.google.android.gms:play-services-measurement-impl:22.4.0",
            "com.google.android.gms:play-services-measurement-sdk:22.4.0",
            "com.google.android.gms:play-services-measurement-sdk-api:22.4.0",
            "com.google.android.gms:play-services-measurement-base:22.4.0"
        )
    }
}

configurations.all {
    exclude(group = "com.google.firebase", module = "firebase-crashlytics")
    exclude(group = "com.google.firebase", module = "firebase-crashlytics-ktx")
    exclude(group = "com.google.firebase", module = "firebase-crashlytics-ndk")
}


sentry {
    includeSourceContext.set(true)
    org.set("digiasistant")
    projectName.set("kotlin")
    authToken.set(System.getenv("SENTRY_AUTH_TOKEN"))
}