package com.appliedrec.verid.facedetection.mp

import android.content.Context
import android.graphics.PointF
import com.appliedrec.verid.common.EulerAngle
import com.appliedrec.verid.common.Face
import com.appliedrec.verid.common.Image
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.Detection
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import kotlin.jvm.optionals.getOrNull
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt

class FaceDetection(context: Context): com.appliedrec.verid.common.FaceDetection {

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

    override fun detectFacesInImage(image: Image, limit: Int): Array<Face> {
        val bitmap = image.convertToBitmap()
        val mediapipeImage = BitmapImageBuilder(bitmap).build()
        val result = faceDetector.detect(mediapipeImage)
        return result.detections().map { detection ->
            Face(detection.boundingBox(), angleOfFace(detection), detection.categories().firstOrNull()?.score() ?: 10f, landmarksOfFace(detection))
        }.toTypedArray()
    }

    private fun landmarksOfFace(face: Detection): Array<PointF> {
        val landmarks = face.keypoints().getOrNull()
        if (landmarks == null) {
            return emptyArray()
        }
        return landmarks.map { PointF(it.x(), it.y()) }.toTypedArray()
    }

    private fun angleOfFace(face: Detection): EulerAngle<Float> {
        val keypoints = face.keypoints().getOrNull()
        if (keypoints == null) {
            return EulerAngle(0.0f, 0.0f, 0.0f)
        }
        val noseTip = keypoints.find { it.label().getOrNull() == "noseTip" }
        val leftEarTragion = keypoints.find { it.label().getOrNull() == "leftEarTragion" }
        val rightEarTragion = keypoints.find { it.label().getOrNull() == "rightEarTragion" }
        if (noseTip == null || leftEarTragion == null || rightEarTragion == null) {
            return EulerAngle(0.0f, 0.0f, 0.0f)
        }
        val centreX = leftEarTragion.x() + (rightEarTragion.x() - leftEarTragion.x()) / 2
        val x: Float = rightEarTragion.x() - leftEarTragion.x()
        val y: Float = noseTip.x() - centreX
        var yaw: Float = 180f - atan2(y, x) * (180f / PI.toFloat());
        if (yaw > 180) {
            yaw -= 360;
        }
        yaw *= 1.5f;
        val radius = sqrt(x * x + y * y);
        val centreY = leftEarTragion.y() + (rightEarTragion.y() - leftEarTragion.y()) / 2
        val pitch: Float = sin((noseTip.y() - centreY) / radius) * (180f / PI.toFloat()) - 10f
        return EulerAngle(yaw, pitch, 0.0f);
    }
}