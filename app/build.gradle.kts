plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") // ✅ Add this line
}

android {
    namespace = "com.example.myapplicationtmppp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplicationtmppp"
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
        viewBinding = true
    }
}

dependencies {
    // Dependințe pentru modelul de AI cu TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.8.0") // TensorFlow Lite core
    implementation("org.tensorflow:tensorflow-lite-support:0.3.1") // Suport preprocesare
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.play.services.mlkit.text.recognition)
    runtimeOnly("org.tensorflow:tensorflow-lite-gpu:2.9.0") // GPU acceleration
    runtimeOnly("org.tensorflow:tensorflow-lite-task-vision:0.4.0") // Task vision

    // ML Kit de la Google - alternativă pentru AI
    runtimeOnly("com.google.mlkit:text-recognition:16.0.0")

    // Dependințe Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)

    // Testare
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ✅ OpenCV pentru procesare imagini
    implementation("com.quickbirdstudios:opencv:4.5.3.0")

    // ✅ Tesseract OCR pentru recunoaștere text
    implementation("com.rmtheis:tess-two:9.1.0")

    // ✅ Google ML Kit pentru scanare coduri QR
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx:22.1.1")

    // Firebase Storage (pentru încărcarea securizată a imaginilor)
    implementation("com.google.firebase:firebase-storage-ktx:20.2.1")

    // Firebase Firestore (dacă stochezi informații despre utilizatori)
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.1")
}
