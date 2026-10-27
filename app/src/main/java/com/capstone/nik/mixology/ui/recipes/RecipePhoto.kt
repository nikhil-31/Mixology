package com.capstone.nik.mixology.ui.recipes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

/** Turns a picked image into the JPEG the server takes. */
interface RecipePhotoLoader {
    suspend fun load(uri: Uri): ByteArray
}

class ContentRecipePhotoLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) : RecipePhotoLoader {
    override suspend fun load(uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("Can't open $uri")
        RecipePhotoEncoder.encode(bytes)
    }
}

/**
 * Upright, at most [MAX_SIDE] px on the longer side, JPEG at [JPEG_QUALITY].
 * Phone photos are often stored sideways with an EXIF orientation, which the
 * server would not apply, so it is baked into the pixels here.
 */
object RecipePhotoEncoder {
    const val MAX_SIDE = 1200
    const val JPEG_QUALITY = 85

    /** Blocking; call off the main thread. */
    fun encode(image: ByteArray): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(image, 0, image.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("Not an image")

        // Decode at a power-of-two reduction first so huge photos don't fill memory.
        val decoded = BitmapFactory.decodeByteArray(
            image,
            0,
            image.size,
            BitmapFactory.Options().apply { inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight) },
        ) ?: throw IOException("Not an image")

        val orientation = runCatching {
            ExifInterface(ByteArrayInputStream(image))
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val scale = scaleFor(decoded.width, decoded.height)
        val matrix = orientationMatrix(orientation).apply { postScale(scale, scale) }
        val upright = if (matrix.isIdentity) {
            decoded
        } else {
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        }
        return ByteArrayOutputStream().use { out ->
            upright.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            if (upright !== decoded) upright.recycle()
            decoded.recycle()
            out.toByteArray()
        }
    }

    /** The largest power of two that keeps the longer side at or above [maxSide]. */
    fun sampleSize(width: Int, height: Int, maxSide: Int = MAX_SIDE): Int {
        var sample = 1
        while (max(width, height) / (sample * 2) >= maxSide) sample *= 2
        return sample
    }

    /** Down to [maxSide] on the longer side; never up. */
    fun scaleFor(width: Int, height: Int, maxSide: Int = MAX_SIDE): Float {
        val longer = max(width, height)
        return if (longer <= maxSide) 1f else maxSide.toFloat() / longer
    }

    /** The output size for a [width]×[height] image, before rotation. */
    fun targetSize(width: Int, height: Int, maxSide: Int = MAX_SIDE): Pair<Int, Int> {
        val scale = scaleFor(width, height, maxSide)
        return (width * scale).roundToInt() to (height * scale).roundToInt()
    }

    /** Clockwise degrees an EXIF orientation needs; mirrored variants included. */
    fun rotationDegrees(orientation: Int): Int = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_TRANSPOSE -> 90
        ExifInterface.ORIENTATION_ROTATE_180, ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180
        ExifInterface.ORIENTATION_ROTATE_270, ExifInterface.ORIENTATION_TRANSVERSE -> 270
        else -> 0
    }

    /** Rotate, then mirror for the flipped orientations. */
    private fun orientationMatrix(orientation: Int): Matrix = Matrix().apply {
        val degrees = rotationDegrees(orientation)
        if (degrees != 0) postRotate(degrees.toFloat())
        if (isMirrored(orientation)) postScale(-1f, 1f)
    }

    private fun isMirrored(orientation: Int): Boolean = orientation in setOf(
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
        ExifInterface.ORIENTATION_FLIP_VERTICAL,
        ExifInterface.ORIENTATION_TRANSPOSE,
        ExifInterface.ORIENTATION_TRANSVERSE,
    )
}
