package com.capstone.nik.mixology.ui.recipes

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RecipePhotoEncoderTest {

    @Test
    fun scalesTheLongerSideDownToTheLimitButNeverUp() {
        assertEquals(1200 to 900, RecipePhotoEncoder.targetSize(4000, 3000))
        assertEquals(675 to 1200, RecipePhotoEncoder.targetSize(1800, 3200))
        assertEquals(800 to 600, RecipePhotoEncoder.targetSize(800, 600))
        assertEquals(1f, RecipePhotoEncoder.scaleFor(1200, 1200))
    }

    @Test
    fun sampleSizeStaysAboveTheLimit() {
        assertEquals(1, RecipePhotoEncoder.sampleSize(2000, 1500))
        assertEquals(2, RecipePhotoEncoder.sampleSize(4000, 3000))
        assertEquals(4, RecipePhotoEncoder.sampleSize(8000, 6000))
    }

    @Test
    fun exifOrientationMapsToRotation() {
        assertEquals(0, RecipePhotoEncoder.rotationDegrees(ExifInterface.ORIENTATION_NORMAL))
        assertEquals(90, RecipePhotoEncoder.rotationDegrees(ExifInterface.ORIENTATION_ROTATE_90))
        assertEquals(180, RecipePhotoEncoder.rotationDegrees(ExifInterface.ORIENTATION_ROTATE_180))
        assertEquals(270, RecipePhotoEncoder.rotationDegrees(ExifInterface.ORIENTATION_ROTATE_270))
        assertEquals(90, RecipePhotoEncoder.rotationDegrees(ExifInterface.ORIENTATION_TRANSPOSE))
    }

    @Test
    fun encodeShrinksALargeImageToAJpeg() {
        val png = ByteArrayOutputStream().use { out ->
            Bitmap.createBitmap(2400, 1600, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }

        val jpeg = RecipePhotoEncoder.encode(png)

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size, bounds)
        assertEquals(1200, bounds.outWidth)
        assertEquals(800, bounds.outHeight)
        assertEquals("image/jpeg", bounds.outMimeType)
    }
}
