# Face Detection for Android

Collection of Android face detection libraries for Ver-ID SDK

All libraries in this collection implement the [FaceDetection](https://github.com/AppliedRecognition/Ver-ID-Common-Types-Android/blob/main/lib/src/main/java/com/appliedrec/verid/common/FaceDetection.kt) interface from the [Ver-ID common types library](https://github.com/AppliedRecognition/Ver-ID-Common-Types-Android). 
The interface conformance makes the libraries available to the Ver-ID SDK's face capture.

## Installation

1. Add the following to your project's **settings.gradle.kts**:

    ```kotlin
    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            google()
            mavenCentral()
            maven {
                url = uri("https://maven.pkg.github.com/AppliedRecognition/Ver-ID-Releases-Android")
            }
        }
    }
    ```
2. Add the following dependency in your **build.gradle.kts** file:

    ```kotlin
    // BOM
    implementation(platform("com.appliedrec.verid3:ver-id-bom:2025-06-04"))
    // Serialization library for converting Bitmap to Image
    implementation("com.appliedrec.verid:common-serialization")
    // Choose between the two modules:
    implementation("com.appliedrec.verid:face-detection-mp") // For simple face detector
    implementation("com.appliedrec.verid:face-landmark-detection-mp") // For face landmark detector
    ```

## Usage

All the libraries in this project implement the [FaceDetection](https://github.com/AppliedRecognition/Ver-ID-Common-Types-Android/blob/main/lib/src/main/java/com/appliedrec/verid/common/FaceDetection.kt) interface. The interface contains a single method `detectFacesInImage`.

### Example: detecting a face in image using [MediaPipe face detection](https://developers.google.com/mediapipe/solutions/vision/face_detector/android) wrapper

```kotlin
import android.content.Context
import android.graphics.Bitmap
import com.appliedrec.verid3.common.Face
import com.appliedrec.verid3.common.Image
import com.appliedrec.verid3.common.serialization.fromBitmap
import com.appliedrec.verid3.facedetection.mp.FaceDetection

suspend fun detectFaceInImage(context: Context, bitmap: Bitmap): Face? {
  // Create face detection instance
  val faceDetection = FaceDetection(context)
  // Convert the bitmap to a Ver-ID image
  val image = Image.fromBitmap(bitmap)
  // Set the maximum number of faces to detect
  val limit = 1
  // Detect face
  return faceDetection.detectFacesInImage(image, limit).firstOrNull()
}
```
