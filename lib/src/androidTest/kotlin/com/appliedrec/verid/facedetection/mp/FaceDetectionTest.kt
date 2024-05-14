package com.appliedrec.verid.facedetection.mp

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.appliedrec.verid.common.BitmapImage
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class FaceDetectionTest {

    @Test
    fun testDetectFaceInImage() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val faceDetection = FaceDetection(appContext)
        InstrumentationRegistry.getInstrumentation().context.assets.open("Photo 04-05-2016, 18 57 50.png").use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val image = BitmapImage(bitmap).convertToImage()
            val faces = faceDetection.detectFacesInImage(image, 1)
            assertEquals(1, faces.size)
        }
    }
}