package com.example.recipe_book.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import androidx.core.graphics.scale

/**
 * Downscales the picked image to a max dimension of 1080px and re-encodes
 * as JPEG at 80% quality before upload — keeps upload time and mobile data
 * usage down (blueprint Section 10.2). Lives in the data layer's call path
 * only (ImageRepositoryImpl), never imported by the domain layer.
 */
object ImageCompressor {

    private const val MAX_DIMENSION = 1080
    private const val JPEG_QUALITY = 80

    fun compress(context: Context, sourceUri: Uri): File {
        val original = decodeBitmap(context, sourceUri)
        val scaled = scaleDown(original)
        val outputFile = File.createTempFile("recipe_image_", ".jpg", context.cacheDir)
        FileOutputStream(outputFile).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        }
        if (scaled !== original) original.recycle()
        scaled.recycle()
        return outputFile
    }

    private fun decodeBitmap(context: Context, uri: Uri): Bitmap {
        val stream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Could not open the selected image")
        return stream.use {
            BitmapFactory.decodeStream(it)
                ?: throw IllegalArgumentException("Could not decode the selected image")
        }
    }

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        val longestSide = max(bitmap.width, bitmap.height)
        if (longestSide <= MAX_DIMENSION) return bitmap
        val scaleFactor = MAX_DIMENSION.toFloat() / longestSide
        val newWidth = (bitmap.width * scaleFactor).toInt()
        val newHeight = (bitmap.height * scaleFactor).toInt()
        return  bitmap.scale(newWidth, newHeight)
    }
}
