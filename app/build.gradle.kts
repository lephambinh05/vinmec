plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Thêm dòng này để đảm bảo Firebase hoạt động
}


android {
    namespace = "com.example.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.app"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.firestore)

    implementation("androidx.work:work-runtime:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("androidx.work:work-runtime:2.9.0")
    implementation ("com.google.guava:guava:33.0.0-android")




    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-firestore:24.10.2")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    //Gson
    implementation("com.google.code.gson:gson:2.8.7")
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")



}
