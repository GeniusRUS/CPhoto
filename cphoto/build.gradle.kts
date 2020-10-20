plugins {
    id("com.jfrog.bintray")
    id("com.github.dcendents.android-maven")
    id("com.android.library")
    kotlin("android")
}

extra.apply{
    set("bintrayRepo", "CPhoto")
    set("bintrayName", "com.geniusrus.cphoto")
    set("libraryName", "cphoto")
    set("publishedGroupId", "com.geniusrus.cphoto")
    set("artifact", "cphoto")
    set("libraryVersion", "3.0.0-b1")
    set("libraryDescription", "Android take photo library on coroutines")
    set("siteUrl", "https://github.com/GeniusRUS/CPhoto")
    set("gitUrl", "https://github.com/GeniusRUS/CPhoto.git")
    set("developerId", "GeniusRUS")
    set("developerName", "Viktor Likhanov")
    set("developerEmail", "Gen1usRUS@yandex.ru")
    set("licenseName", "The Apache Software License, Version 2.0")
    set("licenseUrl", "http://www.apache.org/licenses/LICENSE-2.0.txt")
    set("allLicenses", arrayOf("Apache-2.0"))
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(16)
        targetSdkVersion(30)
        versionCode = 1
        versionName = extra.get("libraryVersion") as String
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

val verCoroutines = "1.3.9"
val verActivity = "1.2.0-beta01"
val varFragment = "1.3.0-beta01"

dependencies {
    testImplementation("junit:junit:4.13.1")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    compileOnly("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.exifinterface:exifinterface:1.3.1")
    implementation("androidx.annotation:annotation:1.1.0")
    implementation(kotlin("stdlib-jdk7", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$verCoroutines")
    // Java language implementation
    implementation("androidx.activity:activity:$verActivity")
    // Kotlin
    implementation("androidx.activity:activity-ktx:$verActivity")
    // Java language implementation
    implementation("androidx.fragment:fragment:$varFragment")
    // Kotlin
    implementation("androidx.fragment:fragment-ktx:$varFragment")
}

if (project.rootProject.file("local.properties").exists()) {
    apply("https://raw.githubusercontent.com/nuuneoi/JCenter/master/installv1.gradle")
    apply("https://raw.githubusercontent.com/nuuneoi/JCenter/master/bintrayv1.gradle")
}