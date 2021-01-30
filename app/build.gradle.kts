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
    buildFeatures {
        viewBinding = true
    }
}

val verCoroutines = "1.4.2"
val verActivity = "1.2.0-rc02"
val varFragment = "1.3.0-rc02"

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation (kotlin("stdlib-jdk7", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))
    implementation ("androidx.appcompat:appcompat:1.2.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:$verCoroutines")
    implementation("androidx.activity:activity:$verActivity")
    implementation("androidx.activity:activity-ktx:$verActivity")
    implementation("androidx.fragment:fragment:$varFragment")
    implementation("androidx.fragment:fragment-ktx:$varFragment")
    implementation(project(":cphoto"))
    testImplementation ("junit:junit:4.13.1")
    androidTestImplementation ("androidx.test:runner:1.3.0")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.3.0")
}