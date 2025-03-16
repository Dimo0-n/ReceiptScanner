plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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
    //dependentele pentru modelul de AI
    implementation("org.tensorflow:tensorflow-lite:2.8.0") // Exemplu pentru TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite-support:0.3.1")
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.play.services.mlkit.text.recognition)// Suport pentru preprocesare
    runtimeOnly("org.tensorflow:tensorflow-lite-gpu:2.9.0")
    runtimeOnly("org.tensorflow:tensorflow-lite-task-vision:0.4.0")

    //ML Kit de la google alternativa pnetru ai-ul nostru
    // https://mvnrepository.com/artifact/com.google.mlkit/text-recognition
    runtimeOnly("com.google.mlkit:text-recognition:16.0.0")

    // https://mvnrepository.com/artifact/androidx.recyclerview/recyclerview
    runtimeOnly("androidx.recyclerview:recyclerview:1.4.0")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}