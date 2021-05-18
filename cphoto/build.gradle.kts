import org.jetbrains.kotlin.config.KotlinCompilerVersion

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.15.1")
    }
}

apply(plugin = "com.vanniktech.maven.publish")

plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.dokka")
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(buildDir.resolve("javadoc"))
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(16)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "3.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = false
    }
}

val verCoroutines = "1.5.0"
val verActivity = "1.2.3"
val varFragment = "1.3.3"

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    compileOnly("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.exifinterface:exifinterface:1.3.2")
    implementation("androidx.annotation:annotation:1.2.0")
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$verCoroutines")
    implementation("androidx.activity:activity:$verActivity")
    implementation("androidx.activity:activity-ktx:$verActivity")
    implementation("androidx.fragment:fragment:$varFragment")
    implementation("androidx.fragment:fragment-ktx:$varFragment")
}