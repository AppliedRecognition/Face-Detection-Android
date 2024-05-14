plugins {
    id("com.android.library")
    alias(libs.plugins.jetbrainsKotlinAndroid)
    `maven-publish`
    signing
}

android {
    namespace = "com.appliedrec.verid.facedetection.mp"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.mediapipe.vision)
    implementation(libs.verid.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

publishing {
    publications {
        create<MavenPublication>("lib") {
            groupId = "com.appliedrec.verid"
            artifactId = "face-detection-mp"
            version = "1.0.0"
            afterEvaluate {
                from(components["release"])
            }
            pom {
                name.set("MediaPipe Face Detection for Ver-ID")
                description.set("Face detection implementation for Ver-ID SDK using MediaPipe")
                url.set("https://github.com/AppliedRecognition/Face-Detection-MediaPipe-Android")
                licenses {
                    license {
                        name.set("Commercial")
                        url.set("https://raw.githubusercontent.com/AppliedRecognition/Face-Detection-MediaPipe-Android/main/LICENCE.txt")
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
                    connection.set("scm:git:git://github.com/AppliedRecognition/Face-Detection-MediaPipe-Android.git")
                    developerConnection.set("scm:git:ssh://github.com/AppliedRecognition/Face-Detection-MediaPipe-Android.git")
                    url.set("https://github.com/AppliedRecognition/Face-Detection-MediaPipe-Android")
                }
            }
        }
    }

    repositories {
        maven {
            name = "MavenCentral"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("mavenCentralUsername") as String?
                password = project.findProperty("mavenCentralPassword") as String?
            }
        }
    }
}

signing {
    sign(publishing.publications["lib"])
}