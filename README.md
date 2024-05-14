# Face Detection for Android

Collection of Android face detection libraries for Ver-ID SDK

All libraries in this collection implement the [FaceDetection](https://github.com/AppliedRecognition/Ver-ID-Common-Types-Android/blob/main/lib/src/main/java/com/appliedrec/verid/common/FaceDetection.kt) interface from the [Ver-ID common types library](https://github.com/AppliedRecognition/Ver-ID-Common-Types-Android). 
The interface conformance makes the libraries available to the Ver-ID SDK's face capture.

## Installation

The face detection libraries are released on [Maven Central](https://central.sonatype.com). Ensure that `repositories` in your build file contain `mavenCentral()`.

Add the following dependency in your **build.gradle.kts** file:

```
implementation("com.appliedrec.verid:face-detection-mp:1.0.0")
```

## Usage

All the libraries in this project implement the [FaceDetection](https://github.com/AppliedRecognition/Ver-ID-Common-Types-Android/blob/main/lib/src/main/java/com/appliedrec/verid/common/FaceDetection.kt) interface. The interface contains a single method `detectFacesInImage`.

### Example: detecting a face in image using [MediaPipe face detection](https://developers.google.com/mediapipe/solutions/vision/face_detector/android) wrapper

```kotlin
import android.content.Context
import android.graphics.Bitmap
import com.appliedrec.verid.facedetection.mp.FaceDetection
import com.appliedrec.verid.common.BitmapImage
import com.appliedrec.verid.common.Face

fun detectFaceInImage(context: Context, image: Bitmap) -> Face? {
  // Create face detection instance
  val faceDetection = FaceDetection(context)
  // Convert the bitmap to a Ver-ID image
  val image = BitmapImage(bitmap).convertToImage()
  // Set the maximum number of faces to detect
  val limit = 1
  // Detect face
  return faceDetection.detectFacesInImage(image, limit).firstOrNull()
}
```
