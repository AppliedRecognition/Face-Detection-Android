package com.appliedrec.verid3.facedetection.mplandmarks

import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.appliedrec.verid3.common.Image
import com.appliedrec.verid3.common.serialization.fromBitmap
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class FaceDetectionTest {

    lateinit var faceDetection: FaceLandmarkDetectionMediaPipe

    @Before
    fun createFaceDetector() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        faceDetection = FaceLandmarkDetectionMediaPipe(appContext)
    }

    @Test
    fun testDetectFaceInImage(): Unit = runBlocking {
        val image = InstrumentationRegistry.getInstrumentation().context.assets.open("Photo 04-05-2016, 18 57 50.png").use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            Image.fromBitmap(bitmap)
        }
        val faces = faceDetection.detectFacesInImage(image, 1)
        assertEquals(1, faces.size)
    }

    @Test
    fun testFilterLandmarks(): Unit = runBlocking {
        val image = InstrumentationRegistry.getInstrumentation().context.assets.open("Photo 04-05-2016, 18 57 50.png").use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            Image.fromBitmap(bitmap)
        }
        val faces = faceDetection.detectFacesInImage(image, 1)
        assertEquals(1, faces.size)
        val face = faceDetection.filterLandmarksOnFace(faces[0])
        assertEquals(68, face.landmarks.size)
    }
}