plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.crimealert"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.crimealert"
        minSdk = 28
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.play.services.cast.framework)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Google Material library
    implementation("com.google.android.material:material:1.9.0")

    // Firebase BOM (Bill of Materials) for managing Firebase versions
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Firebase Authentication library
    implementation("com.google.firebase:firebase-auth")

    // Google Play services library
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Circular ImageView library
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Glide library
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ExoPlayer library
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")

    // Coroutines libraries
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0")

    // Firebase Firestore KTX for coroutine support
    implementation("com.google.firebase:firebase-firestore-ktx:24.5.0")

    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation ("com.squareup.okhttp3:okhttp:4.10.0")

    implementation("org.osmdroid:osmdroid-android:6.1.11")
}