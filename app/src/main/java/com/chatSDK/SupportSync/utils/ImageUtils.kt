package com.chatSDK.SupportSync.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream

object ImageUtils {
    private const val MAX_IMAGE_SIZE = 1024 * 1024 // 1MB
    private const val QUALITY = 80 // Image compression quality (0-100)
    private const val MAX_DIMENSION = 1024 // Maximum width or height

    fun compressImage(context: Context, uri: Uri): Result<ByteArray> {
        return try {
            // Read input stream from Uri
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot read image"))

            // Decode image size first
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate sample size
            var sampleSize = 1
            val maxDimension = maxOf(options.outWidth, options.outHeight)
            while (maxDimension / sampleSize > MAX_DIMENSION) {
                sampleSize *= 2
            }

            // Decode with sample size
            val newInputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot read image"))

            val compressOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val bitmap = BitmapFactory.decodeStream(newInputStream, null, compressOptions)
                ?: return Result.failure(Exception("Cannot decode image"))
            newInputStream.close()

            // Compress to bytes
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, outputStream)

            val compressedBytes = outputStream.toByteArray()
            Log.d("ImageUtils", "Compressed image size: ${compressedBytes.size} bytes")

            if (compressedBytes.size > MAX_IMAGE_SIZE) {
                return Result.failure(Exception("Image too large. Maximum size is 1MB"))
            }

            Result.success(compressedBytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}