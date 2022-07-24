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
    pomFromGradleProperties()
    publishToMavenCentral()
}

android {
    compileSdk = 32
    defaultConfig {
        minSdk = 16
        targetSdk = 32
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = false
    }
}

val coroutinesVer: String by project
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVer")
    implementation("androidx.activity:activity:$verActivity")
    implementation("androidx.activity:activity-ktx:$verActivity")
    implementation("androidx.fragment:fragment:$varFragment")
    implementation("androidx.fragment:fragment-ktx:$varFragment")
}