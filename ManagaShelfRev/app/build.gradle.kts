plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.bluebirdcorp.managashelfrev"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bluebirdcorp.managashelfrev"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.splashscreen)
    kapt(libs.compiler)

//  Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

//  Room
    implementation(libs.room.runtime)
    kapt("androidx.room:room-compiler:2.6.1")
    implementation(libs.room.ktx)
    implementation(libs.androidx.paging.runtime.ktx)

//  Lifecycle components
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)

//  Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

//  Coil
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.glide)
    implementation ("com.google.android.material:material:1.12.0")

}