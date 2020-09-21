plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "com.genius.coroutinesphoto"
        minSdkVersion(16)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

val verCoroutines = "1.3.9"

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation (kotlin("stdlib-jdk7", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))
    implementation ("androidx.appcompat:appcompat:1.2.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.0.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:$verCoroutines")
    implementation(project(":cphoto"))
    testImplementation ("junit:junit:4.13")
    androidTestImplementation ("androidx.test:runner:1.3.0")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.3.0")
}