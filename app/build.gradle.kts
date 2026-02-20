plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "zdrowy.senior.io"
    compileSdk = 34

    defaultConfig {
        applicationId = "zdrowy.senior.io"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "USE_FIREBASE_AUTH_EMULATOR", "true")
            buildConfigField("String", "FIREBASE_AUTH_EMULATOR_HOST", "\"10.0.2.2\"")
            buildConfigField("int", "FIREBASE_AUTH_EMULATOR_PORT", "9099")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "USE_FIREBASE_AUTH_EMULATOR", "false")
            buildConfigField("String", "FIREBASE_AUTH_EMULATOR_HOST", "\"\"")
            buildConfigField("int", "FIREBASE_AUTH_EMULATOR_PORT", "0")
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.koin.android)
    implementation(libs.timber)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(project(":ui"))
    implementation(project(":di"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
