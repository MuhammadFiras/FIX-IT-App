plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics") // For Crashlytics
    id("kotlin-parcelize")
    id("com.google.devtools.ksp") // For KSP
    id("com.google.dagger.hilt.android") // <-- TAMBAHKAN BARIS INI
}

android {
    namespace = "com.example.fixit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fixit"
        minSdk = 24
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
    // KSP configuration for Room schema generation
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Firebase Platform (untuk memastikan semua versi Firebase kompatibel)
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    // Firebase Firestore (untuk database)
    implementation("com.google.firebase:firebase-firestore-ktx")
    // Firebase Authentication (opsional)
    implementation("com.google.firebase:firebase-auth-ktx")
    // Firebase Crashlytics
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    // Firebase Analytics (direkomendasikan bersama Crashlytics)
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-compose:4.3.0")
    // Google Places API (untuk pencarian lokasi dan autocomplete)
    implementation("com.google.android.libraries.places:places:3.4.0")
    // Google Location Services (PENTING UNTUK LocationRequest.Builder dan FusedLocationProviderClient)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Room for Local Database
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // --- DEPENDENSI HILT ---
    // Library Hilt utama
    implementation("com.google.dagger:hilt-android:2.51") // <-- TAMBAHKAN BARIS INI (VERSINYA SAMA DENGAN PLUGIN)
    // Annotation processor untuk Hilt (penting untuk code generation)
    ksp("com.google.dagger:hilt-compiler:2.51") // <-- TAMBAHKAN BARIS INI (Gunakan 'ksp' karena Anda sudah memakainya)

    // Dependensi Hilt untuk integrasi dengan Jetpack ViewModel/Compose
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0") // <-- TAMBAHKAN BARIS INI (untuk Compose Navigation)
    // Jika Anda menggunakan WorkManager dengan Hilt, Anda juga perlu ini:
    implementation("androidx.hilt:hilt-work:1.2.0") // <-- TAMBAHKAN BARIS INI (jika ingin WorkManager diinjeksi Hilt)
    ksp("androidx.hilt:hilt-compiler:1.2.0") // <-- TAMBAHKAN BARIS INI (annotation processor untuk androidx.hilt)

}