package com.appliedrec.verid3.facedetection.mplandmarks

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import com.appliedrec.verid3.common.EulerAngle
import com.appliedrec.verid3.common.Face
import com.appliedrec.verid3.common.IImage
import com.appliedrec.verid3.common.serialization.toBitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt

class FaceDetection(context: Context): com.appliedrec.verid3.common.FaceDetection {

    private val faceDetector: FaceLandmarker

    init {
        val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath("face_landmarker.task")
        val optionsBuilder =
            FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setRunningMode(RunningMode.IMAGE)

        val options = optionsBuilder.build()
        faceDetector = FaceLandmarker.createFromOptions(context, options)
    }

    override suspend fun detectFacesInImage(image: IImage, limit: Int): Array<Face> {
        val bitmap = image.toBitmap()
        val mediapipeImage = BitmapImageBuilder(bitmap).build()
        val result = faceDetector.detect(mediapipeImage)
        return result.faceLandmarks().mapNotNull { mpLandmarks ->
            val landmarks = landmarksOfFace(mpLandmarks, image.width, image.height)
            val leftEye = leftEye(mpLandmarks, image.width, image.height)
            val rightEye = rightEye(mpLandmarks, image.width, image.height)
            val noseTip = noseTip(mpLandmarks, image.width, image.height)
            val mouthCentre = mouthCentre(mpLandmarks, image.width, image.height)
            val leftEarTragion = landmarkAtIndex(mpLandmarks, 234, image.width, image.height)
            val rightEarTragion = landmarkAtIndex(mpLandmarks, 454, image.width, image.height)
            val angle = angleFromKeypoints(leftEye, rightEye, noseTip, leftEarTragion, rightEarTragion)
            val bounds = boundsOfFace(mpLandmarks, image.width, image.height)
            Face(bounds, angle, 10f, landmarks, leftEye, rightEye, noseTip, mouthCentre)
        }.toTypedArray()
    }

    fun filterLandmarksOnFace(face: Face): Face {
        if (face.landmarks.size == 68) {
            return face
        }
        require(face.landmarks.size == 478)
        val landmarkIndices = intArrayOf(
            // jaw
            127, 234, 93, 58, 172, 136, 149, 148, 152, 377, 378, 365, 397, 288, 323, 454, 356,
            // eyebrows
            70, 63, 105, 66, 107, 336, 296, 334, 293, 300,
            // nose
            168, 197, 195, 4, 240, 97, 2, 326, 460,
            // eyes
            33, 160, 158, 155, 153, 144, 382, 385, 387, 263, 373, 380,
            // mouth (outer)
            61, 39, 37, 0, 267, 269, 291, 405, 314, 17, 84, 181,
            // mouth (inner)
            78, 82, 13, 312, 308, 317, 14, 87)
        val filteredLandmarks = landmarkIndices.map { i ->
            face.landmarks[i]
        }.toTypedArray()
        return Face(
            face.bounds,
            face.angle, face.quality,
            filteredLandmarks,
            face.leftEye,
            face.rightEye,
            face.noseTip,
            face.mouthCentre
        )
    }

    private fun landmarksOfFace(landmarks: List<NormalizedLandmark>, imageWidth: Int, imageHeight: Int): Array<PointF> {
        return landmarks.map { landmark ->
            PointF(landmark.x() * imageWidth.toFloat(), landmark.y() * imageHeight.toFloat())
        }.toTypedArray()
    }

    private fun leftEye(landmarks: List<NormalizedLandmark>, imageWidth: Int, imageHeight: Int): PointF {
        return landmarkAtIndex(landmarks, 468, imageWidth, imageHeight)
    }

    private fun rightEye(landmarks: List<NormalizedLandmark>, imageWidth: Int, imageHeight: Int): PointF {
        return landmarkAtIndex(landmarks, 473, imageWidth, imageHeight)
    }

    private fun noseTip(landmarks: List<NormalizedLandmark>, imageWidth: Int, imageHeight: Int): PointF {
        return landmarkAtIndex(landmarks, 4, imageWidth, imageHeight)
    }

    private fun mouthCentre(landmarks: List<NormalizedLandmark>, imageWidth: Int, imageHeight: Int): PointF {
        val bottomOfTopLip = landmarkAtIndex(landmarks, 13, imageWidth, imageHeight)
        val topOfBottomLip = landmarkAtIndex(landmarks, 14, imageWidth, imageHeight)
        return PointF((bottomOfTopLip.x + topOfBottomLip.x) / 2, (bottomOfTopLip.y + topOfBottomLip.y) / 2)
    }

    private fun landmarkAtIndex(landmarks: List<NormalizedLandmark>, index: Int, imageWidth: Int, imageHeight: Int): PointF {
        return PointF(landmarks[index].x() * imageWidth.toFloat(), landmarks[index].y() * imageHeight.toFloat())
    }

    private fun boundsOfFace(landmarks: List<NormalizedLandmark>, imageWidth: Int, imageHeight: Int): RectF {
        val aspectRatio = 4/5f
        val minX = landmarks.minOf { it.x() } * imageWidth.toFloat()
        val maxX = landmarks.maxOf { it.x() } * imageWidth.toFloat()
        val minY = landmarks.minOf { it.y() } * imageHeight.toFloat()
        val maxY = landmarks.maxOf { it.y() } * imageHeight.toFloat()
        val width = maxX - minX
        val height = maxY - minY
        val size = if (width > height * aspectRatio) {
            width
        } else {
            height * aspectRatio
        } * 1.2f
        val leftEye = leftEye(landmarks, imageHeight, imageHeight)
        val rightEye = rightEye(landmarks, imageHeight, imageHeight)
        val centerX = (leftEye.x + rightEye.x) / 2
        val centerY = landmarkAtIndex(landmarks, 5, imageHeight, imageHeight).y
        val sizeY = size / aspectRatio
        val left = centerX - size / 2
        val top = centerY - sizeY / 2
        return RectF(left, top, left + size, top + sizeY)
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