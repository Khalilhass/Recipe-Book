package com.example.recipe_book.data.remote

import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.recipe_book.util.Constants
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CloudinaryImageDataSource @Inject constructor(
    private val mediaManager: MediaManager
) {
    suspend fun uploadImage(file: File, publicId: String, isProfile: Boolean = false): String =
        suspendCancellableCoroutine { continuation ->
            val folder = if (isProfile) Constants.CLOUDINARY_PROFILE_IMAGE_FOLDER else Constants.CLOUDINARY_RECIPE_IMAGE_FOLDER
            val requestId = mediaManager.upload(file.absolutePath)
                .unsigned(Constants.CLOUDINARY_UPLOAD_PRESET)
                .option("folder", folder)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) = Unit
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) = Unit

                    override fun onSuccess(requestId: String, resultData: MutableMap<Any?, Any?>) {
                        val secureUrl = resultData["secure_url"] as? String
                        if (secureUrl != null) {
                            continuation.resume(secureUrl)
                        } else {
                            continuation.resumeWithException(
                                IllegalStateException(
                                    "Cloudinary upload succeeded but returned no secure_url"
                                )
                            )
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resumeWithException(error.toUploadException())
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        continuation.resumeWithException(error.toUploadException())
                    }
                })
                .dispatch()

            continuation.invokeOnCancellation {
                mediaManager.cancelRequest(requestId)
            }
        }

    private fun ErrorInfo.toUploadException(): Exception {
        val message = description?.takeIf { it.isNotBlank() } ?: "Image upload failed"
        val isLikelyNetworkIssue =
            message.contains("network", ignoreCase = true) ||
                message.contains("timeout", ignoreCase = true) ||
                message.contains("timed out", ignoreCase = true) ||
                message.contains("connection", ignoreCase = true)

        return if (isLikelyNetworkIssue) IOException(message) else IllegalStateException(message)
    }
}