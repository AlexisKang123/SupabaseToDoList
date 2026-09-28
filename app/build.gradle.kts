plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "br.com.todolistsupabase"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "br.com.todolistsupabase"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.recyclerview)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}