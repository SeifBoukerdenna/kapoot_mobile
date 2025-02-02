plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.log3990.kapoot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.log3990.kapoot"
        minSdk = 34
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
    }
}

dependencies {
    // ─────────────────────────────────────────────────────────────────────────────
    //  Jetpack Compose
    // ─────────────────────────────────────────────────────────────────────────────
    implementation(libs.ui)
    implementation(libs.androidx.material)
    implementation(libs.ui.tooling.preview)
    implementation(libs.activity.compose)

    // ─────────────────────────────────────────────────────────────────────────────
    //  Lifecycle & ViewModel
    //   - If your version catalog has a "vlatestversion" variable, ensure it
    //     references a real version (e.g., 2.6.1).
    // ─────────────────────────────────────────────────────────────────────────────
    implementation(libs.androidx.lifecycle.runtime.ktx.vlatestversion)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ─────────────────────────────────────────────────────────────────────────────
    //  Retrofit & OkHttp
    //   Choose *one* set of versions. If your libs.versions.toml references both
    //   "libs.retrofit" and "libs.retrofit.v290" with duplicates, unify them.
    // ─────────────────────────────────────────────────────────────────────────────
    implementation(libs.retrofit.v290)
    implementation(libs.converter.gson.v290)
    implementation(libs.logging.interceptor.v493)

    // ─────────────────────────────────────────────────────────────────────────────
    //  Coroutines
    // ─────────────────────────────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)

    // ─────────────────────────────────────────────────────────────────────────────
    //  DataStore (for session storage)
    // ─────────────────────────────────────────────────────────────────────────────
    implementation(libs.androidx.datastore.preferences.v100)


    // ─────────────────────────────────────────────────────────────────────────────
    //  Other AndroidX Libraries
    // ─────────────────────────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // ─────────────────────────────────────────────────────────────────────────────
    //  Testing
    // ─────────────────────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
