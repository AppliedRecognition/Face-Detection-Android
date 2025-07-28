package com.appliedrec.verid3.facedetection.mp

import android.content.Context
import android.graphics.PointF
import com.appliedrec.verid3.common.EulerAngle
import com.appliedrec.verid3.common.Face
import com.appliedrec.verid3.common.IImage
import com.appliedrec.verid3.common.serialization.toBitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.Detection
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import kotlin.jvm.optionals.getOrNull
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt

class FaceDetectionMediaPipe(context: Context): com.appliedrec.verid3.common.FaceDetection {

    private val faceDetector: FaceDetector

    init {
        val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath("blaze_face_short_range.tflite")

        val optionsBuilder =
            FaceDetector.FaceDetectorOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setRunningMode(RunningMode.IMAGE)

        val options = optionsBuilder.build()
        faceDetector = FaceDetector.createFromOptions(context, options)
    }

    override suspend fun detectFacesInImage(image: IImage, limit: Int): List<Face> {
        val bitmap = image.toBitmap()
        val mediapipeImage = BitmapImageBuilder(bitmap).build()
        val result = faceDetector.detect(mediapipeImage)
        return result.detections().mapNotNull { detection ->
            val landmarks = landmarksOfFace(detection, image.width, image.height)
            val leftEye = leftEye(detection) ?: return@mapNotNull null
            val rightEye = rightEye(detection) ?: return@mapNotNull null
            Face(detection.boundingBox(), angleOfFace(detection), (detection.categories().firstOrNull()?.score() ?: 1f) * 10f, landmarks, leftEye, rightEye, noseTip(detection), mouthCentre(detection))
        }
    }

    private fun landmarksOfFace(face: Detection, imageWidth: Int, imageHeight: Int): Array<PointF> {
        val landmarks = face.keypoints().getOrNull() ?: return emptyArray()
        return landmarks.map { PointF(it.x() * imageWidth.toFloat(), it.y() * imageHeight.toFloat()) }.toTypedArray()
//        // This is for landmarker
//        val landmarkIndices = intArrayOf(
//            // jaw
//            127, 234, 93, 58, 172, 136, 149, 148, 152, 377, 378, 365, 397, 288, 323, 454, 356,
//            // eyebrows
//            70, 63, 105, 66, 107, 336, 296, 334, 293, 300,
//            // nose
//            168, 197, 195, 4, 240, 97, 2, 326, 460,
//            // eyes
//            33, 160, 158, 155, 153, 144, 382, 385, 387, 263, 373, 380,
//            // mouth (outer)
//            61, 39, 37, 0, 267, 269, 291, 405, 314, 17, 84, 181,
//            // mouth (inner)
//            78, 82, 13, 312, 308, 317, 14, 87)
//        val landmarks68 = landmarks.mapIndexedNotNull { index, landmark ->
//            if (landmarkIndices.contains(index)) {
//                PointF(landmark.x() * imageWidth.toFloat(), landmark.y() * imageHeight.toFloat())
//            } else {
//                null
//            }
//        }.toMutableList()
//        // Left eye
//        landmarks68.add(PointF(landmarks[468].x() * imageWidth.toFloat(), landmarks[468].y() * imageHeight.toFloat()))
//        // Right eye
//        landmarks68.add(PointF(landmarks[473].x() * imageWidth.toFloat(), landmarks[473].y() * imageHeight.toFloat()))
//        return landmarks68.toTypedArray()
    }

    private fun leftEye(face: Detection): PointF? {
        val keypoints = face.keypoints().getOrNull()
        if (keypoints == null || keypoints.size < 6) {
            return null
        }
        return PointF(keypoints[0].x(), keypoints[0].y())
    }

    private fun rightEye(face: Detection): PointF? {
        val keypoints = face.keypoints().getOrNull()
        if (keypoints == null || keypoints.size < 6) {
            return null
        }
        return PointF(keypoints[1].x(), keypoints[1].y())
    }

    private fun noseTip(face: Detection): PointF? {
        val keypoints = face.keypoints().getOrNull()
        if (keypoints == null || keypoints.size < 6) {
            return null
        }
        return PointF(keypoints[2].x(), keypoints[2].y())
    }

    private fun mouthCentre(face: Detection): PointF? {
        val keypoints = face.keypoints().getOrNull()
        if (keypoints == null || keypoints.size < 6) {
            return null
        }
        return PointF(keypoints[3].x(), keypoints[3].y())
    }

    private fun angleOfFace(face: Detection): EulerAngle<Float> {
        val keypoints = face.keypoints().getOrNull()
        if (keypoints == null || keypoints.size < 6) {
            return EulerAngle(0.0f, 0.0f, 0.0f)
        }
        val leftEye = PointF(keypoints[0].x(), keypoints[0].y())
        val rightEye = PointF(keypoints[1].x(), keypoints[1].y())
        val noseTip = PointF(keypoints[2].x(), keypoints[2].y())
        val leftEarTragion = PointF(keypoints[4].x(), keypoints[4].y())
        val rightEarTragion = PointF(keypoints[5].x(), keypoints[5].y())
        return angleFromKeypoints(leftEye, rightEye, noseTip, leftEarTragion, rightEarTragion)
    }

    internal fun angleFromKeypoints(leftEye: PointF, rightEye: PointF, noseTip: PointF, leftEarTragion: PointF, rightEarTragion: PointF): EulerAngle<Float> {
        val centreX = leftEarTragion.x + (rightEarTragion.x - leftEarTragion.x) / 2
        val x: Float = rightEarTragion.x - leftEarTragion.x
        val y: Float = noseTip.x - centreX
        val yaw: Float = Math.toDegrees(atan2(y, x).toDouble()).toFloat() * 1.5f
        val radius = sqrt(x * x + y * y);
        val centreY = leftEarTragion.y + (rightEarTragion.y - leftEarTragion.y) / 2
        val pitch: Float = Math.toDegrees(sin((noseTip.y - centreY) / radius).toDouble()).toFloat() - 10f
        val deltaY = rightEye.y - leftEye.y
        val deltaX = rightEye.x - leftEye.x
        val roll = Math.toDegrees(atan2(deltaY, deltaX).toDouble()).toFloat()
        return EulerAngle(yaw, pitch, roll)
    }
}