package com.appliedrec.verid3.facedetection.mp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.appliedrec.verid3.common.Image
import com.appliedrec.verid3.common.serialization.fromBitmap
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class FaceDetectionTest {

    lateinit var faceDetection: FaceDetectionMediaPipe

    @Before
    fun createFaceDetector() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        faceDetection = FaceDetectionMediaPipe(appContext)
    }

    @Test
    fun testDetectFaceInImage(): Unit = runBlocking {
        InstrumentationRegistry.getInstrumentation().context.assets.open("Photo 04-05-2016, 18 57 50.png").use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val image = Image.fromBitmap(bitmap)
            val faces = faceDetection.detectFacesInImage(image, 1)
            assertEquals(1, faces.size)
        }
    }

    @Test
    fun testAngleOfFace() {
        val leftEye = PointF(0.3f, 0.5f)
        val rightEye = PointF(0.7f, 0.5f)
        val leftEarTragion = PointF(0.2f, 0.55f)
        val rightEarTragion = PointF(0.8f, 0.55f)
        val noseTip = PointF(0.5f, 0.65f)
        var angle = faceDetection.angleFromKeypoints(leftEye, rightEye, noseTip, leftEarTragion, rightEarTragion)
        assertEquals(0f, angle.yaw, 0.001f)
        assertEquals(0f, angle.roll, 0.001f)
        noseTip.x = 0.6f
        angle = faceDetection.angleFromKeypoints(leftEye, rightEye, noseTip, leftEarTragion, rightEarTragion)
        assertEquals(0f, angle.roll, 0.001f)
        assertTrue(angle.yaw > 0f)
        noseTip.x = 0.4f
        angle = faceDetection.angleFromKeypoints(leftEye, rightEye, noseTip, leftEarTragion, rightEarTragion)
        assertEquals(0f, angle.roll, 0.001f)
        assertTrue(angle.yaw < 0f)
        noseTip.x = 0.5f
        leftEye.y = 0.6f
        leftEarTragion.y = 0.7f
        rightEarTragion.y = 0.5f
        angle = faceDetection.angleFromKeypoints(leftEye, rightEye, noseTip, leftEarTragion, rightEarTragion)
        assertTrue(angle.roll < 0f)
        leftEye.y = 0.5f
        leftEarTragion.y = 0.7f
        rightEarTragion.y = 0.7f
        noseTip.y = 0.3f
        angle = faceDetection.angleFromKeypoints(leftEye, rightEye, noseTip, leftEarTragion, rightEarTragion)
        assertTrue(angle.pitch < 0f)
    }

    @Test
    fun testPlotLandmarksOnImage(): Unit = runBlocking {
        InstrumentationRegistry.getInstrumentation().context.assets.open("Photo 04-05-2016, 18 57 50.png").use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val image = Image.fromBitmap(bitmap)
            val face = faceDetection.detectFacesInImage(image, 1).firstOrNull()
            assertNotNull(face)
            val landmarks = face!!.landmarks
            val outputImage = bitmap.copy(bitmap.config!!, true)
            val canvas = Canvas(outputImage)
            val paint = Paint().apply {
                style = Paint.Style.FILL
                color = Color.GREEN
            }
            landmarks.forEachIndexed { index, landmark ->
//                canvas.drawCircle(landmark.x, landmark.y, 5f, paint)
                canvas.drawText(index.toString(), landmark.x, landmark.y, paint)
            }
            InstrumentationRegistry.getInstrumentation().context.openFileOutput("landmarks.png", 0).use {
                outputImage.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }
}