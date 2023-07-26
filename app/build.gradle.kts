plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.genius.coroutinesphoto"
    compileSdk = 33
    defaultConfig {
        applicationId = "com.genius.coroutinesphoto"
        minSdk = 16
        targetSdk = 33
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
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

val coroutinesVer: String by project
val verActivity = "1.7.2"
val varFragment = "1.6.0"

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation (kotlin("stdlib", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVer")
    implementation("androidx.activity:activity:$verActivity")
    implementation("androidx.activity:activity-ktx:$verActivity")
    implementation("androidx.fragment:fragment:$varFragment")
    implementation("androidx.fragment:fragment-ktx:$varFragment")
    implementation(project(":cphoto"))
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test:runner:1.5.2")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
}