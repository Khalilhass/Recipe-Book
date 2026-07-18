package com.example.recipe_book.domain.repository

import android.net.Uri

interface ImageRepository {
    /**
     * Compresses the image at [uri] and uploads it to Cloudinary under the
     * recipe images folder, using [recipeId] as the asset's public_id.
     * Returns the resulting
     * secure (https) URL to store in Firestore's imageUrl field.
     */
    suspend fun uploadRecipeImage(uri: Uri, recipeId: String): Result<String>

    /**
     * Compresses and uploads a profile photo to Cloudinary.
     * Uses the user's uid as the public_id.
     */
    suspend fun uploadProfileImage(uri: Uri, uid: String): Result<String>
}
