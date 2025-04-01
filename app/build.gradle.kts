import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") // ✅ Firebase services
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

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        val deepSeekApiKey = localProperties.getProperty("DEEPSEEK_API_KEY") ?: ""
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"$deepSeekApiKey\"")
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
        buildConfig = true
    }

    // ✅ Excludere fișiere inutile din APK
    packagingOptions {
        exclude("META-INF/NOTICE.md")
        exclude("META-INF/LICENSE.md")
        exclude("META-INF/INDEX.LIST")
        exclude("META-INF/DEPENDENCIES")
    }
}

dependencies {
    // ✅ TensorFlow Lite pentru AI/ML
    implementation("org.tensorflow:tensorflow-lite:2.8.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.3.1")
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.play.services.mlkit.barcode.scanning)
    runtimeOnly("org.tensorflow:tensorflow-lite-gpu:2.9.0")
    runtimeOnly("org.tensorflow:tensorflow-lite-task-vision:0.4.0")

    // ✅ ML Kit Text Recognition
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:16.0.0")

    // ✅ Dependințe Android
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)

    // ✅ Email (Java Mail API pentru Android)
    implementation("com.sun.mail:android-mail:1.6.7") {
        exclude(group = "com.sun.mail", module = "android-activation")
    }
    implementation("com.sun.mail:android-activation:1.6.7")

    // ✅ Testare
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ✅ OpenCV & OCR
    implementation("com.quickbirdstudios:opencv:4.5.3.0")
    implementation("com.rmtheis:tess-two:9.1.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // ✅ Firebase
    implementation("com.google.firebase:firebase-auth-ktx:22.1.1")
    implementation("com.google.firebase:firebase-storage-ktx:20.2.1")
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.1")

    // ✅ PDF Processing
    implementation("com.itextpdf:itextpdf:5.5.13.4")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    // ✅ Networking (Retrofit & OkHttp)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
}
