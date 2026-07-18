package com.example.recipe_book.data.repository

import android.content.Context
import android.net.Uri
import com.example.recipe_book.data.remote.CloudinaryImageDataSource
import com.example.recipe_book.data.remote.toAppError
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.repository.ImageRepository
import com.example.recipe_book.util.ImageCompressor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cloudinaryImageDataSource: CloudinaryImageDataSource
) : ImageRepository {

    override suspend fun uploadRecipeImage(uri: Uri, recipeId: String): Result<String> =
        runCatching {
            // Bitmap decode/compress is blocking CPU work — Dispatchers.Default
            // keeps it off the main thread without tying up the IO dispatcher's
            // thread pool meant for network/disk waiting.
            val compressedFile = withContext(Dispatchers.Default) {
                ImageCompressor.compress(context, uri)
            }
            try {
                cloudinaryImageDataSource.uploadImage(compressedFile, recipeId)
            } finally {
                compressedFile.delete() // clean up the temp file regardless of outcome
            }
        }.recoverCatching { throwable ->
            throw AppException(throwable.toAppError())
        }

    override suspend fun uploadProfileImage(uri: Uri, uid: String): Result<String> =
        runCatching {
            val compressedFile = withContext(Dispatchers.Default) {
                ImageCompressor.compress(context, uri)
            }
            try {
                cloudinaryImageDataSource.uploadImage(compressedFile, uid, isProfile = true)
            } finally {
                compressedFile.delete()
            }
        }.recoverCatching { throwable ->
            throw AppException(throwable.toAppError())
        }
}
