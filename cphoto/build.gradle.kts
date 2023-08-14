import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(buildDir.resolve("javadoc"))
}

mavenPublishing {
    signAllPublications()
    publishToMavenCentral()
}

android {
    namespace = "com.genius.cphoto"
    compileSdk = 33
    defaultConfig {
        minSdk = 16
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
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
val varFragment = "1.6.1"

dependencies {
    compileOnly("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.exifinterface:exifinterface:1.3.6")
    implementation("androidx.annotation:annotation:1.6.0")
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVer")
}