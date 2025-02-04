pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Kapoot"
include(":app")


dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("hilt-android", "com.google.dagger:hilt-android:2.50")
            library("hilt-compiler", "com.google.dagger:hilt-android-compiler:2.50")
            library("hilt-android-gradle-plugin", "com.google.dagger:hilt-android-gradle-plugin:2.50")
            library("androidx-hilt-navigation-compose", "androidx.hilt:hilt-navigation-compose:1.1.0")
            library("androidx-navigation-compose", "androidx.navigation:navigation-compose:2.7.6")
            library("hilt-android-testing", "com.google.dagger:hilt-android-testing:2.50")

            // Compose
            library("androidx-compose-bom", "androidx.compose:compose-bom:2024.02.00")
            library("ui", "androidx.compose.ui:ui:1.5.4")
            library("androidx-material", "androidx.compose.material:material:1.5.4")
            library("ui-tooling-preview", "androidx.compose.ui:ui-tooling-preview:1.5.4")
            library("activity-compose", "androidx.activity:activity-compose:1.8.2")

            // Lifecycle
            library("androidx-lifecycle-runtime-ktx-vlatestversion", "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
            library("androidx-lifecycle-viewmodel-compose", "androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

            // Retrofit & OkHttp
            library("retrofit-v290", "com.squareup.retrofit2:retrofit:2.9.0")
            library("converter-gson-v290", "com.squareup.retrofit2:converter-gson:2.9.0")
            library("logging-interceptor-v493", "com.squareup.okhttp3:logging-interceptor:4.9.3")

            // Socket.IO
            library("socket-io-client", "io.socket:socket.io-client:2.1.0")

            // Coroutines
            library("kotlinx-coroutines-android", "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

            // DataStore
            library("androidx-datastore-preferences-v100", "androidx.datastore:datastore-preferences:1.0.0")

            // Core
            library("androidx-core-ktx", "androidx.core:core-ktx:1.12.0")
            library("androidx-material3", "androidx.compose.material3:material3:1.1.2")

            // Testing
            library("junit", "junit:junit:4.13.2")
            library("androidx-junit", "androidx.test.ext:junit:1.1.5")
            library("androidx-espresso-core", "androidx.test.espresso:espresso-core:3.5.1")
            library("androidx-ui-test-junit4", "androidx.compose.ui:ui-test-junit4:1.5.4")
            library("androidx-ui-test-manifest", "androidx.compose.ui:ui-test-manifest:1.5.4")

            // Plugins
            plugin("android-application", "com.android.application").version("8.2.0")
            plugin("kotlin-android", "org.jetbrains.kotlin.android").version("1.9.21")
            plugin("kotlin-kapt", "org.jetbrains.kotlin.kapt").version("1.9.21")
            plugin("hilt-android", "com.google.dagger.hilt.android").version("2.50")
        }
    }
}