import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    id("com.android.library")
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.dokka)
    signing
}

version = "2.0.1"

android {
    namespace = "com.appliedrec.verid3.facedetection.mp"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testOptions.targetSdk = 34

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.mediapipe.vision)
    api(libs.verid.common)
    implementation(libs.verid.common.serialization)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.kotlinx.coroutines.core)
}

mavenPublishing {
    coordinates("com.appliedrec", "face-landmark-detection-mp")
    pom {
        name.set("MediaPipe Face Landmark Detection for Ver-ID")
        description.set("Face detection implementation for Ver-ID SDK using MediaPipe face landmark detection")
        url.set("https://github.com/AppliedRecognition/Face-Detection-Android")
        licenses {
            license {
                name.set("Commercial")
                url.set("https://raw.githubusercontent.com/AppliedRecognition/Face-Detection-Android/main/LICENCE.txt")
            }
        }
        developers {
            developer {
                id.set("appliedrec")
                name.set("Applied Recognition")
                email.set("support@appliedrecognition.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/AppliedRecognition/Face-Detection-Android.git")
            developerConnection.set("scm:git:ssh://github.com/AppliedRecognition/Face-Detection-Android.git")
            url.set("https://github.com/AppliedRecognition/Face-Detection-Android")
        }
    }
    publishToMavenCentral(automaticRelease = true)
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

tasks.withType<DokkaTaskPartial>().configureEach {
    moduleName.set("MediaPipe face landmark detection")
    moduleVersion.set(project.version.toString())
}